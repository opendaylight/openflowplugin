/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;


import org.junit.Test;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.METADATA;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.TUNNEL_ID;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldBasicLong}.
 *
 * @author Simon Hunt
 */
public class MFieldBasicLongTest extends AbstractMatchTest {

    private static final MfbMetadata MD_10_1000 =
            (MfbMetadata) createBasicField(V_1_0, METADATA, 1000L);
    private static final MfbMetadata MD_10_1000_COPY =
            (MfbMetadata) createBasicField(V_1_0, METADATA, 1000L);
    private static final MfbMetadata MD_13_1000 =
            (MfbMetadata) createBasicField(V_1_3, METADATA, 1000L);

    private static final MfbMetadata MD_13_2048 =
            (MfbMetadata) createBasicField(V_1_3, METADATA, 2048L);
    private static final MfbMetadata MD_13_2048_MASKED =
            (MfbMetadata) createBasicField(V_1_3, METADATA, 2048L, 0xffffL);
    private static final MfbMetadata MD_13_2048_MASKED_COPY =
            (MfbMetadata) createBasicField(V_1_3, METADATA, 2048L, 0xffffL);

    private static final MfbTunnelId TI_13_2048 =
            (MfbTunnelId) createBasicField(V_1_3, TUNNEL_ID, 2048L);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(MD_10_1000, MD_10_1000);
        verifyNotSameButEqual(MD_10_1000, MD_10_1000_COPY);
        verifyNotSameButEqual(MD_13_2048_MASKED, MD_13_2048_MASKED_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(MD_10_1000, MD_13_1000);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(MD_13_1000, MD_13_2048);
    }

    @Test
    public void maskNoMask() {
        print(EOL + "maskNoMask()");
        verifyNotEqual(MD_13_2048, MD_13_2048_MASKED);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(MD_13_2048, TI_13_2048);
        verifyNotEqual(TI_13_2048, OTHER_FIELD);
    }

}
