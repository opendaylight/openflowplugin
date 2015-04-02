/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.connection
 * <p/>
 * test of {@link ConnectionContextImpl} - lightweight version, using basic ways (TDD)
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p/>
 *         Created: Mar 26, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionContextImplTest {

    private ConnectionContextImpl conContext;

    @Mock
    private ConnectionAdapter conAdapter;

    @Before
    public void initialization() {
        // place for mocking method's general behavior for ConnectorAdapter
        conContext = new ConnectionContextImpl(conAdapter);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#ConnectionContextImpl(org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)}.
     */
    @Test
    @Ignore
    public void testConnectionContextImpl() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#getConnectionAdapter()}.
     */
    @Test
    @Ignore
    public void testGetConnectionAdapter() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#getConnectionState()}.
     */
    @Test
    @Ignore
    public void testGetConnectionState() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#getNodeId()}.
     */
    @Test
    @Ignore
    public void testGetNodeId() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#setConnectionState(org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE)}.
     */
    @Test
    @Ignore
    public void testSetConnectionState() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#getFeatures()}.
     */
    @Test
    @Ignore
    public void testGetFeatures() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl#setFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply)}.
     */
    @Test
    @Ignore
    public void testSetFeatures() {
        fail("Not yet implemented");
    }
}
