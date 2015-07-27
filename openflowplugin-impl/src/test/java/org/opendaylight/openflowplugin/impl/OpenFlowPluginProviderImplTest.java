package org.opendaylight.openflowplugin.impl;

import junit.framework.TestCase;

/**
 * Created by joe on 27.7.2015.
 */
public class OpenFlowPluginProviderImplTest extends TestCase {

    private static final long DUMMY_RPC_REQUEST_QUOTA = 100L;
    private static final Long DUMMY_GLOBAL_NOTIFICATION_QUOTA = 200L;

    public void testInitialize() throws Exception {
        final OpenFlowPluginProviderImpl openFlowPluginProvider = new OpenFlowPluginProviderImpl(DUMMY_RPC_REQUEST_QUOTA, DUMMY_GLOBAL_NOTIFICATION_QUOTA);
        openFlowPluginProvider.initialize();
    }

}