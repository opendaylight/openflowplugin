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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfTcpSrcRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeTcpSrcRangeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjHpeOfMatchTcpSrcRangeGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.src.range.grouping.TcpSrcRangeValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.src.range.grouping.TcpSrcRangeValuesBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class TcpSrcRangeConvertor extends HpeAbstractConvertor<OfjHpeOfMatchTcpSrcRangeGrouping> {
    public TcpSrcRangeConvertor() {
        super(HpeTcpSrcRangeKey.class, HpeOfTcpSrcRange.class, false, OfjHpeOfMatchTcpSrcRangeGrouping.class);
    }

    @Override
    protected ExtensionAugment<? extends Augmentation<Extension>> convertFromOFJava(MatchPath path, OfjAugHpeMatch ofjAugHpeMatch) {
        TcpSrcRangeValues tcpSrcRangeValues = ofjAugHpeMatch.getTcpSrcRangeValues();
        switch (path) {
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifUpdateFlowStats.class,
                        new HpeAugMatchNotifUpdateFlowStatsBuilder().setTcpSrcRangeValues(tcpSrcRangeValues).build(), this.key);
            case PACKETRECEIVED_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifPacketIn.class,
                        new HpeAugMatchNotifPacketInBuilder().setTcpSrcRangeValues(tcpSrcRangeValues).build(), this.key);
            case SWITCHFLOWREMOVED_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifSwitchFlowRemoved.class,
                        new HpeAugMatchNotifSwitchFlowRemovedBuilder().setTcpSrcRangeValues(tcpSrcRangeValues).build(), this.key);
            default:
                throw new IllegalArgumentException("path");
        }
    }

    @Override
    protected void convertToOFJava(Extension extension, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        OfjHpeOfMatchTcpSrcRangeGrouping tcpSrcRangeGrouping = getExtension(extension);
        TcpSrcRangeValues tcpSrcRangeValues = tcpSrcRangeGrouping.getTcpSrcRangeValues();
        TcpSrcRangeValuesBuilder tcpSrcRangeValuesBuilder = new TcpSrcRangeValuesBuilder();
        tcpSrcRangeValuesBuilder.setBegin(tcpSrcRangeValues.getBegin());
        tcpSrcRangeValuesBuilder.setEnd(tcpSrcRangeValues.getEnd());
        ofjAugHpeMatchBuilder.setTcpSrcRangeValues(tcpSrcRangeValuesBuilder.build());
    }
}
