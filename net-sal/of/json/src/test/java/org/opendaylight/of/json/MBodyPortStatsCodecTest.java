/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for {@link MBodyPortStatsCodec}.
 * 
 * @author Jesse Hummer
 */
public class MBodyPortStatsCodecTest extends AbstractCodecTest {

    private static String statJs = null;
    private static String statsJs = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);

        statJs = getJsonContents("v13/portStats");
        statsJs = getJsonContents("v13/portsStats");
    }

    /**
     * An end-to-end test of {@link MBodyPortStatsCodec} of a single portStats
     * using JSON.fromJson and JSON.toJson.
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testPortStats() {
        MBodyPortStats stat = JSON.fromJson(statJs, MBodyPortStats.class);
        JSON.toJson(stat, true);
    }

    /**
     * An end-to-end test of {@link MBodyPortStatsCodec} of multiple portStats
     * using JSON.fromJson and JSON.toJson.
     */
    @Test
    public void testPortsStats() {
        List<MBodyPortStats> stats = JSON.fromJsonList(statsJs,
                                                       MBodyPortStats.class);
        String actual = JSON.toJsonList(stats, MBodyPortStats.class, true);
        print(actual);
        assertEquals(normalizeEOL(statsJs),
                     normalizeEOL(JSON.toJsonList(stats, MBodyPortStats.class,
                                                  true)));
        validate(actual, MBodyPortStatsCodec.ROOTS);
    }
}
