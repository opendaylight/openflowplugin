/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.ofj.nxm.nx.match.reg.grouping.RegValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxMatchRegGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.match.reg.grouping.NxmNxReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.match.reg.grouping.NxmNxRegBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class RegConvertor implements ConvertorToOFJava<MatchEntries>, ConvertorFromOFJava<MatchEntries, MatchPath> {

    private final static Logger LOG = LoggerFactory.getLogger(RegConvertor.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert
     * (org.opendaylight.yangtools.yang.binding.DataContainer,
     * org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntries input, MatchPath path) {
        NxmNxRegBuilder nxRegBuilder = new NxmNxRegBuilder();
        if (!org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg.class
                .isAssignableFrom(input.getOxmMatchField())) {
            String msg = input.getOxmMatchField()
                    + " does not implement "
                    + org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg.class;
            LOG.warn(msg);
            throw new IllegalStateException(msg);
        }
        nxRegBuilder
                .setReg((Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg>) input
                        .getOxmMatchField());
        nxRegBuilder.setRegValues(input.getAugmentation(OfjAugNxMatch.class).getRegValues());
        return resolveAugmentations(nxRegBuilder.build(), path);
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentations(NxmNxReg nxmNxReg,
            MatchPath path) {
        switch (path) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifUpdateFlowStats.class,
                    new NxAugMatchNotifUpdateFlowStatsBuilder().setNxmNxReg(nxmNxReg).build());
        case PACKETRECEIVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                    .setNxmNxReg(nxmNxReg).build());
        case SWITCHFLOWREMOVED_MATCH:
            return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                    new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxReg(nxmNxReg).build());
        default:
            throw new CodecPreconditionException(path);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava#convert
     * (org
     * .opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general
     * .rev140714.general.extension.grouping.Extension)
     */
    @Override
    public MatchEntries convert(Extension extension) {
        Optional<NxmNxMatchRegGrouping> matchGrouping = MatchUtil.regResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        NxmNxReg nxmNxReg = matchGrouping.get().getNxmNxReg();
        RegValuesBuilder regValuesBuilder = new RegValuesBuilder().setValue(nxmNxReg.getRegValues().getValue());
        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder().setRegValues(regValuesBuilder.build());
        return MatchUtil.createNiciraMatchEntries(Nxm1Class.class, nxmNxReg.getReg(), false, augNxMatchBuilder.build());
    }

}
