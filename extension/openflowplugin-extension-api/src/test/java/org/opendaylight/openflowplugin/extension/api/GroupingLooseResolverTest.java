/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

/**
 *  Test of {@link GroupingLooseResolver}.
 */
public class GroupingLooseResolverTest {

    @Test
    public void testGetExtension() {
        GroupingLooseResolver<GeneralExtensionListGrouping> eqGroup =
                new GroupingLooseResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlow.class);
        eqGroup.add(GeneralAugMatchNotifPacketIn.class);

        Match match1 = new MatchBuilder()
            .addAugmentation(new GeneralAugMatchNodesNodeTableFlowBuilder()
                .setExtensionList(BindingMap.of(new ExtensionListBuilder()
                    .setExtensionKey(JoachimTheBig.VALUE)
                    .build()))
                .build())
            .build();

        var match2 = new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service .rev130709.packet.received
            .MatchBuilder().addAugmentation(new GeneralAugMatchNotifPacketInBuilder()
                .setExtensionList(BindingMap.of(
                    new ExtensionListBuilder().setExtensionKey(JoachimTheTiny.VALUE).build()))
                .build())
            .build();

        assertEquals(JoachimTheBig.VALUE, eqGroup.getExtension(match1).orElseThrow()
            .nonnullExtensionList().values().iterator().next().getExtensionKey());
        assertEquals(JoachimTheTiny.VALUE, eqGroup.getExtension(match2).orElseThrow()
            .nonnullExtensionList().values().iterator().next().getExtensionKey());
    }

    private interface JoachimTheBig extends ExtensionKey {
        JoachimTheBig VALUE = () -> JoachimTheBig.class;
    }

    private interface JoachimTheTiny extends ExtensionKey {
        JoachimTheTiny VALUE = () -> JoachimTheTiny.class;
    }
}
