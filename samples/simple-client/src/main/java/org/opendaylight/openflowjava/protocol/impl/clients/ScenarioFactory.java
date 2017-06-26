/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;

/**
 * Class for providing prepared handshake scenario
 *
 * @author michal.polkorab
 */
public final class ScenarioFactory {

    private ScenarioFactory() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Creates stack with handshake needed messages. XID of messages:
     * <ol>
     *   <li> hello sent - 00000001
     *   <li> hello waiting - 00000002
     *   <li> features request waiting - 00000003
     *   <li> features reply sent - 00000003
     * </ol>
     * @return stack filled with Handshake messages
     */
    public static Deque<ClientEvent> createHandshakeScenario() {
        Deque<ClientEvent> stack = new ArrayDeque<>();
        stack.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 00 00 08 00 00 00 01")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 00 00 08 00 00 00 02")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 05 00 08 00 00 00 03")));
        stack.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 06 00 20 00 00 00 03 "
                + "00 01 02 03 04 05 06 07 00 01 02 03 01 00 00 00 00 01 02 03 00 01 02 03")));
        return stack;
    }

    /**
     * Creates stack with handshake needed messages. XID of messages:
     * <ol>
     *   <li> hello sent - 00000001
     *   <li> hello waiting - 00000021
     *   <li> features request waiting - 00000002
     *   <li> features reply sent - 00000002
     * </ol>
     * @return stack filled with Handshake messages
     */
    public static Deque<ClientEvent> createHandshakeScenarioWithBarrier() {
        Deque<ClientEvent> stack = new ArrayDeque<>();
        stack.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 00 00 08 00 00 00 01")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 00 00 10 00 00 00 15 00 01 00 08 00 00 00 12"))); //Hello message 21
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 05 00 08 00 00 00 02")));
        stack.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 06 00 20 00 00 00 02 "
                + "00 01 02 03 04 05 06 07 00 01 02 03 01 00 00 00 00 01 02 03 00 01 02 03")));
        stack.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 14 00 08 00 00 00 00"))); //Barrier request
        stack.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 15 00 08 00 00 00 04"))); //Barrier reply
        return stack;
    }

    /**
     * Creates stack from XML file
     * @return stack filled with Handshake messages
     */
    public static Deque<ClientEvent> getScenarioFromXml(String scenarioName, String scenarioFile) throws JAXBException, SAXException, IOException {
        ScenarioService scenarioService = new ScenarioServiceImpl(scenarioFile);
        Deque<ClientEvent> stack = new ArrayDeque<>();
        for (Map.Entry<Integer, ClientEvent> clientEvent : scenarioService.getEventsFromScenario(scenarioService.unMarshallData(scenarioName)).entrySet()) {
            stack.addFirst(clientEvent.getValue());
        }
        return stack;
    }

    /**
     * Creates stack with handshake needed messages. XID of messages:
     * <ol>
     *   <li> hello sent - 00000001
     *   <li> hello waiting - 00000002
     *   <li> features request waiting - 00000003
     *   <li> features reply sent - 00000003
     * </ol>
     * @param auxiliaryId auxiliaryId wanted in featuresReply message
     * @return stack filled with Handshake messages (featuresReply with auxiliaryId set)
     */
    public static Deque<ClientEvent> createHandshakeScenarioWithAuxiliaryId(byte auxiliaryId) {
        Deque<ClientEvent> queue = new ArrayDeque<>();
        queue.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 00 00 08 00 00 00 01")));
        queue.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 00 00 08 00 00 00 02")));
        queue.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 05 00 08 00 00 00 03")));
        queue.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 06 00 20 00 00 00 03 "
                + "00 01 02 03 04 05 06 07 00 01 02 03 01 " + String.format("%02x ", auxiliaryId) + " 00 00 00 01 02 03 00 01 02 03")));
        return queue;
    }

}
