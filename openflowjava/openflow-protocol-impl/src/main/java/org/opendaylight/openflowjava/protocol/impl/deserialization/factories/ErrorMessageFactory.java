/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.BadActionCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.BadInstructionCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.BadMatchCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.BadRequestCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupModFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortModFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueOpFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.RoleRequestFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableModFailedCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;

/**
 * Translates Error messages.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class ErrorMessageFactory implements OFDeserializer<ErrorMessage>,
        DeserializerRegistryInjector {

    private static final String UNKNOWN_CODE = "UNKNOWN_CODE";
    private static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    private DeserializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public ErrorMessage deserialize(ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        int startIndex = rawMessage.readerIndex();
        ErrorMessageBuilder builder = new ErrorMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage));
        int type = rawMessage.readUnsignedShort();
        ErrorType errorType = ErrorType.forValue(type);
        if (ErrorType.EXPERIMENTER.equals(errorType)) {
            OFDeserializer<ErrorMessage> deserializer = registry.getDeserializer(
                    ExperimenterDeserializerKeyFactory.createExperimenterErrorDeserializerKey(
                            EncodeConstants.OF13_VERSION_ID, rawMessage.getUnsignedInt(
                                    rawMessage.readerIndex() + EncodeConstants.SIZE_OF_SHORT_IN_BYTES)));
            rawMessage.readerIndex(startIndex);
            return deserializer.deserialize(rawMessage);
        }
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

    private static void decodeType(ErrorMessageBuilder builder, ErrorType type, int readValue) {
        if (type != null) {
            builder.setType(type.getIntValue());
            builder.setTypeString(type.name());
        } else {
            builder.setType(readValue);
            builder.setTypeString(UNKNOWN_TYPE);
        }
    }

    private static void decodeCode(ByteBuf rawMessage, ErrorMessageBuilder builder,
            ErrorType type) {
        int code = rawMessage.readUnsignedShort();
        if (type != null) {
            switch (type) {
                case HELLOFAILED: {
                    HelloFailedCode errorCode = HelloFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case BADREQUEST: {
                    BadRequestCode errorCode = BadRequestCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case BADACTION: {
                    BadActionCode errorCode = BadActionCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case BADINSTRUCTION: {
                    BadInstructionCode errorCode = BadInstructionCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case BADMATCH: {
                    BadMatchCode errorCode = BadMatchCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case FLOWMODFAILED: {
                    FlowModFailedCode errorCode = FlowModFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case GROUPMODFAILED: {
                    GroupModFailedCode errorCode = GroupModFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case PORTMODFAILED: {
                    PortModFailedCode errorCode = PortModFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case TABLEMODFAILED: {
                    TableModFailedCode errorCode = TableModFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case QUEUEOPFAILED: {
                    QueueOpFailedCode errorCode = QueueOpFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case SWITCHCONFIGFAILED: {
                    SwitchConfigFailedCode errorCode = SwitchConfigFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case ROLEREQUESTFAILED: {
                    RoleRequestFailedCode errorCode = RoleRequestFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case METERMODFAILED: {
                    MeterModFailedCode errorCode = MeterModFailedCode.forValue(code);
                    if (errorCode != null) {
                        setCode(builder, errorCode.getIntValue(), errorCode.name());
                    } else {
                        setUnknownCode(builder, code);
                    }
                    break;
                }
                case TABLEFEATURESFAILED: {
                    TableFeaturesFailedCode errorCode = TableFeaturesFailedCode.forValue(code);
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

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        this.registry = deserializerRegistry;
    }

}
