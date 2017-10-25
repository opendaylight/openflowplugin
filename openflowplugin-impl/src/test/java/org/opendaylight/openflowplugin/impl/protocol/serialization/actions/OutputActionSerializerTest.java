/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;

public class OutputActionSerializerTest extends AbstractActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int length = 10;
        final String port = OutputPortValues.LOCAL.toString();

        final Action action = new OutputActionCaseBuilder()
                .setOutputAction(new OutputActionBuilder()
                        .setOutputNodeConnector(new Uri("openflow:1:" + port))
                        .setMaxLength(length)
                        .build())
                .build();

        assertAction(action, out -> {
            assertEquals(out.readUnsignedInt(), BinContent.intToUnsignedLong(PortNumberValues.LOCAL.getIntValue()));
            assertEquals(out.readUnsignedShort(), length);
            out.skipBytes(ActionConstants.OUTPUT_PADDING);
        });
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return OutputActionCase.class;
    }

    @Override
    protected int getType() {
        return ActionConstants.OUTPUT_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.LARGER_ACTION_LENGTH;
    }

}
