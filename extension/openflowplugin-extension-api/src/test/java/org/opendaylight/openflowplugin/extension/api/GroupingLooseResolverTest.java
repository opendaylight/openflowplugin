/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;

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

        ExtensionList extension1 = new ExtensionListBuilder().setExtensionKey(JoachimTheBig.class).build();
        Match match1 = new MatchBuilder()
                .addAugmentation(new GeneralAugMatchNodesNodeTableFlowBuilder()
                    .setExtensionList(Collections.singletonMap(extension1.key(), extension1))
                    .build())
                .build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder mb2 =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service
                    .rev130709.packet.received.MatchBuilder();
        ExtensionList extension2 = new ExtensionListBuilder().setExtensionKey(JoachimTheTiny.class).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match match2 =
                mb2.addAugmentation(new GeneralAugMatchNotifPacketInBuilder()
                    .setExtensionList(Collections.singletonMap(extension2.key(), extension2)).build()).build();

        Assert.assertEquals(JoachimTheBig.class,
                eqGroup.getExtension(match1).get().nonnullExtensionList().values().iterator().next().getExtensionKey());
        Assert.assertEquals(JoachimTheTiny.class,
                eqGroup.getExtension(match2).get().nonnullExtensionList().values().iterator().next().getExtensionKey());
    }

    private interface JoachimTheBig extends ExtensionKey {
        // nobody
    }

    private interface JoachimTheTiny extends ExtensionKey {
        // nobody
    }

}
