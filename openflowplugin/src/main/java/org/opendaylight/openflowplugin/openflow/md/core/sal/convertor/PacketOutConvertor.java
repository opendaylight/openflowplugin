/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.sal.common.util.Arguments;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketOutConvertor {
    private static final Logger logger = LoggerFactory.getLogger(MeterConvertor.class);
    private static final String PREFIX_SEPARATOR = "/";

    private PacketOutConvertor() {

    }

    // Get all the data for the PacketOut from the Yang/SAL-Layer
    /**
     * @param version
     * @param Yang
     *            Data source
     * @return PacketOutInput required by OF Library
     */
    public static PacketOutInput toPacketOutInput(TransmitPacketInput inputPacket, short version, Long xid,
            BigInteger datapathid) {

        // Build Port ID from TransmitPacketInput.Ingress
        PortNumber inPortNr = null;
        Long bufferId = OFConstants.OFP_NO_BUFFER;
        List<Action> actions = new ArrayList<>();
        List<PathArgument> inArgs = null;
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        if (inputPacket.getIngress() != null) {
            inArgs = inputPacket.getIngress().getValue().getPath();
        }
        if (inArgs != null && inArgs.size() >= 3) {
            inPortNr = getPortNumber(inArgs.get(2));
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
        List<PathArgument> outArgs = outRef.getValue().getPath();
        if (outArgs.size() >= 3) {
            outPort = getPortNumber(outArgs.get(2));
        } else {
            new Exception("PORT NR not exist in Egress"); // TODO : P4 search
                                                          // for some normal
                                                          // exception
        }

        // TODO VD P! wait for way to move Actions (e.g. augmentation)

        // FIXME VD implementation for testing PacketIn (REMOVE IT)
        if (inputPacket.getAction() == null) {
            ActionBuilder aBuild = new ActionBuilder();
            aBuild.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
            PortActionBuilder paBuild = new PortActionBuilder();
            paBuild.setPort(outPort);
            aBuild.addAugmentation(PortAction.class, paBuild.build());
            MaxLengthActionBuilder mlBuild = new MaxLengthActionBuilder();
            mlBuild.setMaxLength(0xffff);
            aBuild.addAugmentation(MaxLengthAction.class, mlBuild.build());
            actions.add(aBuild.build());
            builder.setAction(actions);
        } else {
            builder.setAction(ActionConvertor.getActions(inputPacket.getAction(), version, datapathid));
        }

        builder.setData(inputPacket.getPayload());
        builder.setVersion(version);
        builder.setXid(xid);
        builder.setInPort(inPortNr);
        builder.setBufferId(bufferId);
        // --------------------------------------------------------

        return builder.build();
    }

    private static PortNumber getPortNumber(PathArgument pathArgument) {
        // FIXME VD P! find InstanceIdentifier helper
        InstanceIdentifier.IdentifiableItem item = Arguments.checkInstanceOf(pathArgument,
                InstanceIdentifier.IdentifiableItem.class);
        NodeConnectorKey key = Arguments.checkInstanceOf(item.getKey(), NodeConnectorKey.class);
        String[] split = key.getId().getValue().split(":");
        Long port = Long.decode(split[split.length - 1]);
        return new PortNumber(port);
    }
}
