/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf.deserializer;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdErrorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.OnfExperimenterErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates (ONF approved) experimenter error messages.
 */
public class OnfExperimenterErrorFactory implements OFDeserializer<ErrorMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(OnfExperimenterErrorFactory.class);
    private static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    private static final String UNKNOWN_CODE = "UNKNOWN_CODE";

    @Override
    public ErrorMessage deserialize(ByteBuf message) {
        ErrorMessageBuilder builder = new ErrorMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(message.readUnsignedInt());

        int type = message.readUnsignedShort();
        ErrorType errorType = ErrorType.forValue(type);
        if (errorType != null && errorType.equals(ErrorType.EXPERIMENTER)) {
            builder.setType(errorType.getIntValue());
            builder.setTypeString(errorType.getName());
        } else {
            LOG.warn("Deserializing other than {} error message with {}", ErrorType.EXPERIMENTER.getName(),
                    this.getClass().getCanonicalName());
            builder.setType(type);
            builder.setTypeString(UNKNOWN_TYPE);
        }

        int code = message.readUnsignedShort();
        OnfExperimenterErrorCode errorCode = OnfExperimenterErrorCode.forValue(code);
        if (errorCode != null) {
            builder.setCode(errorCode.getIntValue());
            builder.setCodeString(errorCode.getName());
        } else {
            builder.setCode(code);
            builder.setCodeString(UNKNOWN_CODE);
        }

        builder.addAugmentation(new ExperimenterIdErrorBuilder()
                .setExperimenter(new ExperimenterId(message.readUnsignedInt()))
                .build());

        if (message.readableBytes() > 0) {
            byte[] data = new byte[message.readableBytes()];
            message.readBytes(data);
            builder.setData(data);
        }
        return builder.build();
    }
}
