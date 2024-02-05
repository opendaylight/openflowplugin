/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SetConfigImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_XID_VALUE = Uint32.valueOf(150);
    private static final SwitchConfigFlag DUMMY_FLAG = SwitchConfigFlag.FRAGNORMAL;
    private static final String DUMMY_FLAG_STR = "FRAGNORMAL";
    private static final Uint16 DUMMY_MISS_SEARCH_LENGTH = Uint16.valueOf(3000);

    private SetConfigImpl setConfig;

    @Test
    public void testSetConfig() {
        setConfig = new SetConfigImpl(mockedRequestContextStack, mockedDeviceContext);
        setConfig.invoke(dummyConfigInput());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() {
        setConfig = new SetConfigImpl(mockedRequestContextStack, mockedDeviceContext);
        final var request = setConfig.buildRequest(new Xid(DUMMY_XID_VALUE), dummyConfigInput());

        assertTrue(request instanceof org.opendaylight.yang.gen.v1.urn
                .opendaylight.openflow.protocol.rev130731.SetConfigInput);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput setConfigInput
                = (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput) request;
        assertEquals(DUMMY_FLAG,setConfigInput.getFlags());
        assertEquals(DUMMY_MISS_SEARCH_LENGTH, setConfigInput.getMissSendLen());
        assertEquals(DUMMY_XID_VALUE, setConfigInput.getXid());
    }

    private static SetConfigInput dummyConfigInput() {
        return new SetConfigInputBuilder()
            .setFlag(DUMMY_FLAG_STR)
            .setMissSearchLength(DUMMY_MISS_SEARCH_LENGTH)
            .build();
    }
}
