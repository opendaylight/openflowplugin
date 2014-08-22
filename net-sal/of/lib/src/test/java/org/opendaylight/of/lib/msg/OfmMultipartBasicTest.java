/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;

import static junit.framework.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Basic unit tests for OfmMultipartRequest and OfmMultipartReply.
 *
 * @author Simon Hunt
 */
public class OfmMultipartBasicTest {

    @Test
    public void basicRequest() {
        print(EOL + "basicRequest()");
        OfmMutableMultipartRequest request = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST);
        request.clearXid();
        print(request);
        assertFalse(AM_HUH, request.hasMore());
        request.setMoreFlag();
        print(request);
        assertTrue(AM_HUH, request.hasMore());
        assertEquals(AM_UXS, 1, request.getFlags().size());
    }

    @Test
    public void basicReply() {
        print(EOL + "basicReply()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_0, MULTIPART_REPLY);
        reply.clearXid();
        print(reply);
        assertFalse(AM_HUH, reply.hasMore());
        reply.setMoreFlag();
        print(reply);
        assertTrue(AM_HUH, reply.hasMore());
        assertEquals(AM_UXS, 1, reply.getFlags().size());
    }
}
