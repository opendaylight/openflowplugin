/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.Iterables;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.controller.sal.common.util.Arguments;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PacketOutConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(MeterConvertor.class);

    private PacketOutConvertor() {

    }

    // Get all the data for the PacketOut from the Yang/SAL-Layer

    /**
     * @param version openflow version
     * @param inputPacket input packet
     * @param datapathid  datapath id
     * @param xid tx id
     * @return PacketOutInput required by OF Library
     */
    public static PacketOutInput toPacketOutInput(final TransmitPacketInput inputPacket, final short version, final Long xid,
                                                  final BigInteger datapathid) {

        LOG.trace("toPacketOutInput for datapathId:{}, xid:{}", datapathid, xid);
        // Build Port ID from TransmitPacketInput.Ingress
        PortNumber inPortNr = null;
        Long bufferId = OFConstants.OFP_NO_BUFFER;
        Iterable<PathArgument> inArgs = null;
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        if (inputPacket.getIngress() != null) {
            inArgs = inputPacket.getIngress().getValue().getPathArguments();
        }
        if (inArgs != null && Iterables.size(inArgs) >= 3) {
            inPortNr = getPortNumber(Iterables.get(inArgs, 2), version);
        } else {
            // The packetOut originated from the controller
            inPortNr = new PortNumber(0xfffffffdL);
        }

        // Build Buffer ID to be NO_OFP_NO_BUFFER
        if (inputPacket.getBufferId() != null) {
            bufferId = inputPacket.getBufferId();
        }

        PortNumber outPort = null;
        NodeConnectorRef outRef = inputPacket.getEgress();
        Iterable<PathArgument> outArgs = outRef.getValue().getPathArguments();
        if (Iterables.size(outArgs) >= 3) {
            outPort = getPortNumber(Iterables.get(outArgs, 2), version);
        } else {
            // TODO : P4 search for some normal exception
            new Exception("PORT NR not exist in Egress");
        }

        List<Action> actions = null;
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> inputActions =
                inputPacket.getAction();
        if (inputActions != null) {
            actions = ActionConvertor.getActions(inputActions, version, datapathid, null);

        } else {
            actions = new ArrayList<>();
            // TODO VD P! wait for way to move Actions (e.g. augmentation)
            ActionBuilder aBuild = new ActionBuilder();

            OutputActionCaseBuilder outputActionCaseBuilder =
                    new OutputActionCaseBuilder();

            OutputActionBuilder outputActionBuilder =
                    new OutputActionBuilder();

            outputActionBuilder.setPort(outPort);
            outputActionBuilder.setMaxLength(OFConstants.OFPCML_NO_BUFFER);

            outputActionCaseBuilder.setOutputAction(outputActionBuilder.build());

            aBuild.setActionChoice(outputActionCaseBuilder.build());

            actions.add(aBuild.build());
        }

        builder.setAction(actions);
        builder.setData(inputPacket.getPayload());
        builder.setVersion(version);
        builder.setXid(xid);
        builder.setInPort(inPortNr);
        builder.setBufferId(bufferId);
        // --------------------------------------------------------

        return builder.build();
    }

    private static PortNumber getPortNumber(final PathArgument pathArgument, final Short ofVersion) {
        // FIXME VD P! find InstanceIdentifier helper
        InstanceIdentifier.IdentifiableItem<?, ?> item = Arguments.checkInstanceOf(pathArgument,
                InstanceIdentifier.IdentifiableItem.class);
        NodeConnectorKey key = Arguments.checkInstanceOf(item.getKey(), NodeConnectorKey.class);
        String[] split = key.getId().getValue().split(":");
        Long port = OpenflowPortsUtil.getPortFromLogicalName(OpenflowVersion.get(ofVersion), split[split.length - 1]);
        return new PortNumber(port);
    }
}
