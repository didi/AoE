package com.didi.aoe.library.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.api.StatusCode;
import com.didi.aoe.library.api.interpreter.InterpreterInitResult;
import com.didi.aoe.library.api.interpreter.OnInterpreterInitListener;
import com.didi.aoe.library.core.io.AoeParcelImpl;
import com.didi.aoe.library.core.pojos.Message;
import com.didi.aoe.library.core.service.IAoeProcessService;
import com.didi.aoe.library.lang.AoeRemoteException;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aoe 远程执行服务
 *
 * @author noctis
 */
public class AoeProcessService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger("AoeProcessService");

    private final Map<String, ProcessorDelegate> mProcessorMap = new HashMap<>();

    private final IAoeProcessService.Stub mBinder = new IAoeProcessService.Stub() {

        @Override
        public int init(String clientId, Message options) throws RemoteException {
            byte[] ins = options.getData();

            Context context = AoeProcessService.this.getApplicationContext();

            AoeProcessor.ParcelComponent packer = ComponentProvider.getParceler(AoeParcelImpl.class.getName());
            Object obj = packer.byte2Obj(ins);
            // RemoteOptions 包含一些简单的配置描述，单个AIDL切片为100k，此处直接判断数据，不做数据切片合并
            if (obj instanceof RemoteOptions) {
                RemoteOptions aoeOptions = (RemoteOptions) obj;
                NativeProcessorWrapper processorWrapper = new NativeProcessorWrapper(context, aoeOptions.getClientOptions());

                final CountDownLatch latch = new CountDownLatch(1);

                final AtomicInteger statusCode = new AtomicInteger(StatusCode.STATUS_INNER_ERROR);
                processorWrapper.init(context, aoeOptions.getModelOptions(), new OnInterpreterInitListener() {
                    @Override
                    public void onInitResult(@NonNull InterpreterInitResult result) {
                        statusCode.set(result.getCode());
                        latch.countDown();
                    }

                });
                try {
                    // 最多等待1s
                    latch.await(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOGGER.warn("InterruptedException: ", e);
                }

                LOGGER.debug("init: " + statusCode.get() + ", clientId: " + clientId);

                if (StatusCode.STATUS_OK == statusCode.get()) {
                    // 加载成功，cache执行委托者
                    ProcessorDelegate processorDelegate = new ProcessorDelegate(processorWrapper);
                    mProcessorMap.put(clientId, processorDelegate);
                }
                return statusCode.get();
            } else {
                LOGGER.error("parse init options " + clientId + ": " + obj);
                throw new AoeRemoteException("parse init options " + clientId + ": " + obj);
            }
        }

        @Override
        public Message process(String clientId, Message input) throws RemoteException {
            ProcessorDelegate processor = mProcessorMap.get(clientId);
            if (processor != null) {
                byte[] ins = processor.getPaser().parse(input);
                if (ins != null && ins.length > 0) {
                    if (processor.getParcelComponent() == null) {
                        throw new AoeRemoteException("Process error, ParcelComponent is NULL");
                    }
                    Object modelInput = processor.getParcelComponent().byte2Obj(ins);
                    @SuppressWarnings("unchecked")
                    Object modelOutput = processor.getInterpreterComponent().run(modelInput);

                    if (modelOutput != null) {
                        byte[] outs = processor.getParcelComponent().obj2Byte(modelOutput);
                        return new Message(1, 0, outs);
                    }
                }
            } else {
                throw new AoeRemoteException("Process error, can't found processor for client: " + clientId);
            }
            return null;
        }

        @Override
        public void release(String clientId) throws RemoteException {
            ProcessorDelegate processor = mProcessorMap.get(clientId);
            if (processor != null) {
                processor.getInterpreterComponent().release();
            } else {
                throw new AoeRemoteException("Release error, can't found processor for client: " + clientId);
            }
            mProcessorMap.remove(clientId);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
