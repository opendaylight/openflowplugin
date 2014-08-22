/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt.impl;

import org.junit.Test;
import org.opendaylight.of.controller.impl.AbstractTest;
import org.opendaylight.of.controller.pkt.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.controller.pkt.ExperimenterHint.E_NON_NEGATIVE_TYPE;
import static org.opendaylight.of.controller.pkt.HintType.HANDLER;
import static org.opendaylight.of.controller.pkt.HintType.TEST_PACKET;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link HintFactory}.
 *
 * @author Simon Hunt
 */
public class HintFactoryTest extends AbstractTest {

    /** Packet listener fixture. */
    private static final SequencedPacketListener PKT_LISTNER =
            new SequencedPacketAdapter();
    private static final Class<? extends SequencedPacketListener> PL_CLASS =
            PKT_LISTNER.getClass();

    /** Payload fixture. */
    private static final ExperimenterHint.Payload PAYLOAD =
            new ExperimenterHint.Payload() {
                @Override
                public String toString() {
                    return "{Payload:FOO}";
                }
            };


    @Test(expected = NullPointerException.class)
    public void nullHandlerType() {
        HintFactory.createHint(null, PKT_LISTNER.getClass());
    }

    @Test(expected = NullPointerException.class)
    public void nullHandler() {
        HintFactory.createHint(HANDLER, null);
    }

    @Test
    public void handlerHint() {
        print(EOL + "handlerHint()");
        HandlerHint hint = (HandlerHint)
                HintFactory.createHint(HANDLER, PL_CLASS);
        print(hint);
        assertEquals(AM_NEQ, PL_CLASS, hint.getHandlerClass());
    }

    @Test
    public void testPacketHint() {
        print(EOL + "testPacketHint()");
        TypeOnlyHint hint = (TypeOnlyHint) HintFactory.createHint(TEST_PACKET);
        print(hint);
        assertEquals(AM_NEQ, TEST_PACKET, hint.getType());
    }

    // all hint types that are not "type-only"
    private static final HintType[] NOT_TYPE_ONLY = {
            HANDLER
    };

    @Test
    public void notTypeOnly() {
        print(EOL + "notTypeOnly()");
        for (HintType h: NOT_TYPE_ONLY) {
            try {
                HintFactory.createHint(h);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(FMT_EX, e);
            }
        }
    }

    private static final int EXPER_TYPE = -5;

    @Test
    public void experHintNoPayload() {
        print(EOL + "experHintNoPayload()");
        ExperimenterHint hint = (ExperimenterHint)
                HintFactory.createHint(EXPER_TYPE);
        print(hint);
        assertEquals(AM_NEQ, EXPER_TYPE, hint.getEncodedType());
        assertNull(AM_HUH, hint.getType());
        assertNull(AM_HUH, hint.getPayload());
    }

    @Test
    public void experHintWithPayload() {
        print(EOL + "experHintWithPayload()");
        ExperimenterHint hint = (ExperimenterHint)
                HintFactory.createHint(EXPER_TYPE, PAYLOAD);
        print(hint);
        assertEquals(AM_NEQ, EXPER_TYPE, hint.getEncodedType());
        assertNull(AM_HUH, hint.getType());
        assertEquals(AM_NEQ, PAYLOAD, hint.getPayload());
    }

    private static final int POSITIVE_NUMBER = 3;
    @Test
    public void experHintBadType() {
        print(EOL + "experHintBadType()");
        try {
            HintFactory.createHint(POSITIVE_NUMBER);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX> {}", e);
            assertEquals(AM_WREXMSG, E_NON_NEGATIVE_TYPE + POSITIVE_NUMBER,
                    e.getMessage());
        }
    }

}
