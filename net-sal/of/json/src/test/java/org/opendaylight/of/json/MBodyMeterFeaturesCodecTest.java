/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.mp.MBodyMeterFeatures;
import org.opendaylight.util.json.JSON;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for {@link MBodyMeterFeaturesCodec}.
 * 
 * @author Jesse Hummer
 */
public class MBodyMeterFeaturesCodecTest extends AbstractCodecTest {

    private static String meterFeaturesJs = null;
    private static String metersFeaturesJs = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JSON.registerFactory(OfJsonFactory.instance());

        meterFeaturesJs = getJsonContents("v13/mbodyMeterFeatures");
        metersFeaturesJs = getJsonContents("v13/mbodyMetersFeatures");
    }

    @AfterClass
    public static void afterClass() {
        JSON.unregisterFactory(OfJsonFactory.instance());
    }

    /**
     * An end-to-end test of {@link MBodyMeterFeaturesCodec} of a single
     * meterFeatures using JSON.fromJson and JSON.toJson.
     */
    @Test
    public void testMeterFeatures() {
        MBodyMeterFeatures features =
                JSON.fromJson(meterFeaturesJs, MBodyMeterFeatures.class);
        String actual = JSON.toJson(features, true);
        print(actual);
        assertEquals(normalizeEOL(meterFeaturesJs), normalizeEOL(actual));
        validate(actual, MBodyMeterFeaturesCodec.ROOT);
    }

    /**
     * An end-to-end test of {@link MBodyMeterFeaturesCodec} of multiple
     * meterFeatures using JSON.fromJson and JSON.toJson.
     */
    @Test
    public void testMetersFeatures() {
        List<MBodyMeterFeatures> features =
                JSON.fromJsonList(metersFeaturesJs, MBodyMeterFeatures.class);
        String actual =
                JSON.toJsonList(features, MBodyMeterFeatures.class, true);
        print(actual);
        assertEquals(normalizeEOL(metersFeaturesJs), normalizeEOL(actual));
        validate(actual, MBodyMeterFeaturesCodec.ROOTS);
    }
}
