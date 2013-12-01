package org.opendaylight.openflowplugin.droptest;

public class DropTestUtils {
    public static String macToString(byte[] mac) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            b.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }

        return b.toString();
    }
}
