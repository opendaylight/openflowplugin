/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.extension;

import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/17/14.
 */
public class ActionExtensionHelperTest {

    @Test
    /**
     * Trivial test for {@link ActionExtensionHelper#processAlienAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion, org.opendaylight.openflowplugin.extension.api.path.ActionPath)}
     */
    public void testProcessAlienAction() {
        ActionBuilder actionBuilder = new ActionBuilder();
        ExperimenterIdActionBuilder experimenterIdActionBuilder = new ExperimenterIdActionBuilder();
        ExperimenterId experimenterId = new ExperimenterId(new Long(42));
        experimenterIdActionBuilder.setExperimenter(experimenterId);

        experimenterIdActionBuilder.setSubType(MockExperimenterActionSubtype.class);

        actionBuilder.addAugmentation(ExperimenterIdAction.class, experimenterIdActionBuilder.build());
        Action action = ActionExtensionHelper.processAlienAction(actionBuilder.build(), OpenflowVersion.OF13, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);
        assertNull(action);
    }


    private class MockExperimenterActionSubtype extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.ExperimenterActionSubType {

    }


}
