/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionSerializerKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.ExperimenterActionSubType;

/**
 * {@link ExtensionConverterManagerImpl} test
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtensionConverterManagerImplTest {

    private ExtensionConverterManagerImpl manager;
    @Mock
    private ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> extConvertorToOFJava;
    private TypeVersionKey<? extends Action> keyToOFJava;
    @Mock
    private ConvertorActionFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, ActionPath> extConvertorFromOFJava;
    private ExperimenterActionSerializerKey keyFromOFJava;
    private AutoCloseable regFromOFJava;
    private AutoCloseable regToOFJava;

    /**
     * prepare required stuff
     */
    @Before
    public void setUp() {
        manager = new ExtensionConverterManagerImpl();
        keyFromOFJava = new ExperimenterActionSerializerKey((short) 1, 42L, ExpSubType.class);
        regFromOFJava = manager.registerActionConvertor(keyFromOFJava, extConvertorFromOFJava);

        keyToOFJava = new TypeVersionKey<>(ActionExpCase.class, (short) 1);
        regToOFJava = manager.registerActionConvertor(keyToOFJava, extConvertorToOFJava);
    }

    /**
     * tear down test case - close registrations
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        regToOFJava.close();
        regToOFJava.close();
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl#unregister(org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey, org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava)}.
     *
     * @throws Exception
     */
    @Test
    public void testUnregisterConverterExtensionKeyOfQConvertorToOFJavaOfQQ() throws Exception {
        regToOFJava.close();
        Assert.assertNull(manager.getConverter(keyToOFJava));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl#unregister(org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey, org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava)}.
     *
     * @throws Exception
     */
    @Test
    public void testUnregisterMessageTypeKeyOfQConvertorFromOFJavaOfQQ() throws Exception {
        regFromOFJava.close();
        Assert.assertNull(manager.getConverter(keyFromOFJava));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl#getConverter(org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey)}.
     */
    @Test
    public void testGetConverterConverterExtensionKeyOfQ() {
        Assert.assertEquals(extConvertorToOFJava, manager.getConverter(keyToOFJava));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl#getConverter(org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey)}.
     */
    @Test
    public void testGetConverterMessageTypeKeyOfQ() {
        Assert.assertEquals(extConvertorFromOFJava, manager.getActionConverter(keyFromOFJava));
    }

    private static class ExpSubType extends ExperimenterActionSubType {
        // NOOP
    }

    private static interface ActionExpCase extends Action {
        // NOOP
    }

}
