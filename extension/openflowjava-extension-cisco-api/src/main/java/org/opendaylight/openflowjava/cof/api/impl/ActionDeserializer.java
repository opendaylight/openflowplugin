/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.api.impl;

import java.util.Map;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.cof.api.CiscoActionDeserializerKey;
import org.opendaylight.openflowjava.cof.api.CiscoConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * bulk decoder relying on registered vendor action decoders 
 */
public class ActionDeserializer implements OFDeserializer<Action> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionDeserializer.class);

    private final Map<CiscoActionDeserializerKey, OFDeserializer<Action>> actionDeserializers;

    private final short version;

    /**
     * @param version protocol wire version
     * @param actionDeserializers 
     */
    public ActionDeserializer(short version, Map<CiscoActionDeserializerKey, OFDeserializer<Action>> actionDeserializers) {
        this.version = version;
        this.actionDeserializers = actionDeserializers;
    }

    @Override
    public Action deserialize(ByteBuf message) {
        int startPossition = message.readerIndex();
        // size of experimenter type
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        // size of length
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        long experimenterId = message.readUnsignedInt();
        if (CiscoConstants.COF_VENDOR_ID != experimenterId) {
            throw new IllegalStateException("Experimenter ID is not Cisco vendor id but is " + experimenterId);
        }
        int subtype = message.readUnsignedShort();
        CiscoActionDeserializerKey key = new CiscoActionDeserializerKey(version, subtype);
        OFDeserializer<Action> actionDeserializer = actionDeserializers.get(key);
        if (actionDeserializer == null) {
            LOG.info("No deserializer was found for key {}", key);
            return null;
        }
        message.readerIndex(startPossition);
        return actionDeserializer.deserialize(message);
    }

}
