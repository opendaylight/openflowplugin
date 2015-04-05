/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by mirehak on 4/5/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionChainManagerTest {

    @Mock
    private DataBroker dataBroker;
    @Mock
    private BindingTransactionChain txChain;
    @Mock
    private WriteTransaction writeTx;
    @Mock
    private TransactionChain<?, ?> transactionChain;

    private TransactionChainManager txChainManager;
    private InstanceIdentifier<Node> path;
    private NodeId nodeId;

    @Before
    public void setUp() throws Exception {
        Mockito.when(dataBroker.createTransactionChain(Matchers.any(TransactionChainListener.class)))
                .thenReturn(txChain);
        txChainManager = new TransactionChainManager(dataBroker, 2);
        Mockito.when(txChain.newWriteOnlyTransaction()).thenReturn(writeTx);

        nodeId = new NodeId("h2g2:42");
        path = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(txChain, writeTx);
    }

    @Test
    public void testWriteToTransaction() throws Exception {
        Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
    }

    @Test
    public void testSubmitTransaction() throws Exception {
        Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx).submit();
    }

    /**
     * test of {@link TransactionChainManager#enableCounter()}: no submit - counter is not active
     * @throws Exception
     */
    @Test
    public void testEnableCounter1() throws Exception {
        Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx, Mockito.times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data);
    }

    /**
     * test of {@link TransactionChainManager#enableCounter()}: submit - after counter activated
     * @throws Exception
     */
    @Test
    public void testEnableCounter2() throws Exception {
        txChainManager.enableCounter();

        Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx, Mockito.times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx).submit();

        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain, Mockito.times(2)).newWriteOnlyTransaction();
        Mockito.verify(writeTx, Mockito.times(4)).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx, Mockito.times(2)).submit();
    }

    @Test
    public void testOnTransactionChainFailed() throws Exception {
        txChainManager.onTransactionChainSuccessful(transactionChain);
        // NOOP
    }

    @Test
    public void testOnTransactionChainSuccessful() throws Exception {
        txChainManager.onTransactionChainFailed(transactionChain, Mockito.mock(AsyncTransaction.class), Mockito.mock(Throwable.class));

        Mockito.verify(txChain).close();
        Mockito.verify(dataBroker, Mockito.times(2)).createTransactionChain(txChainManager);
    }
}