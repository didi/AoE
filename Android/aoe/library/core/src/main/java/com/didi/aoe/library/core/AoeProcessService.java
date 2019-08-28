package com.didi.aoe.library.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.didi.aoe.library.AoeRemoteException;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.core.io.AoeParcelImpl;
import com.didi.aoe.library.core.pojos.Message;
import com.didi.aoe.library.core.service.IAoeProcessService;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Aoe 远程执行服务
 *
 * @author noctis
 */
public class AoeProcessService extends Service {
    private final Logger mLogger = LoggerFactory.getLogger("AoeProcessService");

    private final Map<String, ProcessorDelegate> mProcessorMap = new HashMap<>();

    private final IAoeProcessService.Stub mBinder = new IAoeProcessService.Stub() {

        @Override
        public boolean init(String clientId, Message options) throws RemoteException {
            byte[] ins = options.getData();

            Context context = AoeProcessService.this.getApplicationContext();

            AoeProcessor.ParcelComponent parceler = ComponentProvider.getParceler(AoeParcelImpl.class.getName());
            Object obj = parceler.byte2Obj(ins);
            if (obj instanceof RemoteOptions) {
                RemoteOptions aoeOptions = (RemoteOptions) obj;
                NativeProcessorWrapper processorWrapper = new NativeProcessorWrapper(context, aoeOptions.getClientOptions());
                boolean initResult = processorWrapper.init(context, aoeOptions.getModelOptions());
                mLogger.debug("init: " + initResult + ", clientId: " + clientId);
                if (initResult) {
                    ProcessorDelegate processorDelegate = new ProcessorDelegate(processorWrapper);
                    mProcessorMap.put(clientId, processorDelegate);
                }
                return initResult;
            } else {
                mLogger.error("parse init options " + clientId + ": " + obj);
                throw new AoeRemoteException("parse init options " + clientId + ": " + obj);
            }
        }

        @Override
        //TODO 切割合并功能没有了？？？？
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
