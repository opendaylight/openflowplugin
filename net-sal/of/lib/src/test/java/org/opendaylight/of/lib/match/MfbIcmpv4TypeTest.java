/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.ICMPv4Type;
import org.opendaylight.util.net.ICMPv6Type;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ICMPV4_TYPE;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ICMPV6_TYPE;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MfbIcmpv4Type}.
 *
 * @author Simon Hunt
 */
public class MfbIcmpv4TypeTest extends AbstractMatchTest {

    private static final ICMPv4Type REDIR = ICMPv4Type.valueOf("REDIR");
    private static final ICMPv4Type TS_REQ = ICMPv4Type.valueOf("TS_REQ");

    private static MfbIcmpv4Type create(ProtocolVersion pv, ICMPv4Type type) {
        return (MfbIcmpv4Type) createBasicField(pv, ICMPV4_TYPE, type);
    }

    private static final MfbIcmpv4Type I_10_REDIR = create(V_1_0, REDIR);
    private static final MfbIcmpv4Type I_10_REDIR_COPY = create(V_1_0, REDIR);
    private static final MfbIcmpv4Type I_13_REDIR = create(V_1_3, REDIR);
    private static final MfbIcmpv4Type I_13_REDIR_COPY = create(V_1_3, REDIR);

    private static final MfbIcmpv4Type I_10_TSREQ = create(V_1_0, TS_REQ);
    private static final MfbIcmpv4Type I_10_TSREQ_COPY = create(V_1_0, TS_REQ);
    private static final MfbIcmpv4Type I_13_TSREQ = create(V_1_3, TS_REQ);
    private static final MfbIcmpv4Type I_13_TSREQ_COPY = create(V_1_3, TS_REQ);

    private static final MfbIcmpv6Type I6_TYPE = (MfbIcmpv6Type)
            createBasicField(V_1_3, ICMPV6_TYPE, ICMPv6Type.NBR_SOL);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyNotSameButEqual(I_10_REDIR, I_10_REDIR_COPY);
        verifyNotSameButEqual(I_13_REDIR, I_13_REDIR_COPY);
        verifyNotSameButEqual(I_10_TSREQ, I_10_TSREQ_COPY);
        verifyNotSameButEqual(I_13_TSREQ, I_13_TSREQ_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(I_10_REDIR, I_13_REDIR);
        verifyNotSameButEqual(I_10_TSREQ, I_13_TSREQ);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(I_10_REDIR, I_10_TSREQ);
        verifyNotEqual(I_10_REDIR, I_13_TSREQ);
        verifyNotEqual(I_13_REDIR, I_10_TSREQ);
        verifyNotEqual(I_13_REDIR, I_13_TSREQ);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(I_10_REDIR, I6_TYPE);
        verifyNotEqual(I_13_REDIR, I6_TYPE);
        verifyNotEqual(I_10_TSREQ, I6_TYPE);
        verifyNotEqual(I_13_TSREQ, I6_TYPE);
    }
}
