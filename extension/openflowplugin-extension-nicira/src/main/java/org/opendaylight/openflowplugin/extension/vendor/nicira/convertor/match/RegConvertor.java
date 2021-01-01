/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import java.util.Optional;
import org.opendaylight.openflowjava.nx.codec.match.NiciraMatchCodecs;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.reg.grouping.RegValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.RegCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.RegCaseValueBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxRegGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.reg.grouping.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.reg.grouping.NxmNxRegBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
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
        if (!org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg.class
                .isAssignableFrom(input.getOxmMatchField())) {
            String msg = input.getOxmMatchField()
                    + " does not implement "
                    + org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg.class;
            LOG.warn("Warning {}",msg);
            throw new IllegalStateException(msg);
        }
        nxRegBuilder.setReg((Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match
                .rev140421.NxmNxReg>) input.getOxmMatchField());
        RegCaseValue regCaseValue = (RegCaseValue) input.getMatchEntryValue();
        nxRegBuilder.setValue(regCaseValue.getRegValues().getValue());

        if (input.getHasMask()) {
            nxRegBuilder.setMask(regCaseValue.getRegValues().getMask());
        }

        return resolveAugmentation(nxRegBuilder.build(), path, resolveRegKey(input.getOxmMatchField()));
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        Optional<NxmNxRegGrouping> matchGrouping = MatchUtil.REG_RESOLVER.findExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        NxmNxReg nxmNxReg = matchGrouping.get().getNxmNxReg();
        RegValuesBuilder regValuesBuilder = new RegValuesBuilder()
            .setValue(nxmNxReg.getValue())
            .setMask(nxmNxReg.getMask());

        RegCaseValueBuilder regCaseValueBuilder = new RegCaseValueBuilder();
        regCaseValueBuilder.setRegValues(regValuesBuilder.build());
        return MatchUtil.createDefaultMatchEntryBuilder(nxmNxReg.getReg(),
                Nxm1Class.class,
                regCaseValueBuilder.build())
            .setHasMask(nxmNxReg.getMask() != null)
            .build();
    }

    private static Class<? extends ExtensionKey> resolveRegKey(final Class<? extends MatchField> oxmMatchField) {
        if (NiciraMatchCodecs.REG0_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg0Key.class;
        }
        if (NiciraMatchCodecs.REG1_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg1Key.class;
        }
        if (NiciraMatchCodecs.REG2_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg2Key.class;
        }
        if (NiciraMatchCodecs.REG3_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg3Key.class;
        }
        if (NiciraMatchCodecs.REG4_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg4Key.class;
        }
        if (NiciraMatchCodecs.REG5_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg5Key.class;
        }
        if (NiciraMatchCodecs.REG6_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg6Key.class;
        }
        if (NiciraMatchCodecs.REG7_CODEC.getNxmField().isAssignableFrom(oxmMatchField)) {
            return NxmNxReg7Key.class;
        }
        throw new CodecPreconditionException("There is no key for " + oxmMatchField);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(final NxmNxReg nxmNxReg,
            final MatchPath path, final Class<? extends ExtensionKey> key) {
        switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH:
                return new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                        new NxAugMatchNodesNodeTableFlowBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH:
                return new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                        new NxAugMatchRpcGetFlowStatsBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case PACKET_RECEIVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                        .setNxmNxReg(nxmNxReg).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                        new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxReg(nxmNxReg).build(), key);
            case PACKET_IN_MESSAGE_MATCH:
                return new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                        new NxAugMatchPacketInMessageBuilder().setNxmNxReg(nxmNxReg).build(), key);
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
