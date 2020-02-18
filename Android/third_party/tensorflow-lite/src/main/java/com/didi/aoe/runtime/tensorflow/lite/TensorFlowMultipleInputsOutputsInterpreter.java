package com.didi.aoe.runtime.tensorflow.lite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.StatusCode;
import com.didi.aoe.library.api.convertor.MultiConvertor;
import com.didi.aoe.library.api.domain.ModelSource;
import com.didi.aoe.library.api.interpreter.InterpreterInitResult;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.api.interpreter.SingleInterpreterComponent;
import com.didi.aoe.library.common.util.FileUtils;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * 提供单模型的TensorFlowLite实现，用于多输入、多输出方式调用。
 *
 * @param <TInput>       范型，业务输入数据
 * @param <TOutput>      范型，业务输出数据
 * @param <TModelInput>  范型，模型输入数据
 * @param <TModelOutput> 范型，模型输出数据
 * @author noctis
 */
public abstract class TensorFlowMultipleInputsOutputsInterpreter<TInput, TOutput, TModelInput, TModelOutput>
        extends SingleInterpreterComponent<TInput, TOutput> implements
        MultiConvertor<TInput, TOutput, Object, TModelOutput> {
    private final Logger mLogger = LoggerFactory.getLogger("TFLite.Interpreter");
    private Interpreter mInterpreter;
    private Map<Integer, Object> mOutputPlaceholder;
    private List<Delegate> mDelegates = new ArrayList<>();

    @Override
    public void init(@NonNull Context context,
            @Nullable AoeProcessor.InterpreterComponent.Options interpreterOptions,
            @NonNull AoeModelOption modelOptions,
            @Nullable OnInterpreterInitListener listener) {

        @ModelSource
        String modelSource = modelOptions.getModelSource();
        ByteBuffer bb = null;
        if (ModelSource.CLOUD.equals(modelSource)) {
            String modelFilePath = modelOptions.getModelDir() + File.separator + modelOptions.getVersion() + File.separator + modelOptions.getModelName();
            File modelFile = new File(FileUtils.getFilesDir(context), modelFilePath);
            if (modelFile.exists()) {
                try {
                    bb = loadFromExternal(context, modelFilePath);
                } catch (Exception e) {
                    mLogger.warn("IOException", e);
                }
            } else {
                // 配置为云端模型，本地无文件，返回等待中状态
                if (listener != null) {
                    listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_MODEL_DOWNLOAD_WAITING));
                }
                return;
            }


        } else {
            String modelFilePath = modelOptions.getModelDir() + File.separator + modelOptions.getModelName();
            // local default
            bb = loadFromAssets(context, modelFilePath);
        }

        if (bb != null) {
            Interpreter.Options options = null;
            if (interpreterOptions != null) {
                options = new Interpreter.Options().setNumThreads(interpreterOptions.getNumThreads());
            }
            if (!mDelegates.isEmpty()) {
                Iterator<Delegate> it = mDelegates.iterator();
                while (it.hasNext()) {
                    Delegate delegate = it.next();
                    mLogger.debug("addDelegate: " + delegate);
                    options.addDelegate(delegate);
                }
            }
            mInterpreter = new Interpreter(bb, options);

            mOutputPlaceholder = generalOutputPlaceholder(mInterpreter);
            if (listener != null) {
                listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_OK));
            }
            return;
        } else {
            if (listener != null) {
                listener.onInitResult(InterpreterInitResult.create(StatusCode.STATUS_INNER_ERROR));
            }
        }
    }

    private ByteBuffer loadFromExternal(Context context, String modelFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(FileUtils.getFilesDir(context) + File.separator + modelFilePath);
        FileChannel fileChannel = fis.getChannel();
        long startOffset = fileChannel.position();
        long declaredLength = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private Map<Integer, Object> generalOutputPlaceholder(@NonNull Interpreter interpreter) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, Object> out = new HashMap<>(interpreter.getOutputTensorCount());
        for (int i = 0; i < interpreter.getOutputTensorCount(); i++) {
            Tensor tensor = interpreter.getOutputTensor(i);
            Object data = null;
            switch (tensor.dataType()) {
                case FLOAT32:
                    data = Array.newInstance(Float.TYPE, tensor.shape());
                    break;
                case INT32:
                    data = Array.newInstance(Integer.TYPE, tensor.shape());
                    break;
                case UINT8:
                    data = Array.newInstance(Byte.TYPE, tensor.shape());
                    break;
                case INT64:
                    data = Array.newInstance(Long.TYPE, tensor.shape());
                    break;
                case STRING:
                    data = Array.newInstance(String.class, tensor.shape());
                    break;
                default:
                    // ignore
                    break;
            }
            out.put(i, data);
        }

        return out;
    }

    @Override
    @Nullable
    public TOutput run(@NonNull TInput input) {
        if (isReady()) {
            Object[] modelInput = preProcessMulti(input);

            if (modelInput != null) {

                mInterpreter.runForMultipleInputsOutputs(modelInput, mOutputPlaceholder);

                //noinspection unchecked
                return postProcessMulti((Map<Integer, TModelOutput>) mOutputPlaceholder);
            }

        }
        return null;
    }

    @Override
    public void release() {
        if (mInterpreter != null) {
            mInterpreter.close();
        }
        if (!mDelegates.isEmpty()) {
            for (Delegate delegate : mDelegates) {
                if (delegate instanceof Closeable) {
                    try {
                        ((Closeable) delegate).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public boolean isReady() {
        return mInterpreter != null && mOutputPlaceholder != null;
    }

    private ByteBuffer loadFromAssets(Context context, String modelFilePath) {
        InputStream is = null;
        try {
            is = context.getAssets().open(modelFilePath);
            byte[] bytes = FileUtils.read(is);
            if (bytes == null) {
                return null;
            }
            ByteBuffer bf = ByteBuffer.allocateDirect(bytes.length);
            bf.order(ByteOrder.nativeOrder());
            bf.put(bytes);

            return bf;
        } catch (IOException e) {
            mLogger.error("loadFromAssets error", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return null;

    }

    public void addDelegate(@NonNull Delegate delegate) {
        mDelegates.add(delegate);
    }
}
