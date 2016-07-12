/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager}
 */
@RunWith(MockitoJUnitRunner.class)
public class ConvertorManagerTest {
    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager#registerConvertor(short, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor)}
     * @throws Exception
     */
    @Test
    public void testRegisterConvertor() throws Exception {
        final ConvertorManager convertorManager = new ConvertorManager(OFConstants.OFP_VERSION_1_3)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, new Convertor<Action, String, VersionConvertorData>() {
                    @Override
                    public Collection<Class<? extends DataContainer>> getTypes() {
                        return Collections.singleton(Action.class);
                    }

                    @Override
                    public String convert(Action source, VersionConvertorData data) {
                        return null;
                    }
                });

        final Optional<Convertor> convertor = convertorManager.findConvertor(OFConstants.OFP_VERSION_1_3, Action.class);
        assertTrue("Failed to find convertor for action", convertor.isPresent());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager#convert(org.opendaylight.yangtools.yang.binding.DataContainer, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData)}
     * @throws Exception
     */
    @Test
    public void testConvert() throws Exception {
        final ConvertorManager convertorManager = new ConvertorManager(OFConstants.OFP_VERSION_1_3)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, new Convertor<Action, String, VersionConvertorData>() {
                    @Override
                    public Collection<Class<? extends DataContainer>> getTypes() {
                        return Collections.singleton(Action.class);
                    }

                    @Override
                    public String convert(Action source, VersionConvertorData data) {
                        return String.valueOf(source) + String.valueOf(data);
                    }
                });

        final Action source = new ActionBuilder().build();
        final VersionConvertorData data = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
        final String expectedResult = String.valueOf(source) + String.valueOf(data);
        final Optional<String> result = convertorManager.convert(source, data);

        assertTrue("Failed to convert action to string", result.isPresent());
        assertEquals("Result and expected result do not match", result.get(), expectedResult);
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager#convert(java.util.Collection, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData)}
     * @throws Exception
     */
    @Test
    public void testConvert1() throws Exception {
        final ConvertorManager convertorManager = new ConvertorManager(OFConstants.OFP_VERSION_1_3)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, new Convertor<List<Action>, String, VersionConvertorData>() {
                    @Override
                    public Collection<Class<? extends DataContainer>> getTypes() {
                        return Collections.singleton(Action.class);
                    }

                    @Override
                    public String convert(List<Action> source, VersionConvertorData data) {
                        return String.valueOf(source) + String.valueOf(data);
                    }
                });

        final List<Action> source = Collections.singletonList(new ActionBuilder().build());
        final VersionConvertorData data = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
        final String expectedResult = String.valueOf(source) + String.valueOf(data);
        final Optional<String> result = convertorManager.convert(source, data);

        assertTrue("Failed to convert action to string", result.isPresent());
        assertEquals("Result and expected result do not match", result.get(), expectedResult);
    }
}