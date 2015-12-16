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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeOfTcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeTcpFlagsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjHpeOfMatchTcpFlagsGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.flags.grouping.TcpFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.ofj.hpe.of.match.tcp.flags.grouping.TcpFlagsValuesBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class TcpFlagsConvertor extends HpeAbstractConvertor<OfjHpeOfMatchTcpFlagsGrouping> {
    public TcpFlagsConvertor() {
        super(HpeTcpFlagsKey.class, HpeOfTcpFlags.class, true, OfjHpeOfMatchTcpFlagsGrouping.class);
    }

    @Override
    protected ExtensionAugment<? extends Augmentation<Extension>> convertFromOFJava(MatchPath path, OfjAugHpeMatch ofjAugHpeMatch) {
        TcpFlagsValues tcpFlagsValues = ofjAugHpeMatch.getTcpFlagsValues();
        switch (path) {
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifUpdateFlowStats.class,
                        new HpeAugMatchNotifUpdateFlowStatsBuilder().setTcpFlagsValues(tcpFlagsValues).build(), this.key);
            case PACKETRECEIVED_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifPacketIn.class,
                        new HpeAugMatchNotifPacketInBuilder().setTcpFlagsValues(tcpFlagsValues).build(), this.key);
            case SWITCHFLOWREMOVED_MATCH:
                return new ExtensionAugment<>(HpeAugMatchNotifSwitchFlowRemoved.class,
                        new HpeAugMatchNotifSwitchFlowRemovedBuilder().setTcpFlagsValues(tcpFlagsValues).build(), this.key);
            default:
                throw new IllegalArgumentException("path");
        }
    }

    @Override
    protected void convertToOFJava(Extension extension, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder) {
        OfjHpeOfMatchTcpFlagsGrouping tcpFlagsGrouping = getExtension(extension);
        TcpFlagsValuesBuilder tcpFlagsValuesBuilder = new TcpFlagsValuesBuilder();
        TcpFlagsValues tcpFlagsValues = tcpFlagsGrouping.getTcpFlagsValues();
        tcpFlagsValuesBuilder.setFlags(tcpFlagsValues.getFlags());
        tcpFlagsValuesBuilder.setMask(tcpFlagsValues.getMask());
        ofjAugHpeMatchBuilder.setTcpFlagsValues(tcpFlagsValuesBuilder.build());
    }
}
