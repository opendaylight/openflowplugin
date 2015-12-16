/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeAugMatchNotifUpdateFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfUdpDstRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeUdpDstRangeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjHpeOfMatchUdpDstRangeGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.udp.dst.range.grouping.UdpDstRangeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.udp.dst.range.grouping.UdpDstRangeValuesBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class UdpDstRangeConvertor extends HpeAbstractConvertor<OfjHpeOfMatchUdpDstRangeGrouping> {
    public UdpDstRangeConvertor() {
        super(HpeUdpDstRangeKey.class, HpeOfUdpDstRange.class, false, OfjHpeOfMatchUdpDstRangeGrouping.class);
    }

    @Override
    protected ExtensionAugment<? extends Augmentation<Extension>> convertFromOFJava(MatchPath path, OfjAugHpeMatch ofjAugHpeMatch) {
        UdpDstRangeValues udpDstRangeValues = ofjAugHpeMatch.getUdpDstRangeValues();
        switch (path) {
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifUpdateFlowStats.class,
                        new HpeAugMatchNotifUpdateFlowStatsBuilder().setUdpDstRangeValues(udpDstRangeValues).build(), this.key);
            case PACKETRECEIVED_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifPacketIn.class,
                        new HpeAugMatchNotifPacketInBuilder().setUdpDstRangeValues(udpDstRangeValues).build(), this.key);
            case SWITCHFLOWREMOVED_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifSwitchFlowRemoved.class,
                        new HpeAugMatchNotifSwitchFlowRemovedBuilder().setUdpDstRangeValues(udpDstRangeValues).build(), this.key);
            default:
                throw new IllegalArgumentException("path");
        }
    }

    @Override
    protected void convertToOFJava(Extension extension, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        OfjHpeOfMatchUdpDstRangeGrouping udpDstRangeGrouping = getExtension(extension);
        UdpDstRangeValues udpDstRangeValues = udpDstRangeGrouping.getUdpDstRangeValues();
        UdpDstRangeValuesBuilder udpDstRangeValuesBuilder = new UdpDstRangeValuesBuilder();
        udpDstRangeValuesBuilder.setBegin(udpDstRangeValues.getBegin());
        udpDstRangeValuesBuilder.setEnd(udpDstRangeValues.getEnd());
        ofjAugHpeMatchBuilder.setUdpDstRangeValues(udpDstRangeValuesBuilder.build());
    }
}
