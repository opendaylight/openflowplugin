/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MfbIpv4Dst;
import org.opendaylight.of.lib.match.MfbIpv6Exthdr;
import org.opendaylight.of.lib.match.MfbVlanVid;
import org.opendaylight.util.json.JsonFactory;
import org.opendaylight.util.json.JsonValidationException;
import org.opendaylight.util.json.JsonValidator;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.net.VlanId;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.json.MatchFieldCodecTestUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.VLAN_VID;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for {@link MatchFieldCodec}.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
@Category(SlowTests.class)
public class MatchFieldCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final MatchFieldCodec codec =
            (MatchFieldCodec) factory.codec(MatchField.class);

    private static final String EXP_JSON_PATH = "org/opendaylight/of/json/";
    private static final String V10 = "v10";
    private static final String V13 = "v13";

    private static final String MATCH_FIELD = "matchField.json";
    private static final String MATCH_FIELDS = "matchFields.json";

    private final ClassLoader cl = getClass().getClassLoader();

    private String expected;
    private String actual;
    private String json;

    private String getJson(String dir, String name) {
        String path = EXP_JSON_PATH + dir + "/" + name;
        String contents = null;
        try {
            contents = getFileContents(path, cl);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        if (contents == null)
            fail("Unable to retrieve resource: " + path);
        return contents;
    }

    private String encodeWithPrettyPrint(MatchField field) {
        return codec.encode(field, true);
    }

    private String encodeWithPrettyPrint(List<MatchField> fields) {
        return codec.encodeList(fields, true);
    }

    private void assertJsonEquals(String expected, String actual) {
        assertEquals(AM_NEQ, normalizeEOL(expected.trim()), normalizeEOL(actual));
    }

    @Test
    public void encode() {
        expected = getJson(V13, MATCH_FIELD);
        actual = encodeWithPrettyPrint(createMatchField());
        assertJsonEquals(expected, actual);
    }

    @Test
    public void decode() {
        json = getJson(V13, MATCH_FIELD);
        verifyMatchField((MfbIpv4Dst) codec.decode(json));
        JsonValidator.validate(json, MatchFieldCodec.ROOT);
    }

    @Test
    public void encodeV10() {
        expected = getJson(V10, MATCH_FIELDS);
        actual = encodeWithPrettyPrint(createMatchFields(V_1_0));
        assertJsonEquals(expected, actual);
    }

    @Test
    public void decodeV10() {
        json = getJson(V10, MATCH_FIELDS);
        verifyMatchFields(V_1_0, codec.decodeList(json));
        JsonValidator.validate(json, MatchFieldCodec.ROOTS);
    }

    @Test
    public void encodeV13() {
        expected = getJson(V13, MATCH_FIELDS);
        actual = encodeWithPrettyPrint(createMatchFields(V_1_3));
        assertJsonEquals(expected, actual);
        JsonValidator.validate(actual, MatchFieldCodec.ROOTS);
    }

    @Test
    public void decodeV13() {
        json = getJson(V13, MATCH_FIELDS);
        verifyMatchFields(V_1_3, codec.decodeList(json));
    }

    @Test
    public void testExp13() {
        json = getJson(V13, MATCH_FIELDS);
        JsonValidator.validate(json, MatchFieldCodec.ROOTS);
    }

    @Test
    public void testExp10() {
        json = getJson(V10, MATCH_FIELDS);
        JsonValidator.validate(json, MatchFieldCodec.ROOTS);
    }

    // == further tests for VLAN_VID match fields

    private static final String VLAN_EXACT = "matchFieldVlanExact.json";
    private static final String VLAN_NONE = "matchFieldVlanNone.json";
    private static final String VLAN_PRESENT = "matchFieldVlanPresent.json";

    @Test
    public void vlanExact() {
        json = getJson(V10, VLAN_EXACT);
        print(json);
        MfbVlanVid field = (MfbVlanVid) codec.decode(json);
        print(field.toDebugString());
        assertEquals(AM_NEQ, VlanId.valueOf(42), field.getVlanId());
    }

    @Test
    public void vlanNone() {
        json = getJson(V10, VLAN_NONE);
        print(json);
        MfbVlanVid field = (MfbVlanVid) codec.decode(json);
        print(field.toDebugString());
        assertEquals(AM_NEQ, VlanId.NONE, field.getVlanId());
    }

    @Test
    public void vlanPresent() {
        json = getJson(V10, VLAN_PRESENT);
        print(json);
        MfbVlanVid field = (MfbVlanVid) codec.decode(json);
        print(field.toDebugString());
        assertEquals(AM_NEQ, VlanId.PRESENT, field.getVlanId());
    }

    @Test
    public void encodeVlanExact() {
        MatchField field = createBasicField(V_1_0, VLAN_VID, VlanId.valueOf(42));
        print(field);
        actual = encodeWithPrettyPrint(field);
        print(actual);
        expected = getJson(V10, VLAN_EXACT);
        assertJsonEquals(expected, actual);
    }

    @Test
    public void encodeVlanNone() {
        MatchField field = createBasicField(V_1_0, VLAN_VID, VlanId.NONE);
        print(field);
        actual = encodeWithPrettyPrint(field);
        print(actual);
        expected = getJson(V10, VLAN_NONE);
        assertJsonEquals(expected, actual);
    }

    @Test
    public void encodeVlanPresent() {
        MatchField field = createBasicField(V_1_0, VLAN_VID, VlanId.PRESENT);
        print(field);
        actual = encodeWithPrettyPrint(field);
        print(actual);
        expected = getJson(V10, VLAN_PRESENT);
        assertJsonEquals(expected, actual);
    }

    @Test
    public void validateVlanExact() {
        json = getJson(V10, VLAN_EXACT);
        JsonValidator.validate(json, MatchFieldCodec.ROOT);
    }

    @Test
    public void validateVlanNone() {
        json = getJson(V10, VLAN_NONE);
        JsonValidator.validate(json, MatchFieldCodec.ROOT);
    }

    @Test
    public void validateVlanPresent() {
        json = getJson(V10, VLAN_PRESENT);
        JsonValidator.validate(json, MatchFieldCodec.ROOT);
    }

    @Ignore("put back when schema valiation is enabled")
    @Test(expected=JsonValidationException.class)
    public void validateInvalidVlan() {
        json = getJson(V10, VLAN_NONE).replace(
                VId.NONE.toString().toLowerCase(), "bozo");
        JsonValidator.validate(json, MatchFieldCodec.ROOT);
    }

    // == further tests for IPv6_EXTHDR match fields

    private static final String IPV6_EXTHDR_A = "matchFieldIpv6ExthdrA.json";

    @Test
    public void ipv6ExthdrA() {
        json = getJson(V13, IPV6_EXTHDR_A);
        print(json);
        JsonValidator.validate(json, MatchFieldCodec.ROOT);
        MfbIpv6Exthdr field = (MfbIpv6Exthdr) codec.decode(json);
        print(field.toDebugString());
    }

}

