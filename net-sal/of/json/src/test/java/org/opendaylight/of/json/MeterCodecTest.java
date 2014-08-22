/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.MBodyMeterConfig;
import org.opendaylight.of.lib.mp.MBodyMutableMeterConfig;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonCodecException;
import org.opendaylight.util.json.JsonFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for MeterCodec.
 * 
 * @author Jesse Hummer
 */
public class MeterCodecTest {

    private static final String JSON_PATH = "org/opendaylight/of/json/v13/";

    private static String meterJs = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
        meterJs = getFileContents(JSON_PATH + "meterInput.json",
                                  MeterCodecTest.class.getClassLoader());
    }

    /**
     * An end-to-end test of MeterCodec of a single meter using JSON.fromJson
     * and JSON.toJson.
     */
    @Test
    public void testMeter() {
        MBodyMeterConfig meter = JSON.fromJson(meterJs, MBodyMeterConfig.class);
        print(JSON.toJson(meter, true));
        String actual = JSON.toJson(meter, true);
        assertEquals(normalizeEOL(meterJs), normalizeEOL(actual));
        validate(actual, MeterCodec.ROOT);
    }

    /**
     * An end-to-end test of MeterCodec of multiple meters using JSON.fromJson
     * and JSON.toJson.
     * 
     * @throws IOException if problems occur reading JSON file
     */
    @Test
    public void testMeters() throws IOException {
        String metersJs = getFileContents(JSON_PATH + "metersInput.json",
                                          MeterCodecTest.class.getClassLoader());

        List<MBodyMeterConfig> meters = JSON
            .fromJsonList(metersJs, MBodyMeterConfig.class);
        assertEquals(normalizeEOL(metersJs),
                     normalizeEOL(JSON.toJsonList(meters,
                                                  MBodyMeterConfig.class, true)));
    }

    /**
     * Test method for
     * {@link org.opendaylight.of.json.MeterCodec#encode(org.opendaylight.of.lib.mp.MBodyMeterConfig)}
     * . Encode a MBodyMeterConfig and compare it to known JSON.
     */
    @Test
    public void testEncodePojo() {
        MeterCodec codec = new MeterCodec();
        MBodyMutableMeterConfig mutMeter = new MBodyMutableMeterConfig(
                                                                       ProtocolVersion.V_1_3);
        mutMeter.meterId(MeterId.valueOf(1L));
        Set<MeterFlag> flags = new HashSet<MeterFlag>();
        flags.add(MeterFlag.BURST);
        flags.add(MeterFlag.KBPS);
        flags.add(MeterFlag.STATS);
        mutMeter.meterFlags(flags);

        // {"burst_size":1000,"rate":1500,"type":"drop"}
        MeterBand band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                                     MeterBandType.DROP, 1500L,
                                                     1000L);
        mutMeter.addBand(band);

        // {"burst_size":1000,"rate":100,"type":"dscp_remark","prec_level":1}
        band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                           MeterBandType.DSCP_REMARK, 100L,
                                           1000L, 1);
        mutMeter.addBand(band);

        // {"burst_size":1000,"rate":100,"type":"experimenter","experimenter":"0x2481"}
        band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                           MeterBandType.EXPERIMENTER, 100L,
                                           1000L, ExperimenterId.HP);
        mutMeter.addBand(band);

        MBodyMeterConfig meter = (MBodyMeterConfig) mutMeter.toImmutable();

        String actual = codec.encode(meter, true);
        assertEquals("Encoded JSON does not match expected JSON",
                     normalizeEOL(meterJs), normalizeEOL(actual));
        validate(actual, MeterCodec.ROOT);
    }

    /**
     * Test method for
     * {@link org.opendaylight.of.json.MeterCodec#decode(com.fasterxml.jackson.databind.node.ObjectNode)}
     * . Decode JSON to a MBodyMeterConfig and compare it to a known
     * MBodyMeterConfig.
     */
    @Test
    public void testDecodeObjectNode() {
        MBodyMutableMeterConfig mutMeter = new MBodyMutableMeterConfig(
                                                                       ProtocolVersion.V_1_3);
        mutMeter.meterId(MeterId.valueOf(1L));
        Set<MeterFlag> flags = new HashSet<MeterFlag>();
        flags.add(MeterFlag.BURST);
        flags.add(MeterFlag.KBPS);
        flags.add(MeterFlag.STATS);
        mutMeter.meterFlags(flags);

        // {"burst_size":1000,"rate":1500,"type":"drop"}
        MeterBand band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                                     MeterBandType.DROP, 1500L,
                                                     1000L);
        mutMeter.addBand(band);

        // {"burst_size":1000,"rate":100,"type":"dscp_remark","prec_level":1}
        band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                           MeterBandType.DSCP_REMARK, 100L,
                                           1000L, 1);
        mutMeter.addBand(band);

        // {"burst_size":1000,"rate":100,"type":"experimenter","experimenter":"0x2481"}
        band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                           MeterBandType.EXPERIMENTER, 100L,
                                           1000L, ExperimenterId.HP);
        mutMeter.addBand(band);

        MBodyMeterConfig meter = (MBodyMeterConfig) mutMeter.toImmutable();

        MeterCodec codec = new MeterCodec();
        MBodyMeterConfig decoded = codec.decode(meterJs);

        assertTrue("Decoded MBodyMeterConfig does not match expected MBodyMeterConfig.",
                   isEquivalent(meter, decoded));
        // NOTE: An alternative is to convert to raw bytes and compare.
        // Ideally MBodyMeterConfig should implement .equals().
    }

    /**
     * Test for equivalence between two objects. This is a workaround for the
     * missing MBodyMeterConfig.equals(MBodyMeterConfig).
     * 
     * @param a An MBodyMeterConfig to compare
     * @param b Another MBodyMeterConfig to compare
     * @return true if the a and b are equivalent, false otherwise
     */
    private boolean isEquivalent(MBodyMeterConfig a, MBodyMeterConfig b) {
        if (a == null || b == null)
            return false;
        // version
        if (a.getVersion().compareTo(b.getVersion()) != 0)
            return false;
        // id
        if (a.getMeterId().compareTo(b.getMeterId()) != 0)
            return false;
        // flags
        if (!isEquivalent(a.getFlags(), b.getFlags()))
            return false;
        // bands
        if (!isEquivilent(a.getBands(), b.getBands()))
            return false;

        return true;
    }

    /**
     * Test for equivalence between two Sets of MeterFlag.
     * 
     * @param a Set of MeterFlag to compare
     * @param b Another Set of MeterFlag to compare
     * @return true if the a and b are equivalent, false otherwise
     */
    private boolean isEquivalent(Set<MeterFlag> a, Set<MeterFlag> b) {
        return (a.equals(b));
    }

    /**
     * Test for equivalence between two Lists of MeterBand.
     * 
     * @param a List of MeterBand to compare
     * @param b Another List of MeterBand to compare
     * @return true if the a and b are equivalent, false otherwise
     */
    private boolean isEquivilent(List<MeterBand> a, List<MeterBand> b) {
        // are both sets null?
        if (a == null && b == null)
            return true;
        // is either set null while the other is not?
        if ((a == null && b != null) || (a != null && b == null))
            return false;
        // are set lengths equal?
        if (a.size() != b.size())
            return false;

        // are list contents equal?
        for (int i = 0; i < a.size(); i++) {
            if (!isEquivalent(a.get(i), b.get(i)))
                return false;
        }
        return true;
    }

    /**
     * Test for equivalence between two MeterBands. This is a workaround for
     * the missing MeterBand.equals(MeterBand).
     * 
     * @param a A MeterBand to compare
     * @param b Another MeterBand to compare
     * @return true if the a and b are equivalent, false otherwise
     */
    private boolean isEquivalent(MeterBand a, MeterBand b) {
        if (!a.getType().equals(b.getType()))
            return false;
        if (a.getBurstSize() != b.getBurstSize())
            return false;
        if (a.getRate() != b.getRate())
            return false;
        if (!a.getVersion().equals(b.getVersion()))
            return false;
        MeterBandType type = a.getType();
        switch (type) {
        case DSCP_REMARK:
            MeterBandDscpRemark ad = (MeterBandDscpRemark) a;
            MeterBandDscpRemark bd = (MeterBandDscpRemark) b;
            if (ad.getPrecLevel() != bd.getPrecLevel())
                return false;
            break;
        case EXPERIMENTER:
            MeterBandExperimenter ae = (MeterBandExperimenter) a;
            MeterBandExperimenter be = (MeterBandExperimenter) b;
            if (!ae.getExpId().equals(be.getExpId()))
                return false;
            break;
        default:
            break;
        }
        return true;
    }

    /**
     * Test method for validating symmetry between
     * {@link org.opendaylight.of.json.MeterCodec#encode(org.opendaylight.of.lib.mp.MBodyMeterConfig)}
     * and
     * {@link org.opendaylight.of.json.MeterCodec#decode(com.fasterxml.jackson.databind.node.ObjectNode)}
     * . Starting with an MBodyMerterConfig, we should be able to encode to
     * JSON then decode that back into an MBodyMeterConfig and the two
     * MBodyMeterConfig objects should be equivalent.
     */
    @Test
    public void testEncodeDecodeSymmetry() {
        MeterCodec mc = new MeterCodec();
        MBodyMutableMeterConfig mutMeter = new MBodyMutableMeterConfig(
                                                                       ProtocolVersion.V_1_3);
        mutMeter.meterId(MeterId.valueOf(1L));
        Set<MeterFlag> flags = new HashSet<MeterFlag>();
        flags.add(MeterFlag.BURST);
        flags.add(MeterFlag.KBPS);
        flags.add(MeterFlag.STATS);
        mutMeter.meterFlags(flags);

        // {"burst_size":1000,"rate":1500,"type":"drop"}
        MeterBand band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                                     MeterBandType.DROP, 1000L,
                                                     1500L);
        mutMeter.addBand(band);

        // {"burst_size":1000,"rate":100,"type":"dscp_remark","prec_level":1}
        band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                           MeterBandType.DSCP_REMARK, 1000L,
                                           100L, 1);
        mutMeter.addBand(band);

        // {"burst_size":1000,"rate":100,"type":"experimenter","experimenter":"0x2481"}
        band = MeterBandFactory.createBand(ProtocolVersion.V_1_3,
                                           MeterBandType.EXPERIMENTER, 1000L,
                                           100L, ExperimenterId.HP);
        mutMeter.addBand(band);

        MBodyMeterConfig meter = (MBodyMeterConfig) mutMeter.toImmutable();

        assertTrue("MBodyMeterConfig encode to decode is not symmetric",
                   isEquivalent(meter, mc.decode(mc.encode(meter, true))));
    }

    /**
     * Test method for validating symmetry between
     * {@link org.opendaylight.of.json.MeterCodec#decode(com.fasterxml.jackson.databind.node.ObjectNode)}
     * and
     * {@link org.opendaylight.of.json.MeterCodec#encode(org.opendaylight.of.lib.mp.MBodyMeterConfig)}
     * . Starting with JSON, we should be able to decode to an
     * MBodyMeterConfig, then encode that back into JSON and the two JSONs
     * should be equivalent.
     * 
     * @throws JsonProcessingException if invalid JSON is encountered
     * @throws IOException if there is a problem reading JSON
     */
    @Test
    public void testDecodeEncodeSymmetry() throws JsonProcessingException,
                                          IOException {
        MeterCodec mc = new MeterCodec();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.readTree(meterJs);

        assertEquals("MBodyMeterConfig decode to encode is not symmetric",
                     normalizeEOL(meterJs),
                     normalizeEOL(mc.encode(mc.decode(meterJs), true)
                         .toString()));
    }

    /**
     * Negative test for bogus MeterBandType.
     * 
     * @throws IOException if there is a problem reading JSON file
     */
    @Test
    public void testBogusBandType() throws IOException {
        String bogusJs = getFileContents(JSON_PATH
                                                 + "meterBogusBandTypeInput.json",
                                         MeterCodecTest.class.getClassLoader());
        try {
            JSON.fromJson(bogusJs, MBodyMeterConfig.class);
            fail("Invalid meter band type not caught.");
        } catch (JsonCodecException e) {
            assertTrue("Incorrect JsonCodecException thrown, " + e, e
                .getMessage().startsWith("Invalid meter band type"));
        }
    }
}
