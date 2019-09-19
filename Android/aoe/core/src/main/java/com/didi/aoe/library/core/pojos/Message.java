package com.didi.aoe.library.core.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * 与独立进程通信的数据切片
 *
 * @author noctis
 */
public final class Message implements Parcelable {
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    /**
     * 切片数
     */
    private int partNum;
    /**
     * 当前切片索引
     */
    private int partIndex;
    /**
     * 当前切片数据
     */
    private byte[] data;

    public Message() {
    }

    public Message(byte[] data) {
        this(1, 0, data);
    }

    public Message(int partNum, int partIndex, byte[] data) {
        this.partNum = partNum;
        this.partIndex = partIndex;
        this.data = data;
    }

    private Message(Parcel in) {
        this.partNum = in.readInt();
        this.partIndex = in.readInt();
        this.data = in.createByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.partNum);
        dest.writeInt(this.partIndex);
        dest.writeByteArray(this.data);
    }

    public int getPartNum() {
        return partNum;
    }

    public void setPartNum(int partNum) {
        this.partNum = partNum;
    }

    public int getPartIndex() {
        return partIndex;
    }

    public void setPartIndex(int partIndex) {
        this.partIndex = partIndex;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "partNum=" + partNum +
                ", partIndex=" + partIndex +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
