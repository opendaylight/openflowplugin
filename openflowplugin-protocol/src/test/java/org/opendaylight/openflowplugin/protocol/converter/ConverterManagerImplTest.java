/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter;

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
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ConverterManagerImplTest {
    @Test
    public void testRegisterConvertor() throws Exception {
        final ConverterManagerImpl converterManager = new ConverterManagerImpl(OFConstants.OFP_VERSION_1_3)
                .registerConverter(OFConstants.OFP_VERSION_1_3, new Converter<Action, String, VersionConverterData>() {
                    @Override
                    public Collection<Class<?>> getTypes() {
                        return Collections.singleton(Action.class);
                    }

                    @Override
                    public String convert(Action source, VersionConverterData data) {
                        return null;
                    }
                });

        final Optional<Converter> convertor = converterManager.findConvertor(OFConstants.OFP_VERSION_1_3, Action.class);
        assertTrue("Failed to find converter for action", convertor.isPresent());
    }

    @Test
    public void testConvert() throws Exception {
        final ConverterManagerImpl converterManager = new ConverterManagerImpl(OFConstants.OFP_VERSION_1_3)
                .registerConverter(OFConstants.OFP_VERSION_1_3, new Converter<Action, String, VersionConverterData>() {
                    @Override
                    public Collection<Class<?>> getTypes() {
                        return Collections.singleton(Action.class);
                    }

                    @Override
                    public String convert(Action source, VersionConverterData data) {
                        return String.valueOf(source) + String.valueOf(data);
                    }
                });

        final Action source = new ActionBuilder().build();
        final VersionConverterData data = new VersionConverterData(OFConstants.OFP_VERSION_1_3);
        final String expectedResult = String.valueOf(source) + String.valueOf(data);
        final Optional<String> result = converterManager.convert(source, data);

        assertTrue("Failed to convert action to string", result.isPresent());
        assertEquals("Result and expected result do not match", result.get(), expectedResult);
    }

    @Test
    public void testConvert1() throws Exception {
        final ConverterManagerImpl converterManager = new ConverterManagerImpl(OFConstants.OFP_VERSION_1_3)
                .registerConverter(OFConstants.OFP_VERSION_1_3, new Converter<List<Action>, String, VersionConverterData>() {
                    @Override
                    public Collection<Class<?>> getTypes() {
                        return Collections.singleton(Action.class);
                    }

                    @Override
                    public String convert(List<Action> source, VersionConverterData data) {
                        return String.valueOf(source) + String.valueOf(data);
                    }
                });

        final List<Action> source = Collections.singletonList(new ActionBuilder().build());
        final VersionConverterData data = new VersionConverterData(OFConstants.OFP_VERSION_1_3);
        final String expectedResult = String.valueOf(source) + String.valueOf(data);
        final Optional<String> result = converterManager.convert(source, data);

        assertTrue("Failed to convert action to string", result.isPresent());
        assertEquals("Result and expected result do not match", result.get(), expectedResult);
    }
}