/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.XidConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.binding.KeyStep;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a MD-SAL packet out data into the OF library packet out input.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * XidConvertorData data = new XidConvertorData(version);
 * data.setDatapathId(datapathId);
 * data.setXid(xid);
 * Optional<PacketOutInput> ofPacketInput = convertorManager.convert(salPacket, data);
 * }
 * </pre>
 */
public class PacketOutConvertor extends Convertor<TransmitPacketInput, PacketOutInput, XidConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(PacketOutConvertor.class);
    private static final Set<Class<?>> TYPES = Collections.singleton(TransmitPacketInput.class);
    private static final PortNumber CONTROLLER_PORT = new PortNumber(Uint32.valueOf(0xfffffffdL).intern());

    /**
     * Create default empty meter mot input builder.
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty meter mod input builder
     */
    public static PacketOutInput defaultResult(final Uint8 version) {
        return new PacketOutInputBuilder().setVersion(version).build();
    }

    private static PortNumber getPortNumber(final DataObjectStep<?> step, final Uint8 ofVersion) {
        if (!(step instanceof KeyStep<?, ?> keyStep)) {
            throw new IllegalArgumentException("Unexpected path argument " + step);
        }

        // FIXME VD P! find InstanceIdentifier helper
        final var key = keyStep.key();
        if (key instanceof NodeConnectorKey nodeConnectorKey) {
            return new PortNumber(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.ofVersion(ofVersion), nodeConnectorKey.getId()));
        }
        throw new IllegalArgumentException("Unexpected key " + key);
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public PacketOutInput convert(final TransmitPacketInput source, final XidConvertorData data) {
        LOG.trace("toPacketOutInput for datapathId:{}, xid:{}", data.getDatapathId(), data.getXid());
        // Build Port ID from TransmitPacketInput.Ingress
        PortNumber inPortNr;
        Uint32 bufferId = OFConstants.OFP_NO_BUFFER;
        Iterable<DataObjectStep<?>> inArgs = null;

        if (source.getIngress() != null) {
            inArgs = source.getIngress().getValue().getPathArguments();
        }

        if (inArgs != null && Iterables.size(inArgs) >= 3) {
            inPortNr = getPortNumber(Iterables.get(inArgs, 2), data.getVersion());
        } else {
            // The packetOut originated from the controller
            inPortNr = CONTROLLER_PORT;
        }

        // Build Buffer ID to be NO_OFP_NO_BUFFER
        if (source.getBufferId() != null) {
            bufferId = source.getBufferId();
        }

        final PortNumber outPort;
        final var outArgs = source.getEgress().getValue().getPathArguments();
        if (Iterables.size(outArgs) >= 3) {
            outPort = getPortNumber(Iterables.get(outArgs, 2), data.getVersion());
        } else {
            // TODO : P4 search for some normal exception
            // new Exception("PORT NR not exist in Egress");
            LOG.error("PORT NR not exist in Egress");
            outPort = null;
        }

        final List<Action> actions;
        Map<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey,
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> inputActions =
                source.nonnullAction();

        if (!inputActions.isEmpty()) {
            final ActionConvertorData actionConvertorData = new ActionConvertorData(data.getVersion());
            actionConvertorData.setDatapathId(data.getDatapathId());

            final Optional<List<Action>> convertedActions = getConvertorExecutor().convert(
                    inputActions, actionConvertorData);

            actions = convertedActions.orElse(List.of());
        } else {
            // TODO VD P! wait for way to move Actions (e.g. augmentation)
            actions = List.of(new ActionBuilder()
                .setActionChoice(new OutputActionCaseBuilder()
                    .setOutputAction(new OutputActionBuilder()
                        .setPort(outPort)
                        .setMaxLength(OFConstants.OFPCML_NO_BUFFER)
                        .build())
                    .build())
                .build());
        }

        return new PacketOutInputBuilder()
            .setAction(actions)
            .setData(source.getPayload())
            .setVersion(data.getVersion())
            .setXid(data.getXid())
            .setInPort(inPortNr)
            .setBufferId(bufferId)
            .build();
    }
}
