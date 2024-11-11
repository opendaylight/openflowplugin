/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.common.txchain;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TransactionChainManagerTest {
    @Mock
    private DataBroker dataBroker;
    @Mock
    private TransactionChain txChain;
    @Mock
    private ReadWriteTransaction writeTx;
    @Mock
    private TransactionChain transactionChain;

    private DataObjectIdentifier.WithKey<Node, NodeKey> nodeKeyIdent;

    private TransactionChainManager txChainManager;
    private DataObjectIdentifier<Node> path;
    private NodeId nodeId;

    @Before
    public void setUp() {
        final ReadTransaction readOnlyTx = mock(ReadTransaction.class);
        when(dataBroker.createTransactionChain()).thenReturn(txChain);
        nodeId = new NodeId("h2g2:42");
        nodeKeyIdent = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(nodeId)).build();
        txChainManager = new TransactionChainManager(dataBroker, nodeId.getValue());
        when(txChain.newReadWriteTransaction()).thenReturn(writeTx);

        path = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(nodeId)).build();
        doReturn(CommitInfo.emptyFluentFuture()).when(writeTx).commit();
        txChainManager.activateTransactionManager();
    }

    @Test
    public void testWriteToTransaction() {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
    }

    /**
     * Tests transaction submit {@link TransactionChainManager#submitTransaction()}.
     */
    @Test
    public void testSubmitTransaction() {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        verify(writeTx).commit();
    }

    /**
     * test of {@link TransactionChainManager#submitTransaction()}: no submit, never enabled.
     */
    @Test
    public void testSubmitTransaction1() {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        verify(writeTx, never()).commit();
    }

    @Test
    public void testSubmitTransactionFailed() {
        doReturn(FluentFutures.immediateFailedFluentFuture(new ExecutionException(new Throwable("mock"))))
            .when(writeTx).commit();
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        verify(writeTx).commit();
    }

    /**
     * Test of {@link TransactionChainManager#enableSubmit()}: no submit - counter is not active.
     */
    @Test
    public void testEnableCounter1() {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        verify(txChain).newReadWriteTransaction();
        verify(writeTx, times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data);
        verify(writeTx, never()).commit();
    }

    @Test
    public void testOnTransactionChainFailed() {
        txChainManager.onTransactionChainFailed(txChain, mock(Throwable.class));
        verify(txChain).close();
        verify(dataBroker, times(2)).createTransactionChain();
    }

    @Test
    public void testAddDeleteOperationTotTxChain() {
        txChainManager.addDeleteOperationToTxChain(LogicalDatastoreType.CONFIGURATION, path);

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).delete(LogicalDatastoreType.CONFIGURATION, path);
    }

    @Test
    public void testDeactivateTransactionChainManager() {
        txChainManager.deactivateTransactionManager();

        verify(txChain).close();
    }

    @Test
    public void testDeactivateTransactionChainManagerFailed() {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        txChainManager.deactivateTransactionManager();

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        verify(writeTx, never()).commit();
        verify(writeTx).cancel();
        verify(txChain).close();
    }

    @Test
    public void testShuttingDown() {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.shuttingDown();

        verify(txChain).newReadWriteTransaction();
        verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        verify(writeTx).commit();
    }

    @Test
    public void testClose() {
        txChainManager.shuttingDown();
        txChainManager.close();
        verify(txChain).close();
    }
}
