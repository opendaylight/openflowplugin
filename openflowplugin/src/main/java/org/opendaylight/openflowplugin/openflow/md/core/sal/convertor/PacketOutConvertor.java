/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.controller.sal.common.util.Arguments;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.PacketOutConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
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
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a MD-SAL packet out data into the OF library packet out input.
 *
 * Example usage:
 * <pre>
 * {@code
 * PacketOutConvertorData data = new PacketOutConvertorData(version);
 * data.setDatapathId(datapathId);
 * data.setXid(xid);
 * Optional<PacketOutInput> ofPacketInput = convertorManager.convert(salPacket, data);
 * }
 * </pre>
 */
public class PacketOutConvertor extends Convertor<TransmitPacketInput, PacketOutInput, PacketOutConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(PacketOutConvertor.class);
    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(TransmitPacketInput.class);

    /**
     * Create default empty meter mot input builder.
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty meter mod input builder
     */
    public static PacketOutInput defaultResult(short version) {
        return new PacketOutInputBuilder()
                .setVersion(version)
                .build();
    }

    private static PortNumber getPortNumber(final PathArgument pathArgument, final Short ofVersion) {
        // FIXME VD P! find InstanceIdentifier helper
        InstanceIdentifier.IdentifiableItem<?, ?> item = Arguments.checkInstanceOf(pathArgument,
                InstanceIdentifier.IdentifiableItem.class);
        NodeConnectorKey key = Arguments.checkInstanceOf(item.getKey(), NodeConnectorKey.class);
        Long port = InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.get(ofVersion), key.getId());
        return new PortNumber(port);
    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public PacketOutInput convert(TransmitPacketInput source, PacketOutConvertorData data) {
        LOG.trace("toPacketOutInput for datapathId:{}, xid:{}", data.getDatapathId(), data.getXid());
        // Build Port ID from TransmitPacketInput.Ingress
        PortNumber inPortNr;
        Long bufferId = OFConstants.OFP_NO_BUFFER;
        Iterable<PathArgument> inArgs = null;
        PacketOutInputBuilder builder = new PacketOutInputBuilder();

        if (source.getIngress() != null) {
            inArgs = source.getIngress().getValue().getPathArguments();
        }

        if (inArgs != null && Iterables.size(inArgs) >= 3) {
            inPortNr = getPortNumber(Iterables.get(inArgs, 2), data.getVersion());
        } else {
            // The packetOut originated from the controller
            inPortNr = new PortNumber(0xfffffffdL);
        }

        // Build Buffer ID to be NO_OFP_NO_BUFFER
        if (source.getBufferId() != null) {
            bufferId = source.getBufferId();
        }

        PortNumber outPort = null;
        NodeConnectorRef outRef = source.getEgress();
        Iterable<PathArgument> outArgs = outRef.getValue().getPathArguments();

        if (Iterables.size(outArgs) >= 3) {
            outPort = getPortNumber(Iterables.get(outArgs, 2), data.getVersion());
        } else {
            // TODO : P4 search for some normal exception
            // new Exception("PORT NR not exist in Egress");
            LOG.error("PORT NR not exist in Egress");
        }

        List<Action> actions = new ArrayList<>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> inputActions = source.getAction();

        if (inputActions != null) {
            final ActionConvertorData actionConvertorData = new ActionConvertorData(data.getVersion());
            actionConvertorData.setDatapathId(data.getDatapathId());

            final Optional<List<Action>> convertedActions = getConvertorExecutor().convert(
                    inputActions, actionConvertorData);

            actions = convertedActions.orElse(Collections.emptyList());

        } else {
            // TODO VD P! wait for way to move Actions (e.g. augmentation)
            ActionBuilder aBuild = new ActionBuilder();
            OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
            OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
            outputActionBuilder.setPort(outPort);
            outputActionBuilder.setMaxLength(OFConstants.OFPCML_NO_BUFFER);
            outputActionCaseBuilder.setOutputAction(outputActionBuilder.build());
            aBuild.setActionChoice(outputActionCaseBuilder.build());
            actions.add(aBuild.build());
        }

        builder.setAction(actions);
        builder.setData(source.getPayload());
        builder.setVersion(data.getVersion());
        builder.setXid(data.getXid());
        builder.setInPort(inPortNr);
        builder.setBufferId(bufferId);

        return builder.build();
    }
}