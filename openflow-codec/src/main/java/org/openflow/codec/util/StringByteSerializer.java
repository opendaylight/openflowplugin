package org.openflow.codec.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;

public class StringByteSerializer {
    public static String readFrom(IDataBuffer data, int length) {
        byte[] stringBytes = new byte[length];
        data.get(stringBytes);
        // find the first index of 0
        int index = 0;
        for (byte b : stringBytes) {
            if (0 == b)
                break;
            ++index;
        }
        return new String(Arrays.copyOf(stringBytes, index), Charset.forName("ascii"));
    }

    public static void writeTo(IDataBuffer data, int length, String value) {
        try {
            byte[] name = value.getBytes("ASCII");
            if (name.length < length) {
                data.put(name);
                for (int i = name.length; i < length; ++i) {
                    data.put((byte) 0);
                }
            } else {
                int end = length - 1;
                for (int i = 0; i < end; i++)
                    data.put(name[i]);
                data.put((byte) 0);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
