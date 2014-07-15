/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.ofj.nxm.nx.match.reg.grouping.RegValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
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
    public <T extends Augmentation<Extension>> Entry<Class<T>, T> convert(MatchEntries input, MatchPath path) {
        NxmNxRegBuilder nxRegBuilder = new NxmNxRegBuilder();
        if (!org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg.class
                .isAssignableFrom(input.getOxmMatchField())) {
            String msg = input.getOxmMatchField()
                    + " does not implement "
                    + org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg.class;
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
        nxRegBuilder
                .setReg((Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxReg>) input
                        .getOxmMatchField());
        nxRegBuilder.setRegValues(input.getAugmentation(OfjAugNxMatch.class).getRegValues());
        return resolveAugmentations(nxRegBuilder.build(), path);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends Augmentation<Extension>> Entry<Class<T>, T> resolveAugmentations(NxmNxReg nxmNxReg,
            MatchPath path) {
        switch (path) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new SimpleEntry(NxAugMatchNotifUpdateFlowStats.class, new NxAugMatchNotifUpdateFlowStatsBuilder()
                    .setNxmNxReg(nxmNxReg).build());
        case PACKETRECEIVED_MATCH:
            return new SimpleEntry(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder().setNxmNxReg(
                    nxmNxReg).build());
        case SWITCHFLOWREMOVED_MATCH:
            return new SimpleEntry(NxAugMatchNotifSwitchFlowRemoved.class,
                    new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxReg(nxmNxReg).build());
        default:
            String msg = "Augmentation for path " + path + " is not implemented!";
            LOG.error(msg);
            throw new IllegalStateException(msg);
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
        Optional<NxmNxMatchRegGrouping> nxMatchRegGrouping = MatchUtil.getNxmNxMatchRegGrouping(extension);
        if (!nxMatchRegGrouping.isPresent()) {
            String msg = "Extension " + extension.getClass() + " does not contain any known augmentation.";
            LOG.error("{} : {}", extension.getClass(), extension);
            throw new IllegalStateException(msg);
        }
        NxmNxReg nxmNxReg = nxMatchRegGrouping.get().getNxmNxReg();
        RegValuesBuilder regValuesBuilder = new RegValuesBuilder();
        regValuesBuilder.setValue(nxmNxReg.getRegValues().getValue());
        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder().setRegValues(regValuesBuilder.build());
        MatchEntriesBuilder matchEntriesBuilder = MatchUtil.createNiciraMatchEntriesBuilder(Nxm1Class.class,
                nxmNxReg.getReg(), false);
        matchEntriesBuilder.addAugmentation(OfjAugNxMatch.class, augNxMatchBuilder.build());
        return matchEntriesBuilder.build();
    }

}
