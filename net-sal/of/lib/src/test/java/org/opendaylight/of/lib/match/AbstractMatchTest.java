/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.AbstractTest;

import static org.junit.Assert.assertNotSame;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IP_PROTO;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.IpProtocol.TCP;

/**
 * Abstract base for match field testing.
 *
 * @author Simon Hunt
 */
public abstract class AbstractMatchTest extends AbstractTest {

    /** Sample IP_PROTO match field. */
    protected static final MfbIpProto OTHER_FIELD =
            (MfbIpProto) FieldFactory.createBasicField(V_1_3, IP_PROTO, TCP);

    /** Asserts that the two object are not the same reference
     * (different instances), but that they are equivalent.
     *
     * @param o1 first object
     * @param o2 second object
     */
    protected void verifyNotSameButEqual(Object o1, Object o2) {
        print(o1);
        assertNotSame(AM_HUH, o1, o2);
        verifyEqual(o1, o2);
    }

}
