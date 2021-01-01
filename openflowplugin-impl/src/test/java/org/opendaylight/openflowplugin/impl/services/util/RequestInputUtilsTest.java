/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yangtools.yang.common.Uint32;

public class RequestInputUtilsTest {
    @Test
    public void createMultipartHeader() {
        final short version = OFConstants.OFP_VERSION_1_3;
        final Uint32 xid = Uint32.valueOf(42L);
        final MultipartType type = MultipartType.OFPMPDESC;

        final MultipartRequestInput input = RequestInputUtils
                .createMultipartHeader(type, xid, version)
                .build();

        assertEquals(version, input.getVersion().toJava());
        assertEquals(xid, input.getXid());
        assertEquals(type, input.getType());
        assertFalse(input.getFlags().getOFPMPFREQMORE());
    }
}