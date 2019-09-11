package com.didi.aoe.library.core;

import androidx.annotation.NonNull;

import com.didi.aoe.library.core.pojos.Message;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 内部数据拆装包操作工具类
 *
 * @author noctis
 */
final class Paser {
    /**
     * AIDL 通信数据大小限制是1m，不易传送太大文件，按指定大小进行拆包，保证稳定性
     */
    private static final int PART_SIZE = 100 * 1024;
    /**
     * 限制一个文件拆包数量
     */
    private static final int PART_MAX_LENGTH = 500;
    private final Logger mLogger = LoggerFactory.getLogger("Paser");
    private final List<Message> messages = new ArrayList<>();

    /**
     * 将数据按PART_SIZE拆成信息列表
     *
     * @param data 数据文件
     * @return 拆分成的消息数组
     */
    static List<Message> split(@NonNull byte[] data) {
        int partNum = ((data.length % PART_SIZE) == 0)
                ? (data.length / PART_SIZE)
                : (data.length / PART_SIZE) + 1;
        List<Message> msgs = new ArrayList<>(partNum);
        for (int i = 0; i < partNum; i++) {
            Message msg = new Message(partNum, i,
                    Arrays.copyOfRange(data
                            , i * PART_SIZE
                            , Math.min(((i + 1) * PART_SIZE), data.length)
                    )
            );
            msgs.add(msg);
        }
        return msgs;
    }

    /**
     * 数据完整解析（串行），则返回数据，否则返回空
     *
     * @param msg 序列化传递的信息片段
     * @return 接收到完整数据以后，返回整段数据，否则返回空数组。
     */
    byte[] parse(@NonNull Message msg) {
        messages.add(msg);
        if (msg.getPartNum() == msg.getPartIndex() + 1) {
            // 最后一帧数据抵达，合并数据返回
            int totalSize = 0;
            for (Message message : messages) {
                totalSize += message.getData().length;
            }
            byte[] result = new byte[totalSize];
            int index = 0;
            for (Message message : messages) {
                // 拼装数据
                System.arraycopy(message.getData(), 0, result, index, message.getData().length);
                index += message.getData().length;
            }

            // 重置数据
            messages.clear();
            return result;
        }

        if (messages.size() > PART_MAX_LENGTH) {
            mLogger.warn("reach max data size, ignore!!! size: " + messages.size());
            messages.clear();
        }

        return new byte[0];
    }

}
