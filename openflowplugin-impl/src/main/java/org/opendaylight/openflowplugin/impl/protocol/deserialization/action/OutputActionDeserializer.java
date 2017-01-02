/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;

import io.netty.buffer.ByteBuf;

public class OutputActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf message) {
        processHeader(message);

        final String portNumberAsString = OpenflowPortsUtil
            .portNumberToString(OpenflowPortsUtil
                    .getProtocolAgnosticPort(OpenflowVersion.OF13, message.readUnsignedInt()));

        final int maxLength = message.readUnsignedShort();
        message.skipBytes(ActionConstants.OUTPUT_PADDING);

        return new OutputActionCaseBuilder()
            .setOutputAction(new OutputActionBuilder()
                    .setOutputNodeConnector(new Uri(portNumberAsString))
                    .setMaxLength(maxLength)
                    .build())
            .build();
    }

    @Override
    public Action deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new OutputActionCaseBuilder().build();
    }

}
