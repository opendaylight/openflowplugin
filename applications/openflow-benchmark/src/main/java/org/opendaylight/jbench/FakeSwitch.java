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

import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
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
    private int bufferId;
    private long xid;
    private long count;
    private DataOutputStream sendToController;
    private DataInputStream receiveFromController;
    private static final int BUFLEN = 65536;
    private static final byte FEATURESREQUEST = 5;
    private static final byte ECHOREQUEST = 2;
    private static final byte MULTIPARTREQUEST = 18;
    private static final byte FLOWMOD = 14;
    private static final byte PACKETOUT = 13;
    private static final int XIDOFFSET = 4;
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
        dpid = id + 1;
        bufferId = 1;
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
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            sendToController = new DataOutputStream(socket.getOutputStream());
            receiveFromController = new DataInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            LOG.error("Failure during controller host name resolution {}", e);
        } catch (IOException e) {
            LOG.error("IO failure while connecting to controller {}", e);
        }
        return socket;
    }

    /**
     * This method constructs OF 1.3 Hello Message and sends it to the controller
     */
    public void sendHello() {
        byte[] bytes = new byte[EncodeConstants.OFHEADER_SIZE];

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
                    LOG.info("OF1.3 Hello sent from switch dpid {}", dpid);
                }
            } catch (IOException e) {
                LOG.error("IO exception while sending HELLO message to controller {}", e);
            } finally {
                out.release();
            }
        }
        if (out.refCnt() > 0) {
            out.release();
        }
    }

    /**
     * This method reads message received from Controller if available
     * and decodes the type of OF 1.3 message received
     * @return type of OF 1.3 message or -1 on error
     */
    public int getResponseFromController() {
        byte[] message = new byte[BUFLEN];
        if (receiveFromController != null) {
            try {
                if (receiveFromController.available() <= 0) {
                    return -1;
                }
                int readByteCount = receiveFromController.read(message, 0, BUFLEN);
                if (readByteCount == 0 || readByteCount == -1) {
                    if (jbench.getDebug() == 1) {
                        LOG.info("Error while reading from Controller {}", readByteCount);
                    }
                    return -1;
                }
                ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
                in.writeBytes(message);

                byte type = ofHeaderDecoder(in);
                if (type == FEATURESREQUEST) {
                    if ( jbench.getDebug() == 1) {
                        LOG.info("OF1.3 Features Request received at switch dpid {}", dpid);
                    }
                    long xid = in.getUnsignedInt(in.readerIndex() + XIDOFFSET);
                    makeFeaturesReply(xid);
                    in.release();
                    return FEATURESREQUEST;
                } else if (type == -1) {
                    in.release();
                    return -1;
                } else if (type == ECHOREQUEST) {
                    if ( jbench.getDebug() == 1) {
                        LOG.info("OF1.3 Echo Request received at switch dpid {}", dpid);
                    }
                    long xid = in.getUnsignedInt(in.readerIndex() + XIDOFFSET);
                    makeEchoReply(xid);
                    in.release();
                    return ECHOREQUEST;
                } else if (type == MULTIPARTREQUEST) {
                    if ( jbench.getDebug() == 1) {
                        LOG.info("OF1.3 MultiPart Request received at switch dpid {}", dpid);
                    }
                    long xid = in.getUnsignedInt(in.readerIndex() + XIDOFFSET);
                    makeMultiPartReply(xid);
                    in.release();
                    return type;
                } else if (type == FLOWMOD || type == PACKETOUT) {
                    if ( jbench.getDebug() == 1) {
                        LOG.info("OF1.3 Packet-Out/Flow-Mod received at switch dpid {}", dpid);
                    }
                    count++;
                    in.release();
                    return type;
                } else if (type != 0) {
                    in.release();
                    return 0;
                }
                in.readByte();
                DeserializerRegistry deserializerRegistry = new DeserializerRegistryImpl();
                deserializerRegistry.init();

                DeserializationFactory factory = new DeserializationFactory();
                factory.setRegistry(deserializerRegistry);
                DataObject reply = factory.deserialize(in, EncodeConstants.OF13_VERSION_ID);
                in.release();
                if ( reply instanceof HelloMessage ) {
                    if ( jbench.getDebug() == 1) {
                        LOG.info("OF1.3 Hello reply received at switch dpid {}", dpid);
                    }
                } else {
                    if ( jbench.getDebug() == 1) {
                        LOG.info("Unknown message received at switch dpid {}", dpid);
                    }
                }
            } catch (IOException e) {
                LOG.error("IO exception while receiving message from controller {}", e);
            }
        }
        return 0;
    }

    /**
     * This method constructs packet-in messages and sends to controller
     */
    public void makePacketIns() {
        byte[] bytes = new byte[124];
        byte[] payload1 = new byte[] {
            (byte)0x97,0x0a,0x00,0x52,0x00,0x00,0x00,0x00 };
        byte[] payload2 = new byte[] {
            0x00,0x40,0x00,0x01,0x00,0x00,(byte)0x80,0x00,0x00,0x00,
            0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x02,0x08,0x00,0x45,
            0x00,0x00,0x32,0x00,0x00,0x00,0x00,0x40,(byte)0xff,(byte)0xf7,0x2c,
            (byte)0xc0,(byte)0xa8,0x00,0x28,(byte)0xc0,(byte)0xa8,0x01,0x28,0x7a,0x18,0x58,
            0x6b,0x11,0x08,(byte)0x97,(byte)0xf5,0x19,(byte)0xe2,0x65,0x7e,0x07,(byte)0xcc,
            0x31,(byte)0xc3,0x11,(byte)0xc7,(byte)0xc4,0x0c,(byte)0x8b,(byte)0x95,0x51,0x51,0x33,
            0x54,0x51,(byte)0xd5,0x00,0x36};
        byte[] ofpayload = new byte[] {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x0c,
            (byte)0x80, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        out.writeByte(EncodeConstants.OF13_VERSION_ID);
        out.writeByte(10);
        out.writeShort(124);
        out.writeInt((int) xid);
        xid++;
        out.writeInt(bufferId);
        bufferId++;
        out.writeShort(42);
        out.writeBytes(ofpayload);
        out.writeBytes(payload1);
        out.writeInt(macAddress);
        macAddress = (macAddress + 1) % jbench.getNumberOfMacAddresses();;
        out.writeBytes(payload2);
        out.getBytes(0, bytes);
        out.release();
        if ( socket != null && sendToController != null) {
            try {
                sendToController.write(bytes);
                if ( jbench.getDebug() == 1) {
                    LOG.info("OF1.3 Packet-In sent from switch dpid {}", dpid);
                }
            } catch (IOException e) {
                LOG.error("IO exception while sending Packet-Ins message to controller {}", e);
            }
        }
    }

    /**
     * This method constructs Features reply message for Features
     * request from controller
     * @param xid - Transaction ID of Features request message
     */
    public void makeFeaturesReply(long xid) {
        byte[] bytes = new byte[32];
        byte[] payload = new byte[] {
            0x00, 0x00, 0x01, 0x00, (byte)0xfe, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x4f,
            0x00, 0x00, 0x00, 0x00
        };
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        out.writeByte(EncodeConstants.OF13_VERSION_ID);
        out.writeByte(6);
        out.writeShort(32);
        out.writeInt((int) xid);
        out.writeInt(0);
        out.writeInt(dpid);
        out.writeBytes(payload);
        out.getBytes(0, bytes);
        out.release();
        if ( socket != null && sendToController != null) {
            try {
                sendToController.write(bytes);
                if ( jbench.getDebug() == 1) {
                    LOG.info("OF1.3 Features reply sent from switch dpid {}", dpid);
                }
            } catch (IOException e) {
                LOG.error("IO exception while sending Features reply message to controller {}", e);
            }
        }
    }

    /**
     * This method constructs OF 1.3 Echo reply for echo
     * request message from controller
     * @param xid - Transaction ID of Echo request message
     */
    public void makeEchoReply(long xid) {
        byte[] bytes = new byte[EncodeConstants.OFHEADER_SIZE];
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        out.writeByte(EncodeConstants.OF13_VERSION_ID);
        out.writeByte(3);
        out.writeShort(EncodeConstants.OFHEADER_SIZE);
        out.writeInt((int)xid);
        out.release();
        if ( socket != null && sendToController != null) {
            try {
                sendToController.write(bytes);
                if ( jbench.getDebug() == 1) {
                    LOG.info("OF1.3 Echo reply sent from switch dpid {}", dpid);
                }
            } catch (IOException e) {
                LOG.error("IO exception while sending Echo reply message to controller {}", e);
            }
        }
    }

    /**
     * This method constructs OF 1.3 Multi-part reply for Multi-part request
     * from controller
     * @param xid - Transaction ID of Multi-part request message
     */
    public void makeMultiPartReply(long xid) {
        byte[] bytes = new byte[208];
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        byte[] payload = new byte[] {
            0x00, 0x0d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x00, (byte)0xd6, (byte)0x98, 0x1c, 0x72, 0x7f, (byte)0xb6, 0x00, 0x00, 0x73, 0x31, 0x2d,
            0x65, 0x74, 0x68, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x98, (byte)0x96, (byte)0x80, 0x00,
            0x00, 0x00, 0x00, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, 0x00, 0x00, 0x00, 0x00,
            0x1e, (byte)0xd3, 0x20, (byte)0x84, (byte)0xe9, 0x4a, 0x00, 0x00, 0x73, 0x31, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, (byte)0xe6, (byte)0xf2, 0x60, (byte)0xe2, (byte)0x8e,
            0x35, 0x00, 0x00, 0x73, 0x31, 0x2d, 0x65, 0x74, 0x68, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
            0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x98,
            (byte)0x96, (byte)0x80, 0x00, 0x00, 0x00, 0x00
            };
        out.writeByte(EncodeConstants.OF13_VERSION_ID);
        out.writeByte(19);
        out.writeShort(208);
        out.writeInt((int)xid);
        out.writeBytes(payload);
        out.getBytes(0, bytes);
        out.release();
        if ( socket != null && sendToController != null) {
            try {
                sendToController.write(bytes);
                if ( jbench.getDebug() == 1) {
                    LOG.info("OF1.3 Multipart reply sent from switch dpid {}", dpid);
                }
            } catch (IOException e) {
                LOG.error("IO exception while sending Multipart reply message to controller {}", e);
            }
        }
    }

    /**
     * @param bb - ByteBuf holding the received message from controller
     * @return - Type of OF 1.3 message or -1 if bb is not OF1.3 message
     */
    public byte ofHeaderDecoder(ByteBuf bb) {
        if (bb.readableBytes() < EncodeConstants.OFHEADER_SIZE) {
            if ( jbench.getDebug() == 1) {
                LOG.info("Too few data for OF header {}", bb.readableBytes());
            }
            return -1;
        }

        int length = bb.getUnsignedShort(bb.readerIndex() + EncodeConstants.OFHEADER_LENGTH_INDEX);
        if (bb.readableBytes() < length) {
            if (jbench.getDebug() == 1) {
                LOG.info("Too few data for OF message {} < {}", bb.readableBytes(), length);
            }
            return -1;
        }
        if (jbench.getDebug() == 1) {
            LOG.info("OF Protocol message received, type {}", bb.getByte(bb.readerIndex() + 1));
        }
        if ( bb.getByte(bb.readerIndex() + 1) == 0 && bb.readableBytes() > length) {
            int type = bb.getByte(bb.readerIndex() + length + 1);
            if ( type == 5) {
                bb.skipBytes(length);
                return 5;
            }
        }
        return bb.getByte(bb.readerIndex() + 1);
    }

    /**
     * @return - Number of flow-mods received at the FakeSwitch
     */
    public long getFlowModCount() {
        return count;
    }

    /**
     * Resets the flow-mod count
     */
    public void resetFlowModeCount() {
        count = 0;
        macAddress = 1;
        xid = 1;
        bufferId = 1;
    }

    /**
     * This method closes the input and output stream of the socket
     * and closes the FakeSwitch socket to the controller.
     */
    public void closeSocket() {
        if (socket != null) {
            try {
                if (sendToController != null) {
                    sendToController.close();
                }
                if (receiveFromController != null) {
                    receiveFromController.close();
                }
                socket.close();
            } catch (IOException e) {
                LOG.info("Error while closing socket {}", e);
            }
        }
    }
}
