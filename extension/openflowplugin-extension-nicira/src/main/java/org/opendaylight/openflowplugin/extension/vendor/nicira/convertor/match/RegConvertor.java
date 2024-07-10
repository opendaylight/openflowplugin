/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.RegCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.RegCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.reg.grouping.RegValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg0Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg5Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg6Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg7Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.reg.grouping.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.reg.grouping.NxmNxRegBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert to/from SAL flow model to openflowjava model for NxmNxReg.
 *
 * @author msunal
 */
public class RegConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {

    private static final Logger LOG = LoggerFactory.getLogger(RegConvertor.class);

    @SuppressWarnings("unchecked")
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        NxmNxRegBuilder nxRegBuilder = new NxmNxRegBuilder();
        if (!(input.getOxmMatchField()
            instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg)) {
            String msg = input.getOxmMatchField()
                    + " does not implement "
                    + org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg.class;
            LOG.warn("Warning {}",msg);
            throw new IllegalStateException(msg);
        }
        nxRegBuilder.setReg((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg)
            input.getOxmMatchField());
        RegCaseValue regCaseValue = (RegCaseValue) input.getMatchEntryValue();
        nxRegBuilder.setValue(regCaseValue.getRegValues().getValue());

        if (input.getHasMask()) {
            nxRegBuilder.setMask(regCaseValue.getRegValues().getMask());
        }

        return resolveAugmentation(nxRegBuilder.build(), path, resolveRegKey(input.getOxmMatchField()));
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        final var matchGrouping = MatchUtil.REG_RESOLVER.findExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        final var nxmNxReg = matchGrouping.orElseThrow().getNxmNxReg();

        return MatchUtil.createDefaultMatchEntryBuilder(nxmNxReg.getReg(), Nxm1Class.VALUE,
            new RegCaseValueBuilder()
                .setRegValues(new RegValuesBuilder().setValue(nxmNxReg.getValue()).setMask(nxmNxReg.getMask()).build())
                .build())
            .setHasMask(nxmNxReg.getMask() != null)
            .build();
    }

    private static ExtensionKey resolveRegKey(final MatchField oxmMatchField) {
        // FIXME: Use direct field value equalitity
        if (NiciraMatchCodecs.REG0_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg0Key.VALUE;
        }
        if (NiciraMatchCodecs.REG1_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg1Key.VALUE;
        }
        if (NiciraMatchCodecs.REG2_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg2Key.VALUE;
        }
        if (NiciraMatchCodecs.REG3_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg3Key.VALUE;
        }
        if (NiciraMatchCodecs.REG4_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg4Key.VALUE;
        }
        if (NiciraMatchCodecs.REG5_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg5Key.VALUE;
        }
        if (NiciraMatchCodecs.REG6_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg6Key.VALUE;
        }
        if (NiciraMatchCodecs.REG7_CODEC.getNxmField().equals(oxmMatchField)) {
            return NxmNxReg7Key.VALUE;
        }
        throw new CodecPreconditionException("There is no key for " + oxmMatchField);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(final NxmNxReg nxmNxReg,
            final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                new NxAugMatchNodesNodeTableFlowBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                new NxAugMatchRpcGetFlowStatsBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                new NxAugMatchNotifPacketInBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                new NxAugMatchPacketInMessageBuilder().setNxmNxReg(nxmNxReg).build(), key);
        };
    }
}
