package com.didi.aoe.library.core.io

import com.didi.aoe.library.api.ParcelComponent
import java.io.*

/**
 * 默认的对象序列化组件实现
 * Note: 需要被序列化对象实现[java.io.Serializable]接口
 *
 * @author noctis
 */
class AoeParcelImpl : ParcelComponent {
    override fun obj2Byte(obj: Any): ByteArray {
        var bytes: ByteArray? = null
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(obj)
                    objectOutputStream.flush()
                    bytes = byteArrayOutputStream.toByteArray()
                }
            }
        } catch (e: IOException) {
            // ignore

        }
        return bytes!!
    }

    override fun byte2Obj(bytes: ByteArray): Any {
        var obj: Any? = null
        try {
            ByteArrayInputStream(bytes).use { byteArrayInputStream ->
                ObjectInputStream(byteArrayInputStream)
                        .use { objectInputStream -> obj = objectInputStream.readObject() }
            }
        } catch (e: Exception) {
            // ignore

        }
        return obj!!
    }
}