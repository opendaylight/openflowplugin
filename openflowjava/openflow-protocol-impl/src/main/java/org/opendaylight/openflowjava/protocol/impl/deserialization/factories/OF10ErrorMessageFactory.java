/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.BadActionCodeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.BadRequestCodeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ErrorTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFailedCodeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloFailedCodeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortModFailedCodeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueOpFailedCodeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;

/**
 * Translates Error messages (OpenFlow v1.0).
 *
 * @author michal.polkorab
 */
public class OF10ErrorMessageFactory implements OFDeserializer<ErrorMessage> {

    private static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    private static final String UNKNOWN_CODE = "UNKNOWN_CODE";

    @Override
    public ErrorMessage deserialize(ByteBuf rawMessage) {
        ErrorMessageBuilder builder = new ErrorMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        int type = rawMessage.readUnsignedShort();
        ErrorTypeV10 errorType = ErrorTypeV10.forValue(type);
        decodeType(builder, errorType, type);
        decodeCode(rawMessage, builder, errorType);
        int remainingBytes = rawMessage.readableBytes();
        if (remainingBytes > 0) {
            byte[] data = new byte[remainingBytes];
            rawMessage.readBytes(data);
            builder.setData(data);
        }
        return builder.build();
    }

    private static void decodeType(ErrorMessageBuilder builder, ErrorTypeV10 type, int readValue) {
        if (type != null) {
            builder.setType(type.getIntValue());
            builder.setTypeString(type.name());
        } else {
            builder.setType(readValue);
            builder.setTypeString(UNKNOWN_TYPE);
        }
    }

    private static void decodeCode(ByteBuf rawMessage, ErrorMessageBuilder builder,
            ErrorTypeV10 type) {
        int code = rawMessage.readUnsignedShort();
        if (type != null) {
            switch (type) {
                case HELLOFAILED: {
                    HelloFailedCodeV10 errorCode = HelloFailedCodeV10.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case BADREQUEST: {
                    BadRequestCodeV10 errorCode = BadRequestCodeV10.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case BADACTION: {
                    BadActionCodeV10 errorCode = BadActionCodeV10.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case FLOWMODFAILED: {
                    FlowModFailedCodeV10 errorCode = FlowModFailedCodeV10.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case PORTMODFAILED: {
                    PortModFailedCodeV10 errorCode = PortModFailedCodeV10.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case QUEUEOPFAILED: {
                    QueueOpFailedCodeV10 errorCode = QueueOpFailedCodeV10.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                default:
                    setUnknownCode(builder, code);
                    break;
            }
        } else {
            setUnknownCode(builder, code);
        }
    }

    private static void setUnknownCode(ErrorMessageBuilder builder, int readValue) {
        builder.setCode(readValue);
        builder.setCodeString(UNKNOWN_CODE);
    }

    private static void setCode(ErrorMessageBuilder builder, int code, String codeString) {
        builder.setCode(code);
        builder.setCodeString(codeString);
    }

}
