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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;

/**
 * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConvertorManagerTest {
    @Test
    public void testRegisterConvertor() {
        final ConvertorManager convertorManager = new ConvertorManager(OFConstants.OFP_VERSION_1_3)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, new Convertor<Action, String, VersionConvertorData>() {
                    @Override
                    public Collection<Class<?>> getTypes() {
                        return Set.of(Action.class);
                    }

                    @Override
                    public String convert(final Action source, final VersionConvertorData data) {
                        return null;
                    }
                });

        final Optional<Convertor> convertor = convertorManager.findConvertor(OFConstants.OFP_VERSION_1_3, Action.class);
        assertTrue("Failed to find convertor for action", convertor.isPresent());
    }

    @Test
    public void testConvert() {
        final ConvertorManager convertorManager = new ConvertorManager(OFConstants.OFP_VERSION_1_3)
                .registerConvertor(OFConstants.OFP_VERSION_1_3, new Convertor<Action, String, VersionConvertorData>() {
                    @Override
                    public Collection<Class<?>> getTypes() {
                        return Set.of(Action.class);
                    }

                    @Override
                    public String convert(final Action source, final VersionConvertorData data) {
                        return String.valueOf(source) + String.valueOf(data);
                    }
                });

        final Action source = new ActionBuilder().setOrder(0).build();
        final VersionConvertorData data = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
        final String expectedResult = String.valueOf(source) + String.valueOf(data);
        assertEquals(Optional.of(expectedResult), convertorManager.convert(source, data));
    }

    /**
     * Test for {@link ConvertorManager#convert(Collection, ConvertorData)}.
     */
    @Test
    public void testConvert1() {
        final ConvertorManager convertorManager = new ConvertorManager(OFConstants.OFP_VERSION_1_3)
            .registerConvertor(OFConstants.OFP_VERSION_1_3,
                new Convertor<List<Action>, String, VersionConvertorData>() {
                    @Override
                    public Collection<Class<?>> getTypes() {
                        return Set.of(Action.class);
                    }

                    @Override
                    public String convert(final List<Action> source, final VersionConvertorData data) {
                        return String.valueOf(source) + String.valueOf(data);
                    }
                });

        final List<Action> source = List.of(new ActionBuilder().setOrder(0).build());
        final VersionConvertorData data = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
        final String expectedResult = String.valueOf(source) + String.valueOf(data);
        assertEquals(Optional.of(expectedResult), convertorManager.convert(source, data));
    }
}
