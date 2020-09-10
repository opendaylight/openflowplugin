/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.action.container.action.choice.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.action.container.action.choice.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/17/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionExtensionHelperTest {

    @Mock
    private ExtensionConverterProvider extensionConverterProvider;

    @Before
    public void setup() {
        OFSessionUtil.getSessionManager().setExtensionConverterProvider(extensionConverterProvider);
        when(extensionConverterProvider.getActionConverter(any(MessageTypeKey.class)))
            .thenReturn((input, path) -> new MockAction());
    }

    @Test
    public void testProcessAlienAction() {
        ActionBuilder actionBuilder = new ActionBuilder();


        ExperimenterIdCaseBuilder experimenterIdCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        experimenterBuilder.setExperimenter(new ExperimenterId(Uint32.valueOf(42)));
        experimenterIdCaseBuilder.setExperimenter(experimenterBuilder.build());
        actionBuilder.setActionChoice(experimenterIdCaseBuilder.build());
        Action action = ActionExtensionHelper.processAlienAction(actionBuilder.build(), OpenflowVersion.OF13,
                ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);
        assertNotNull(action);
        assertEquals(MockAction.class, action.implementedInterface());
    }


    private class MockAction implements Action {

        @Override
        public Class<? extends DataContainer> implementedInterface() {
            return MockAction.class;
        }
    }
}
