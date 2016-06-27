/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor;

public class ConvertorManagerTest {
    private static final String CONVERT_INPUT = "10";
    private static final Integer CONVERT_EXPECTED_RESULT = 10;
    private static final Short P_CONVERT_INPUT = 0x01;
    private static final String P_CONVERT_RESULT = "12";
    private static final Short P_CONVERT_VERSION = 0x02;

    private Convertor<CharSequence, Integer> convertor;
    private ParametrizedConvertor<Number, String, TestConvertorData> parametrizedConvertor;

    @Before
    public void setUp() throws Exception {
        convertor = new Convertor<CharSequence, Integer>() {
            @Override
            public Class<?> getType() {
                return CharSequence.class;
            }

            @Override
            public Integer convert(CharSequence source) {
                return Integer.valueOf(source.toString());
            }
        };

        parametrizedConvertor = new ParametrizedConvertor<Number, String, TestConvertorData>() {
            @Override
            public Class<?> getType() {
                return Number.class;
            }

            @Override
            public String convert(Number source, TestConvertorData testConvertorData) {
                return String.valueOf(source) + String.valueOf(testConvertorData.getVersion());
            }
        };

        ConvertorManager.getInstance().registerConvertor(convertor);
        ConvertorManager.getInstance().registerConvertor(parametrizedConvertor);
    }

    @Test
    public void testRegisterConvertor() throws Exception {
        boolean success = ConvertorManager.getInstance().registerConvertor(convertor);
        assertFalse("Convertor should be already registered", success);
    }

    @Test
    public void testRegisterParametrizedConvertor() throws Exception {
        boolean success = ConvertorManager.getInstance().registerConvertor(parametrizedConvertor);
        assertFalse("Parametrized convertor should be already registered", success);
    }

    @Test
    public void testConvert() throws Exception {
        final Optional<Integer> result = ConvertorManager.getInstance().convert(CONVERT_INPUT);

        assertTrue("Failed to convert string to integer", result.isPresent());
        assertEquals("Wrong conversion between string and integer", CONVERT_EXPECTED_RESULT, result.get());
    }

    @Test
    public void testCollectionConvert() throws Exception {
        final Optional<List<Boolean>> result = ConvertorManager.getInstance().convert(
                Collections.singletonList(Boolean.TRUE));

        assertFalse("Convertor result should be empty on wrong convertor", result.isPresent());
    }

    @Test
    public void testEmptyCollectionConvert() throws Exception {
        final Optional<List<Boolean>> result = ConvertorManager.getInstance().convert(Collections.emptyList());

        assertFalse("Convertor result should be empty on empty collection", result.isPresent());
    }

    @Test
    public void testFailedConvert() throws Exception {
        final Optional<Integer> result = ConvertorManager.getInstance().convert(null);

        assertFalse("Convertor result should be empty on null input", result.isPresent());
    }

    @Test
    public void testNotFoundConvert() throws Exception {
        final Optional<Boolean> result = ConvertorManager.getInstance().convert(Boolean.TRUE);

        assertFalse("Convertor result should be empty on wrong input", result.isPresent());
    }

    @Test
    public void testParametrizedConvert() throws Exception {
        final TestConvertorData data = new TestConvertorData(P_CONVERT_VERSION);
        final Optional<String> result = ConvertorManager.getInstance().convert(P_CONVERT_INPUT, data);

        assertTrue("Failed to convert short with data to string", result.isPresent());
        assertEquals("Wrong conversion between short with data and string", P_CONVERT_RESULT, result.get());
    }

    @Test
    public void testCollectionParametrizedConvert() throws Exception {
        final TestConvertorData data = new TestConvertorData(P_CONVERT_VERSION);
        final Optional<List<Boolean>> result = ConvertorManager.getInstance().convert(
                Collections.singletonList(Boolean.TRUE), data);

        assertFalse("Convertor result should be empty on wrong convertor", result.isPresent());
    }

    @Test
    public void testEmptyCollectionParametrizedConvert() throws Exception {
        final TestConvertorData data = new TestConvertorData(P_CONVERT_VERSION);
        final Optional<List<Boolean>> result = ConvertorManager.getInstance().convert(Collections.emptyList(), data);

        assertFalse("Convertor result should be empty on empty collection", result.isPresent());
    }

    @Test
    public void testFailedParametrizedConvert() throws Exception {
        final TestConvertorData data = new TestConvertorData(P_CONVERT_VERSION);
        final Optional<String> result = ConvertorManager.getInstance().convert(null, data);

        assertFalse("Parametrized convertor result should be empty on null input", result.isPresent());
    }

    @Test
    public void testNotFoundParametrizedConvert() throws Exception {
        final TestConvertorData data = new TestConvertorData(P_CONVERT_VERSION);
        final Optional<Boolean> result = ConvertorManager.getInstance().convert(Boolean.TRUE, data);

        assertFalse("Parametrized convertor result should be empty on wrong input", result.isPresent());
    }

    private class TestConvertorData extends ConvertorData {
        TestConvertorData(short version) {
            super(version);
        }
    }
}