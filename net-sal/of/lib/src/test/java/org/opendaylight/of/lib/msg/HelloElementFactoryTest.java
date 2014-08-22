/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.HelloElementType.VERSION_BITMAP;
import static org.opendaylight.util.StringUtils.EOL;
import static org.opendaylight.util.StringUtils.toCamelCase;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for HelloElementFactory.
 *
 * @author Simon Hunt
 */
public class HelloElementFactoryTest extends OfmTest {
    private static final String E_PARSE_FAIL = "failed to parse element";
    private static final String FILE_PREFIX = "msg/struct/helloElem";

    private OfPacketReader pkt;
    private HelloElement elem;

    private OfPacketReader getPkt(HelloElementType elemType, String suffix) {
        String basename = toCamelCase(FILE_PREFIX, elemType);
        return getPacketReader(basename + suffix + HEX);
    }

    private void verify(HelloElement elem, HelloElementType expType, int expLen) {
        assertEquals(AM_NEQ, expType, elem.getElementType());
        assertEquals(AM_NEQ, expLen, elem.header.length); // no getter
    }


    private void verifyVersionBitmap(String suffix,
                                     ProtocolVersion... expVers) {
        try {
            verifyVersionBitmap(true, suffix, expVers);
        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
    }

    /** Verify version bitmap hello element.
     *
     * @param exFails if true, exception causes test to fail
     * @param suffix test file suffix
     * @param expVers expected supported versions
     * @throws MessageParseException if message structure fails to parse
     */
    private void verifyVersionBitmap(boolean exFails, String suffix,
                                     ProtocolVersion... expVers)
            throws MessageParseException {
        pkt = getPkt(VERSION_BITMAP, suffix);

        elem = HelloElementFactory.parseElement(pkt, V_1_3);
        print(elem);
        verify(elem, VERSION_BITMAP, 8);

        assertTrue(AM_WRCL, elem instanceof HelloElemVersionBitmap);
        HelloElemVersionBitmap ei = (HelloElemVersionBitmap) elem;
        Set<ProtocolVersion> suppVers = ei.getSupportedVersions();
        assertEquals(AM_UXCC, expVers.length, suppVers.size());
        for (ProtocolVersion expPv: expVers)
            assertTrue("missing exp ver", suppVers.contains(expPv));

        checkEOBuffer(pkt);
    }


    // ====== Parse Test methods

    @Test
    public void versionBitmapV0() {
        print(EOL + "versionBitmapV0()");
        verifyVersionBitmap("V0", V_1_0);
        // see helloElemVersionBitmapV0.hex for expected values
    }

    @Test
    public void versionBitmapV1V3() {
        print(EOL + "versionBitmapV1V3()");
        verifyVersionBitmap("V1V3", V_1_1, V_1_3);
        // see helloElemVersionBitmapV1V3.hex for expected values
    }

    @Test
    public void versionBitmapV0V1V2V3() {
        print(EOL + "versionBitmapV0V1V2V3()");
        verifyVersionBitmap("V0V1V2V3", V_1_0, V_1_1, V_1_2, V_1_3);
        // see helloElemVersionBitmapV0V1V2V3.hex for expected values
    }

    @Test
    public void badBits() {
        print(EOL + "badBits()");
        try {
            verifyVersionBitmap(false, "BadBits", V_1_0);
        } catch (MessageParseException mpe) {
            print(FMT_EX, mpe);
            assertTrue(AM_WREXMSG, mpe.getMessage()
                    .contains("Bad bits set in version bitmap: 0x8002"));

        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
        // see helloElemVersionBitmapBadBits.hex
    }


    // ====== Create / Encode Test methods

    private Set<ProtocolVersion> suppVers;

    private byte[] getExpBytes(HelloElementType eType, String suffix) {
        String basename = toCamelCase(FILE_PREFIX, eType);
        return getExpByteArray("../" + basename + suffix);
    }

    private void checkEncodedBitmap(HelloElement elem, String suffix) {
        String label = toCamelCase("encode", VERSION_BITMAP) + suffix;
        byte[] expData = getExpBytes(VERSION_BITMAP, suffix);
        print(EOL + label + "()");
        print(elem);
        // encode the element into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        HelloElementFactory.encodeElement(elem, pkt);
        // check that all is as expected
        verifyEncodement(label, expData, pkt);
    }

    @Test
    public void encodeVersionBitmapV0() {
        suppVers = new HashSet<ProtocolVersion>();
        suppVers.add(V_1_0);
        elem = HelloElementFactory.createVersionBitmapElement(suppVers);
        assertEquals(AM_NEQ, V_1_0, elem.getVersion());
        checkEncodedBitmap(elem, "V0");
    }

    @Test
    public void encodeVersionBitmapV1V3() {
        suppVers = new HashSet<ProtocolVersion>();
        suppVers.add(V_1_1);
        suppVers.add(V_1_3);
        elem = HelloElementFactory.createVersionBitmapElement(suppVers);
        assertEquals(AM_NEQ, V_1_3, elem.getVersion());
        checkEncodedBitmap(elem, "V1V3");
    }

    @Test
    public void encodeVersionBitmapV0V1V2V3() {
        suppVers = new HashSet<ProtocolVersion>();
        suppVers.add(V_1_0);
        suppVers.add(V_1_1);
        suppVers.add(V_1_2);
        suppVers.add(V_1_3);
        elem = HelloElementFactory.createVersionBitmapElement(suppVers);
        assertEquals(AM_NEQ, V_1_3, elem.getVersion());
        checkEncodedBitmap(elem, "V0V1V2V3");
    }

}
