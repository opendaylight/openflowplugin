/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.it;

import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.openflowjava.protocol.impl.clients.ClientEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.SendEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.SleepEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.WaitForMessageEvent;
import org.opendaylight.openflowjava.util.ByteBufUtils;

/**
 * provisioning of most common scenarios used by testing of integration between OFLibrary, OFPlugin and MD-SAL
 */
public abstract class ScenarioFactory {

    /** default sleep time [ms] - at scenario end */
    public static final int DEFAULT_SCENARIO_SLEEP = 2000;
    /** version bitmap hex-string containing version 1.3 */
    public static final String VERSION_BITMAP_13 = "00 01 00 08 00 00 00 10";
    /** version bitmap hex-string containing versions: 1.0 + 1.3 */
    public static final String VERSION_BITMAP_10_13 = "00 01 00 08 00 00 00 12";

    /**
     * Creates stack with handshake needed messages.
     * <ol> XID of messages:
     *   <li> hello sent - 00000001
     *   <li> hello waiting - 00000015
     *   <li> featuresrequest waiting - 00000002
     *   <li> featuresreply sent - 00000002
     * </ol>
     * @param switchVersionBitmap
     * @param auxId
     * @param pluginVersionBitmap
     * @param addSleep if true - then add final sleep {@link #DEFAULT_SCENARIO_SLEEP}
     * @return stack filled with Handshake messages
     */
    public static Deque<ClientEvent> createHandshakeScenarioVBM(
            String switchVersionBitmap, short auxId, String pluginVersionBitmap, boolean addSleep) {
        Deque<ClientEvent> stack = new ArrayDeque<>();

        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("04 00 00 10 00 00 00 01 "
                        + switchVersionBitmap)));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 00 00 10 00 00 00 15 "
                        + pluginVersionBitmap)));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 05 00 08 00 00 00 02")));
        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("04 06 00 20 00 00 00 02 "
                        + "00 01 02 03 04 05 06 07 " + "00 01 02 03 01 "
                        + Integer.toHexString(auxId)
                        + " 00 00 00 01 02 03 00 01 02 03")));

        if (addSleep) {
            addSleep(stack);
        }

        return stack;
    }

    /**
     * @param stack
     * @param addSleep if true - then add final sleep {@link #DEFAULT_SCENARIO_SLEEP}
     * @return
     */
    public static Deque<ClientEvent> appendPostHandshakeScenario(Deque<ClientEvent> stack, boolean addSleep) {
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 12 00 10 00 00 00 01 "+
                                  "00 0d 00 00 00 00 00 00"  )));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 12 00 10 00 00 00 02 "+
                                  "00 00 00 00 00 00 00 00"  )));

        if (addSleep) {
            addSleep(stack);
        }

        return stack;
    }

    /**
     * @param stack
     */
    private static void addSleep(Deque<ClientEvent> stack) {
        stack.addFirst(new SleepEvent(DEFAULT_SCENARIO_SLEEP));
    }

    /**
     * @param auxId
     * @param pluginVersionBitmap
     * @return handshake scenario without switch version bitmap
     */
    public static Deque<ClientEvent> createHandshakeScenario(short auxId,
            String pluginVersionBitmap) {
        Deque<ClientEvent> stack = new ArrayDeque<>();

        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("04 00 00 08 00 00 00 01")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 00 00 10 00 00 00 15 "
                        + pluginVersionBitmap)));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 05 00 08 00 00 00 02")));
        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("04 06 00 20 00 00 00 02 "
                        + "00 01 02 03 04 05 06 07 " + "00 01 02 03 01 "
                        + Integer.toHexString(auxId)
                        + " 00 00 00 01 02 03 00 01 02 03")));
        addSleep(stack);
        return stack;
    }

    /**
     * Attempt to simulate the MLX running 1.0 talking to ODL
     *
     * @return handshake scenario without switch version bitmap
     */
    public static Deque<ClientEvent> createHandshakeScenarioNoVBM_OF10_TwoHello() {
        Deque<ClientEvent> stack = new ArrayDeque<>();

        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("01 00 00 08 00 00 01 67")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                        .hexStringToBytes("04 00 00 10 00 00 00 15 00 01 00 08 00 00 00 12")));
        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("01 01 00 0c 00 00 00 15 00 00 00 00")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("01 00 00 08 00 00 01 68")));
        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("01 00 00 08 00 00 01 68")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("01 05 00 08 00 00 01 69")));

        stack.addFirst(new SendEvent(
                ByteBufUtils
                        .hexStringToBytes("01 06 00 80 00 00 01 69 cc 4e 24 1c 4a 00 00 00"
                                + " 00 00 01 00 01 00 00 00 00 00 00 07 00 00 01 0f"
                                + " 00 61 cc 4e 24 1c 4a 60 65 74 68 33 2f 31 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 11 00 00 00 01"
                                + " 00 00 01 40 00 00 01 40 00 00 01 40 00 00 01 40"
                                + " 00 62 cc 4e 24 1c 4a 61 65 74 68 33 2f 32 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 11 00 00 00 01"
                                + " 00 00 01 40 00 00 01 40 00 00 01 40 00 00 01 40")));

        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("01 10 00 0c 00 00 00 01 00 00 00 00")));

        stack.addFirst(new SendEvent(
                ByteBufUtils
                        .hexStringToBytes("01 11 04 2c 00 00 00 01 00 00 00 00 42 72 6f 63"
                                + " 61 64 65 20 43 6f 6d 6d 75 6e 69 63 61 74 69"
                                + " 6f 6e 73 2c 20 49 6e 63 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 4d 75 6c"
                                + " 74 69 2d 53 65 72 76 69 63 65 20 49 72 6f 6e"
                                + " 77 61 72 65 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 4e 49"
                                + " 20 35 2e 37 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 4e"
                                + " 6f 6e 65 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 4e 6f 6e 65 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00")));

        stack.addFirst(new SleepEvent(5000));

        addSleep(stack);
        return stack;
    }

    /**
     * Attempt to simulate the MLX running 1.0 talking to ODL
     *
     * @return handshake scenario without switch version bitmap
     */
    public static Deque<ClientEvent> createHandshakeScenarioNOVBM_OF10_OneHello() {
        System.out.println("createHandshakeScenarioMininet");
        Deque<ClientEvent> stack = new ArrayDeque<>();

        stack.addFirst(new SendEvent(ByteBufUtils
                .hexStringToBytes("01 00 00 08 00 00 00 0d")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("04 00 00 10 00 00 00 15")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("01 00 00 08 00 00 00 0e")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes("01 05 00 08 00 00 00 0f")));

        stack.addFirst(new SendEvent(
                ByteBufUtils
                        .hexStringToBytes("01 01 00 14 00 00 00 0e 00 01 00 01 01 00 00 08 00 00 00 0e")));

        stack.addFirst(new SendEvent(
                ByteBufUtils
                        .hexStringToBytes(" 01 06 00 b0 00 00 00 0f 00 00 00 00 00 00 00 01 00 00 01 00 fe 00"
                                + " 00 00 00 00 00 c7 00 00 0f ff 00 01 fa 01 ff 57 86 aa 73 31 2d"
                                + " 65 74 68 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01"
                                + " 00 00 00 c0 00 00 00 00 00 00 00 00 00 00 00 00 00 02 c2 51 d8"
                                + " 24 38 97 73 31 2d 65 74 68 32 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 c0 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 ff fe ea bd f8 db 41 40 73 31 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                                + " 00 00 00 00 00 00 00")));
        addSleep(stack);
        return stack;
    }
}
