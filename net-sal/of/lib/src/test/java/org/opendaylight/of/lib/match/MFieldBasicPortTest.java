/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;


import org.junit.Test;
import org.opendaylight.util.net.PortNumber;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.TCP_DST;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.TCP_SRC;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldBasicPort}.
 *
 * @author Simon Hunt
 */
public class MFieldBasicPortTest extends AbstractMatchTest {

    private static final PortNumber P77 = pn(77);
    private static final PortNumber P88 = pn(88);

    private static final MfbTcpSrc TCP_SRC_10_77 =
            (MfbTcpSrc) createBasicField(V_1_0, TCP_SRC, P77);
    private static final MfbTcpSrc TCP_SRC_10_77_COPY =
            (MfbTcpSrc) createBasicField(V_1_0, TCP_SRC, P77);
    private static final MfbTcpSrc TCP_SRC_13_77 =
            (MfbTcpSrc) createBasicField(V_1_3, TCP_SRC, P77);

    private static final MfbTcpDst TCP_DST_13_77 =
            (MfbTcpDst) createBasicField(V_1_3, TCP_DST, P77);
    private static final MfbTcpDst TCP_DST_13_88 =
            (MfbTcpDst) createBasicField(V_1_3, TCP_DST, P88);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(TCP_SRC_10_77, TCP_SRC_10_77);
        verifyNotSameButEqual(TCP_SRC_10_77, TCP_SRC_10_77_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(TCP_SRC_10_77, TCP_SRC_13_77);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(TCP_DST_13_77, TCP_DST_13_88);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(TCP_SRC_10_77, TCP_DST_13_77);
        verifyNotEqual(TCP_SRC_10_77, OTHER_FIELD);
    }
}
