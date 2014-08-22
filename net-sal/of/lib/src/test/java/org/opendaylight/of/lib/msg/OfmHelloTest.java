/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.HELLO;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit test for the OfmHello message.
 * <p>
 * We are also using this test class to verify the behavior of the
 * MessageFactory's XID generator.
 *
 * @author Simon Hunt
 */
public class OfmHelloTest extends OfmTest {

    // test files
    private static final String TF_HELLO_10 = "v10/hello";
    private static final String TF_HELLO_11 = "v11/hello";
    private static final String TF_HELLO_12 = "v12/hello";
    private static final String TF_HELLO_13 = "v13/hello";
    private static final String TF_HELLO_13_V3 = "v13/helloV3";
    private static final String TF_HELLO_13_V3FOO = "v13/helloV3Foo";
    private static final String TF_HELLO_13_V0V2 = "v13/helloV0V2";
    private static final String TF_HELLO_13_V0V1V2V3 = "v13/helloV0V1V2V3";

    private MutableMessage mm;
    private MutableMessage mm2;
    private OpenflowMessage msg;

    @Before
    public void beforeTest() {
        MessageFactory.getTestSupport().reset(MessageFactory.TestReset.XID);
    }

    /* IMPLEMENTATION NOTE:
    *   The library supports parsing, creating, and encoding of ERROR messages
    *   in ALL the protocol versions.
    */

    // ========================================================= PARSING ====

    private void verifyVersionBitmap(OfmHello msg, ProtocolVersion... expVers) {
        List<HelloElement> elems = msg.getElements();
        assertEquals(AM_UXS, 1, elems.size());
        HelloElement elem = elems.get(0);
        assertTrue(AM_WRCL, elem instanceof HelloElemVersionBitmap);
        HelloElemVersionBitmap vb = (HelloElemVersionBitmap) elem;
        Set<ProtocolVersion> supported = vb.getSupportedVersions();
        assertEquals(AM_UXS, expVers.length, supported.size());
        for (ProtocolVersion pv: expVers)
            assertTrue("missing exp version", supported.contains(pv));
    }

    @Test
    public void hello13V3Foo() {
        print(EOL + "hello13V3Foo()");
        OfmHello msg = (OfmHello)
                verifyMsgHeader(TF_HELLO_13_V3FOO, V_1_3, HELLO, 24);
        verifyVersionBitmap(msg, V_1_3);
    }

    @Test
    public void hello13V3() {
        print(EOL + "hello13V3()");
        OfmHello msg = (OfmHello)
                verifyMsgHeader(TF_HELLO_13_V3, V_1_3, HELLO, 16);
        verifyVersionBitmap(msg, V_1_3);
    }

    @Test
    public void hello13V0V2() {
        print(EOL + "hello13V0V2()");
        OfmHello msg = (OfmHello)
                verifyMsgHeader(TF_HELLO_13_V0V2, V_1_2, HELLO, 16);
        verifyVersionBitmap(msg, V_1_0, V_1_2);
    }

    @Test
    public void hello13V0V1V2V3() {
        print(EOL + "hello13V0V1V2V3()");
        OfmHello msg = (OfmHello)
                verifyMsgHeader(TF_HELLO_13_V0V1V2V3, V_1_3, HELLO, 16);
        verifyVersionBitmap(msg, V_1_0, V_1_1, V_1_2, V_1_3);
    }

    @Test
    public void hello13() {
        print(EOL + "hello13()");
        verifyMsgHeader(TF_HELLO_13, V_1_3, HELLO, 8);
    }

    @Test
    public void hello12() {
        print(EOL + "hello12()");
        verifyMsgHeader(TF_HELLO_12, V_1_2, HELLO, 8);
    }

    @Test
    public void hello11() {
        print(EOL + "hello11()");
        verifyMsgHeader(TF_HELLO_11, V_1_1, HELLO, 8);
    }

    @Test
    public void hello10() {
        print(EOL + "hello10()");
        verifyMsgHeader(TF_HELLO_10, V_1_0, HELLO, 8);
    }

    // ======================================================== CREATING ====

