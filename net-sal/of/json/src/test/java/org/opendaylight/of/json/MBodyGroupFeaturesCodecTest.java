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
import org.opendaylight.of.lib.mp.MBodyGroupFeatures;
import org.opendaylight.util.json.JSON;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JSON.fromJson;
import static org.opendaylight.util.json.JSON.toJson;
import static org.opendaylight.util.json.JsonValidator.validate;

/**
 * Unit tests for {@link MBodyGroupFeaturesCodec}.
 *
 * @author Shaila Shree
 */
public class MBodyGroupFeaturesCodecTest extends AbstractCodecTest {

    private static String GROUP_FEATURES = getJsonContents("v13/mbodyGroupFeatures");

    @BeforeClass
    public static void init() throws Exception {
        JSON.registerFactory(OfJsonFactory.instance());
    }

    @AfterClass
    public static void afterClass() {
        JSON.unregisterFactory(OfJsonFactory.instance());
    }

    /**
     * An end-to-end test of {@link org.opendaylight.of.json.MBodyMeterFeaturesCodec} of a single
     * meterFeatures using JSON.fromJson and JSON.toJson.
     */
    @Test
    public void encode() {
        MBodyGroupFeatures features = fromJson(GROUP_FEATURES,
                                               MBodyGroupFeatures.class);
        String actual = toJson(features, true);
        assertEquals(normalizeEOL(GROUP_FEATURES), normalizeEOL(actual));

        // schema validation
        validate(actual, MBodyGroupFeaturesCodec.ROOT);
    }
}
