package com.didi.aoe.runtime.tensorflow.lite;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.SingleInterpreterComponent;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供单模型的TensorFlowLite实现，用于多输入、多输出方式调用。
 *
 * @param <TInput>       范型，业务输入数据
 * @param <TOutput>      范型，业务输出数据
 * @param <TModelInput>  范型，模型输入数据
 * @param <TModelOutput> 范型，模型输出数据
 * @author noctis
 */
public abstract class TensorFlowLiteMultipleInputsOutputsInterpreter<TInput, TOutput, TModelInput, TModelOutput>
        extends SingleInterpreterComponent<TInput, TOutput> implements AoeProcessor.MultiConvertor<TInput, TOutput, Object, TModelOutput> {
    private final Logger mLogger = LoggerFactory.getLogger("TensorFlowLite.Interpreter");
    private Interpreter mInterpreter;
    private Map<Integer, Object> outputPlaceholder;

    @Override
    public boolean init(@NonNull Context context, @NonNull AoeModelOption modelOptions) {
        String modelFilePath = modelOptions.getModelDir() + File.separator + modelOptions.getModelName() + ".tflite";
        ByteBuffer bb = loadFromAssets(context, modelFilePath);
        if (bb != null) {
            mInterpreter = new Interpreter(bb);

            outputPlaceholder = generalOutputPlaceholder(mInterpreter);
            return true;
        }
        return false;
    }

    private Map<Integer, Object> generalOutputPlaceholder(@NonNull Interpreter interpreter) {
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

                mInterpreter.runForMultipleInputsOutputs(modelInput, outputPlaceholder);

                //noinspection unchecked
                return postProcessMulti((Map<Integer, TModelOutput>) outputPlaceholder);
            }

        }
        return null;
    }

    @Override
    public void release() {
        mInterpreter.close();
    }

    @Override
    public boolean isReady() {
        return mInterpreter != null && outputPlaceholder != null;
    }

    private ByteBuffer loadFromAssets(Context context, String modelFilePath) {
        InputStream is = null;
        try {
            is = context.getAssets().open(modelFilePath);
            byte[] bytes = read(is);
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
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return null;

    }

    private byte[] read(InputStream is) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        try {
            bis = new BufferedInputStream(is);
            baos = new ByteArrayOutputStream();

            int len;

            byte[] buf = new byte[1024];

            while ((len = bis.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            return baos.toByteArray();

        } catch (Exception e) {
            mLogger.error("read InputStream error: ", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignored) {
                }
            }
        }
        return new byte[0];
    }
}