    @Test
    public void createHelloDefault() {
        print(EOL + "createHelloDefault()");
        mm = MessageFactory.create(V_1_3, HELLO);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, HELLO, 0);
    }

    @Test
    public void createHelloWithDefault() {
        print(EOL + "createHelloDefault()");
        mm = MessageFactory.create(V_1_3, HELLO);
        mm.clearXid();
        mm2 = MessageFactory.create(mm, HELLO);
        verifyMutableHeader(mm2, V_1_3, HELLO, 0);
    }

    @Test
    public void createHelloVer12() {
        print(EOL + "createHelloVer12()");
        mm = MessageFactory.create(V_1_2, HELLO);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_2, HELLO, 0);
    }

    @Test
    public void createHelloWithVer12() {
        print(EOL + "createHelloWithVer12()");
        mm = MessageFactory.create(V_1_2, HELLO);
        mm.clearXid();
        mm2 = MessageFactory.create(mm, HELLO);
        verifyMutableHeader(mm2, V_1_2, HELLO, 0);
    }

    @Test
    public void createHelloXid() {
        print(EOL + "createHelloXid()");
        mm = MessageFactory.create(V_1_3, HELLO);
        mm.clearXid();
        MessageFactory.assignXid(mm);
        verifyMutableHeader(mm, V_1_3, HELLO, 102);
    }

    @Test
    public void createHelloWithXid() {
        print(EOL + "createHelloWithXid()");
        mm = MessageFactory.create(V_1_3, HELLO);
        MessageFactory.assignXid(mm);
        mm2 = MessageFactory.create(mm, HELLO);
        assertEquals(AM_NEQ, mm.getXid(), mm2.getXid());
        mm2.clearXid();
        verifyMutableHeader(mm2, V_1_3, HELLO, 0);
    }

    @Test
    public void alloAlloAllo() {
        print(EOL + "'allo 'allo 'allo!");
        mm = MessageFactory.create(V_1_3, HELLO);
        MessageFactory.assignXid(mm);
        verifyMutableHeader(mm, V_1_3, HELLO, 102);
        MessageFactory.assignXid(mm);
        verifyMutableHeader(mm, V_1_3, HELLO, 103);
        MessageFactory.assignXid(mm);
        verifyMutableHeader(mm, V_1_3, HELLO, 104);
    }

    @Test
    public void toImmutable() {
        print(EOL + "toImmutable()");
        mm = MessageFactory.create(V_1_3, HELLO);
        MessageFactory.assignXid(mm);
        verifyMutableHeader(mm, V_1_3, HELLO, 102);
        assertTrue(AM_HUH, mm.writable());

        // replace our mutable instance with an immutable one...
        msg = mm.toImmutable();
        verifyHeader(msg, V_1_3, HELLO, 102);
        assertTrue(AM_WRCL, OfmHello.class.isInstance(msg));
        assertFalse(AM_HUH, mm.writable());

        // verify that the mutable instance mutators throw exceptions now...
        try {
            MessageFactory.assignXid(mm);
            fail(AM_NOEX);
        } catch (InvalidMutableException ime) {
            print(FMT_EX, ime);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }

        try {
            mm.clearXid();
            fail(AM_NOEX);
        } catch (InvalidMutableException ime) {
            print(FMT_EX, ime);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }

        try {
            mm.toImmutable();
            fail(AM_NOEX);
        } catch (InvalidMutableException ime) {
            print(FMT_EX, ime);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }

        verifyHeader(mm, V_1_3, HELLO, 102);
    }

    // ========================================================= ENCODING ====

    @Test
    public void encodeHello13() {
        print(EOL + "encodeHello13()");
        mm = MessageFactory.create(V_1_3, HELLO);
        mm.clearXid();
        encodeAndVerifyMessage(mm.toImmutable(), TF_HELLO_13);
    }

    @Test
    public void encodeHello12() {
        print(EOL + "encodeHello12()");
        mm = MessageFactory.create(V_1_2, HELLO);
        mm.clearXid();
        encodeAndVerifyMessage(mm.toImmutable(), TF_HELLO_12);
    }

    @Test
    public void encodeHello11() {
        print(EOL + "encodeHello11()");
        mm = MessageFactory.create(V_1_1, HELLO);
        mm.clearXid();
        encodeAndVerifyMessage(mm.toImmutable(), TF_HELLO_11);
    }

    @Test
    public void encodeHello10() {
        print(EOL + "encodeHello10()");
        mm = MessageFactory.create(V_1_0, HELLO);
        mm.clearXid();
        encodeAndVerifyMessage(mm.toImmutable(), TF_HELLO_10);
    }

    private void encodeWithBitmap(String tf, ProtocolVersion... suppVers) {
        Set<ProtocolVersion> supported =
                new HashSet<ProtocolVersion>(Arrays.asList(suppVers));
        HelloElement versionBitmap =
                HelloElementFactory.createVersionBitmapElement(supported);
        mm = MessageFactory.create(versionBitmap.getVersion(), HELLO);
        mm.clearXid();
        OfmMutableHello msg = (OfmMutableHello) mm;
        msg.addElement(versionBitmap);
        encodeAndVerifyMessage(msg.toImmutable(), tf);
    }

    @Test
    public void encodeHello13V3() {
        print(EOL + "encodeHello13V3()");
        encodeWithBitmap(TF_HELLO_13_V3, V_1_3);
    }

    @Test
    public void encodeHello13V0V2() {
        print(EOL + "encodeHello13V0V2()");
        encodeWithBitmap(TF_HELLO_13_V0V2, V_1_0, V_1_2);
    }

    @Test
    public void encodeHello13V0V1V2V3() {
        print(EOL + "encodeHello13V0V1V2V3()");
        encodeWithBitmap(TF_HELLO_13_V0V1V2V3, V_1_0, V_1_1, V_1_2, V_1_3);
    }

    @Test
    public void createBadHello() {
        print(EOL + "createBadHello()");
        try {
            ProtocolVersion[] suppVers = {V_1_0, V_1_2};
            Set<ProtocolVersion> supported =
                    new HashSet<>(Arrays.asList(suppVers));
            HelloElement versionBitmap =
                    HelloElementFactory.createVersionBitmapElement(supported);
            mm = MessageFactory.create(V_1_3, HELLO);
            mm.clearXid();
            OfmMutableHello msg = (OfmMutableHello) mm;
            msg.addElement(versionBitmap);
            fail(AM_NOEX);
        } catch (IllegalArgumentException iae) {
            print(FMT_EX, iae);
            assertEquals(AM_NEQ, "V_1_3: max supported version: V_1_2",
                    iae.getMessage());
        }
    }
}
