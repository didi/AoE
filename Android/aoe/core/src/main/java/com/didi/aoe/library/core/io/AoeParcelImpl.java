package com.didi.aoe.library.core.io;

import androidx.annotation.NonNull;

import com.didi.aoe.library.api.AoeProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 默认的对象序列化组件实现
 * Note: 需要被序列化对象实现{@link java.io.Serializable}接口
 *
 * @author noctis
 */
public final class AoeParcelImpl implements AoeProcessor.ParcelComponent {

    @Override
    public byte[] obj2Byte(@NonNull Object obj) {
        byte[] bytes = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            // ignore
        }
        return bytes;
    }

    @Override
    public Object byte2Obj(@NonNull byte[] bytes) {
        Object obj = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            // ignore
        }
        return obj;
    }

}
