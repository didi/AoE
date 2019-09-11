package com.didi.aoe.library.core.io;

import androidx.annotation.NonNull;

import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.core.util.CloseUtil;

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
public class AoeParcelImpl implements AoeProcessor.ParcelComponent {

    @Override
    public byte[] obj2Byte(@NonNull Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            // ignore
        } finally {
            CloseUtil.close(objectOutputStream);
            CloseUtil.close(byteArrayOutputStream);
        }
        return bytes;
    }

    @Override
    public Object byte2Obj(@NonNull byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            // ignore
        } finally {
            CloseUtil.close(objectInputStream);
            CloseUtil.close(byteArrayInputStream);
        }
        return obj;
    }

}
