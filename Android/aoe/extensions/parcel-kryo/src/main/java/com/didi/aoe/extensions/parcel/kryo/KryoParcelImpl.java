package com.didi.aoe.extensions.parcel.kryo;

import android.support.annotation.NonNull;

import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author noctis
 */
public class KryoParcelImpl implements AoeProcessor.ParcelComponent {
    private static final Logger mLogger = LoggerFactory.getLogger("KryoParcelImpl");

    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(ByteBuffer.allocate(0).getClass(), new ByteBufferSerializer());
            return kryo;
        }

    };

    @Override
    public byte[] obj2Byte(@NonNull Object obj) {
        ByteArrayOutputStream bo = null;
        Output output = null;
        try {
            // object to bytearray
            bo = new ByteArrayOutputStream();
            output = new Output(bo);
            //noinspection ConstantConditions
            kryos.get().writeClassAndObject(output, obj);
            output.flush();

            return bo.toByteArray();
        } catch (Exception e) {
            mLogger.error("obj2Byte error", e);
        } finally {
            if (bo != null) {
                try {
                    bo.close();
                } catch (IOException e) {
                    mLogger.error("obj2Byte io error", e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    mLogger.error("obj2Byte io error", e);
                }
            }
        }
        return new byte[0];
    }

    @Override
    public Object byte2Obj(@NonNull byte[] bytes) {
        ByteArrayInputStream bi = null;
        Object obj;
        Input input = null;
        try {
            // bytearray to object
            bi = new ByteArrayInputStream(bytes);

            input = new Input(bi);
            //noinspection ConstantConditions
            obj = kryos.get().readClassAndObject(input);

            return obj;

        } catch (Exception e) {
            mLogger.error("byte2Obj error", e);
        } finally {
            if (bi != null) {
                try {
                    bi.close();
                } catch (IOException e) {
                    mLogger.error("byte2Obj io error", e);
                }
            }

            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    mLogger.error("byte2Obj io error", e);
                }
            }
        }

        return null;
    }

}
