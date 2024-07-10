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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.util.BindingMap;

/**
 *  Test of {@link GroupingResolver}.
 */
public class GroupingResolverTest {

    /**
     * test of method {@link GroupingResolver#getExtension(Augmentable)}.
     */
    @Test
    public void testGetExtension() {
        GroupingResolver<GeneralExtensionListGrouping, Match> eqGroup =
                new GroupingResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlow.class);
        eqGroup.add(GeneralAugMatchRpcAddFlow.class);

        ExtensionList extension1 = new ExtensionListBuilder().setExtensionKey(JoachimTheBig.VALUE).build();
        Match match1 = new MatchBuilder()
            .addAugmentation(new GeneralAugMatchNodesNodeTableFlowBuilder()
                .setExtensionList(BindingMap.of(extension1))
                .build())
            .build();

        ExtensionList extension2 = new ExtensionListBuilder().setExtensionKey(JoachimTheTiny.VALUE).build();
        Match match2 = new MatchBuilder()
            .addAugmentation(new GeneralAugMatchNodesNodeTableFlowBuilder()
                .setExtensionList(BindingMap.of(extension2))
                .build())
            .build();

        assertEquals(JoachimTheBig.VALUE, eqGroup.getExtension(match1).orElseThrow()
            .nonnullExtensionList().values().iterator().next().getExtensionKey());
        assertEquals(JoachimTheTiny.VALUE, eqGroup.getExtension(match2).orElseThrow()
            .nonnullExtensionList().values().iterator().next().getExtensionKey());
    }

    private interface JoachimTheBig extends ExtensionKey {
        // nobody
    }

    private interface JoachimTheTiny extends ExtensionKey {
        // nobody
    }
}
