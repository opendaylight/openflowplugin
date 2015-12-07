package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class NodeConfigServiceImplTest extends ServiceMocking{

    private static final Long DUMMY_XID_VALUE = 150L;
    private static final SwitchConfigFlag DUMMY_FLAG = SwitchConfigFlag.FRAGNORMAL;
    private static final String DUMMY_FLAG_STR = "FRAGNORMAL";
    private static final Integer DUMMY_MISS_SEARCH_LENGTH = 3000;
    NodeConfigServiceImpl nodeConfigService;

    @Test
    public void testSetConfig() throws Exception {
        nodeConfigService = new NodeConfigServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        SetConfigInput setConfigInput = new SetConfigInputBuilder().build();
        nodeConfigService.setConfig(setConfigInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        nodeConfigService = new NodeConfigServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        SetConfigInputBuilder setConfigInputBuilder = new SetConfigInputBuilder();
        setConfigInputBuilder.setFlag(DUMMY_FLAG_STR);
        setConfigInputBuilder.setMissSearchLength(DUMMY_MISS_SEARCH_LENGTH);
        final OfHeader request = nodeConfigService.buildRequest(new Xid(DUMMY_XID_VALUE), setConfigInputBuilder.build());

        assertTrue(request instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput setConfigInput
                = (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput) request;
        assertEquals(DUMMY_FLAG,setConfigInput.getFlags());
        assertEquals(DUMMY_MISS_SEARCH_LENGTH, setConfigInput.getMissSendLen());
        assertEquals(DUMMY_XID_VALUE, setConfigInput.getXid());
    }
}
