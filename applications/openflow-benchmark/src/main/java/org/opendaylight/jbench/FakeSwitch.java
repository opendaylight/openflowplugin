/*
 * Copyright (c) 2015 Intel Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.jbench;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;

/**
 * This class represents the fakeswitches emulated by Jbench Program. It exposes methods that connects to the
 * controller, creates packet-ins and receives flow-mods.
 * @author Raksha Madhava Bangera
 *
 */
public class FakeSwitch {
    private Jbench jbench;
    private Socket socket;
    private int macAddress;
    private int dpid;
    private long xid;
    private long count;
    private DataOutputStream sendToController;
    private DataInputStream receiveFromController;
    private static final int BUFLEN = 256;
    private static final Logger LOG = LoggerFactory.getLogger("Jbench");

    /**
     * Constructor of FakeSwitch class
     * @param jbenchObj - reference to Jbench driver class
     */
    FakeSwitch(Jbench jbenchObj, int id) {
        jbench = jbenchObj;
        macAddress = 1;
        sendToController = null;
        receiveFromController = null;
        xid = 1;
        count = 0;
        dpid = id;
    }

    /**
     * This method connects to SdnController using its hostName a
     * @return Socket - socket connected to SdnController
     */
    public Socket connectToController() {
        //TODO: Handle connections to multiple SDN controllers
        SdnController[] controllerArray = jbench.getControllerArray();
        socket = null;
        try {
            socket = new Socket(controllerArray[0].getHost(), controllerArray[0].getPort());
            sendToController = new DataOutputStream(socket.getOutputStream());
            receiveFromController = new DataInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            LOG.error("Failure during controller host name resolution {}", e);
        } catch (IOException e) {
            LOG.error("IO failure while connecting to controller {}", e);
        }
        return socket;
    }

    public void sendHello() {
        byte[] bytes = new byte[BUFLEN];

        HelloInputBuilder helloBuilder = new HelloInputBuilder();
        helloBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        helloBuilder.setXid(xid++);
        HelloInput helloInput = helloBuilder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();

        OFSerializer<HelloInput> helloFactory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.
                OF13_VERSION_ID, HelloInput.class));
        helloFactory.serialize(helloInput, out);
        out.getBytes(0, bytes);
        if ( socket != null && sendToController != null) {
            try {
                sendToController.write(bytes);
                if ( jbench.getDebug() == 1) {
                    LOG.debug("OF1.3 Hello sent from switch dpid {}", dpid);
                }
            } catch (IOException e) {
                LOG.error("IO exception while sending HELLO message to controller {}", e);
            }
        }
    }

    public void getResponseFromController() {
        //TODO get response from controller and deserialize the message
        //Temporary dummy code
        count++;
    }

    public long getFlowModCount() {
        return count;
    }

    public void resetFlowModeCount() {
        count = 0;
    }
}
