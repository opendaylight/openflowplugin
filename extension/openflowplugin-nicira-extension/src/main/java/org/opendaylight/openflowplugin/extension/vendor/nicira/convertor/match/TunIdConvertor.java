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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.NxmNxTunId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjAugNxMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.OfjNxmNxMatchTunIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.of.extension.nicira.match.rev140421.ofj.nxm.nx.match.tun.id.grouping.TunIdValues;
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
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class TunIdConvertor implements ConvertorToOFJava<MatchEntries>, ConvertorFromOFJava<MatchEntries, MatchPath> {

    private final static Logger LOG = LoggerFactory.getLogger(TunIdConvertor.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert
     * (org.opendaylight.yangtools.yang.binding.DataContainer,
     * org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @Override
    public <T extends Augmentation<Extension>> Entry<Class<T>, T> convert(MatchEntries input, MatchPath path) {
        TunIdValues tunIdValues = input.getAugmentation(OfjAugNxMatch.class).getTunIdValues();
        return resolveAugmentations(tunIdValues, path);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends Augmentation<Extension>> Entry<Class<T>, T> resolveAugmentations(TunIdValues tunIdValues,
            MatchPath path) {
        switch (path) {
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
            return new SimpleEntry(NxAugMatchNotifUpdateFlowStats.class, new NxAugMatchNotifUpdateFlowStatsBuilder()
                    .setTunIdValues(tunIdValues).build());
        case PACKETRECEIVED_MATCH:
            return new SimpleEntry(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder().setTunIdValues(
                    tunIdValues).build());
        case SWITCHFLOWREMOVED_MATCH:
            return new SimpleEntry(NxAugMatchNotifSwitchFlowRemoved.class,
                    new NxAugMatchNotifSwitchFlowRemovedBuilder().setTunIdValues(tunIdValues).build());
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
        Optional<OfjNxmNxMatchTunIdGrouping> nxMatchTunIdGrouping = MatchUtil.getNxmNxMatchTunIdGrouping(extension);
        if (!nxMatchTunIdGrouping.isPresent()) {
            LOG.error("Extension {} does not contain any known augmentation. {}", extension.getClass(), extension);
            return null;
        }
        OfjAugNxMatchBuilder augNxMatchBuilder = new OfjAugNxMatchBuilder().setTunIdValues(nxMatchTunIdGrouping.get()
                .getTunIdValues());
        MatchEntriesBuilder matchEntriesBuilder = MatchUtil.createNiciraMatchEntriesBuilder(Nxm1Class.class,
                NxmNxTunId.class, false);
        matchEntriesBuilder.addAugmentation(OfjAugNxMatch.class, augNxMatchBuilder.build());
        return matchEntriesBuilder.build();
    }

}
