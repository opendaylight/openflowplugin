/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.common;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for HandshakeLogic.
 *
 * @author Simon Hunt
 */
public class HandshakeLogicTest {

    private static final String WRPV = "wrong negotiated protocol version";


    /** Create a hello message appropriate for given parameters.
     * @param legacy turns on legacy handshake behaviours
     * @param vers supported protocol versions
     *
     * @return a shiny new hello message
     */
    private OfmHello createHelloMsg(boolean legacy, ProtocolVersion... vers) {
        Set<ProtocolVersion> versions =
                new HashSet<ProtocolVersion>(Arrays.asList(vers));
        ProtocolVersion maxVer = ProtocolVersion.max(versions);

        OfmMutableHello hello = (OfmMutableHello)
                MessageFactory.create(maxVer, MessageType.HELLO);
        hello.clearXid();
        if (!legacy) {
            HelloElement vbitmap =
                    HelloElementFactory.createVersionBitmapElement(versions);
            hello.addElement(vbitmap);
        }
        return (OfmHello) hello.toImmutable();
    }


    private OfmHello thing1;
    private OfmHello thing2;


    private void verifyNegotiated(OfmHello thing1, OfmHello thing2,
                                  ProtocolVersion expVer) {
        ProtocolVersion result = HandshakeLogic.negotiateVersion(thing1, thing2);
        print("H1: {}", thing1.toDebugString());
        print("H2: {}", thing2.toDebugString());
        print("result is: {}", result);
        assertEquals(WRPV, expVer, result);
    }

    @Test
    public void sameVersionNoElements() {
        print(EOL + "sameVersionNoElements()");
        thing1 = createHelloMsg(true, V_1_0);
        thing2 = createHelloMsg(true, V_1_0);
        verifyNegotiated(thing1, thing2, V_1_0);

        thing1 = createHelloMsg(true, V_1_1);
        thing2 = createHelloMsg(true, V_1_1);
        verifyNegotiated(thing1, thing2, V_1_1);

        thing1 = createHelloMsg(true, V_1_2);
        thing2 = createHelloMsg(true, V_1_2);
        verifyNegotiated(thing1, thing2, V_1_2);

        thing1 = createHelloMsg(true, V_1_3);
        thing2 = createHelloMsg(true, V_1_3);
        verifyNegotiated(thing1, thing2, V_1_3);
    }

    @Test
    public void differentVersionNoElements() {
        print(EOL + "differentVersionNoElements()");
        thing1 = createHelloMsg(true, V_1_0);
        thing2 = createHelloMsg(true, V_1_1);
        verifyNegotiated(thing1, thing2, null);

        thing1 = createHelloMsg(true, V_1_2);
        thing2 = createHelloMsg(true, V_1_3);
        verifyNegotiated(thing1, thing2, null);

        thing1 = createHelloMsg(true, V_1_0);
        thing2 = createHelloMsg(true, V_1_2);
        verifyNegotiated(thing1, thing2, null);
    }

    @Test
    public void differentVersionsOneWithElements() {
        print(EOL + "differentVersionsOneWithElements()" );
        thing1 = createHelloMsg(true, V_1_0);
        thing2 = createHelloMsg(false, V_1_0, V_1_1);
        verifyNegotiated(thing1, thing2, V_1_0);

        thing1 = createHelloMsg(true, V_1_2);
        thing2 = createHelloMsg(false, V_1_0, V_1_1, V_1_2);
        verifyNegotiated(thing1, thing2, V_1_2);

        thing1 = createHelloMsg(true, V_1_0);
        thing2 = createHelloMsg(false, V_1_0, V_1_1, V_1_2, V_1_3);
        verifyNegotiated(thing1, thing2, V_1_0);
    }

    @Test
    public void differentVersionsDifferentElements() {
        print(EOL + "differentVersionsDifferentElements()");
        thing1 = createHelloMsg(false, V_1_0, V_1_2);
        thing2 = createHelloMsg(false, V_1_1, V_1_3);
        verifyNegotiated(thing1, thing2, null);
    }

    @Test
    public void differentVersionsOneElementMatch() {
        print(EOL + "differentVersionsOneElementMatch()");
        thing1 = createHelloMsg(false, V_1_0, V_1_1);
        thing2 = createHelloMsg(false, V_1_3, V_1_1);
        verifyNegotiated(thing1, thing2, V_1_1);
    }

    @Test
    public void differentVersionMultipleElementMatch() {
        print(EOL + "differentVersionMultipleElementMatch()");
        thing1 = createHelloMsg(false, V_1_0, V_1_1, V_1_2);
        thing2 = createHelloMsg(false, V_1_0, V_1_1, V_1_2, V_1_3);
        verifyNegotiated(thing1, thing2, V_1_2);
    }

    @Test
    public void matchingElements() {
        print(EOL + "matchingElements()");
        thing1 = createHelloMsg(false, V_1_0, V_1_2, V_1_1, V_1_3);
        thing2 = createHelloMsg(false, V_1_0, V_1_3, V_1_1, V_1_2);
        verifyNegotiated(thing1, thing2, V_1_3);
    }
}
