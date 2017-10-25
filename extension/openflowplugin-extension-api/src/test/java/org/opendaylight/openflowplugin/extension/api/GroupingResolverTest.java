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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionListBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentable;

/**
 *  Test of {@link GroupingResolver}
 */
public class GroupingResolverTest {

    /**
     * test of method {@link GroupingResolver#getExtension(Augmentable)}
     */
    @Test
    public void testGetExtension() {
        GroupingResolver<GeneralExtensionListGrouping, Match> eqGroup = new GroupingResolver<>(GeneralExtensionListGrouping.class);
        eqGroup.add(GeneralAugMatchNodesNodeTableFlow.class);
        eqGroup.add(GeneralAugMatchRpcAddFlow.class);

        MatchBuilder mb1 = new MatchBuilder();
        ExtensionList extension1 = new ExtensionListBuilder().setExtensionKey(JoachimTheBig.class).build();
        GeneralAugMatchNodesNodeTableFlow odlxxx1 = new GeneralAugMatchNodesNodeTableFlowBuilder().setExtensionList(Collections.singletonList(extension1)).build();
        Match match1 = mb1.addAugmentation(GeneralAugMatchNodesNodeTableFlow.class, odlxxx1).build();

        MatchBuilder mb2 = new MatchBuilder();
        ExtensionList extension2 = new ExtensionListBuilder().setExtensionKey(JoachimTheTiny.class).build();
        GeneralAugMatchNodesNodeTableFlow odlxxx2 = new GeneralAugMatchNodesNodeTableFlowBuilder().setExtensionList(Collections.singletonList(extension2)).build();
        Match match2 = mb2.addAugmentation(GeneralAugMatchNodesNodeTableFlow.class, odlxxx2).build();

        Assert.assertEquals(JoachimTheBig.class, eqGroup.getExtension(match1).get().getExtensionList().get(0).getExtensionKey());
        Assert.assertEquals(JoachimTheTiny.class, eqGroup.getExtension(match2).get().getExtensionList().get(0).getExtensionKey());
    }
    
    private static class JoachimTheBig extends ExtensionKey {
        // nobody
    }
    
    private static class JoachimTheTiny extends ExtensionKey {
        // nobody
    }
}
