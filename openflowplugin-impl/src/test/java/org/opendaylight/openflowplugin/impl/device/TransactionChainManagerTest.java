/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by mirehak on 4/5/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionChainManagerTest {

    @Mock
    private DataBroker dataBroker;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private BindingTransactionChain txChain;
    @Mock
    private WriteTransaction writeTx;
    @Mock
    private TransactionChain<?, ?> transactionChain;
    @Mock
    HashedWheelTimer timer;
    @Mock
    Registration registration;
    @Mock
    private ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler;

    @Mock
    private KeyedInstanceIdentifier<Node, NodeKey> nodeKeyIdent;

    private Entity entity;
    private EntityOwnershipChange entityOwnershipChange;
    private TransactionChainManager txChainManager;
    private InstanceIdentifier<Node> path;
    private NodeId nodeId;

    @Before
    public void setUp() throws Exception {
        final ReadOnlyTransaction readOnlyTx = Mockito.mock(ReadOnlyTransaction.class);
        final CheckedFuture<Optional<Node>, ReadFailedException> noExistNodeFuture = Futures.immediateCheckedFuture(Optional.<Node>absent());
        Mockito.when(readOnlyTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent)).thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTx);
        Mockito.when(dataBroker.createTransactionChain(Matchers.any(TransactionChainListener.class)))
                .thenReturn(txChain);
        nodeId = new NodeId("h2g2:42");
        nodeKeyIdent = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
        txChainManager = new TransactionChainManager(dataBroker, nodeKeyIdent, registration);
        Assert.assertEquals(txChainManager.getTransactionChainManagerStatus(), TransactionChainManager.TransactionChainManagerStatus.SLEEPING);
        entity = new Entity("openflow", "openflow:1");
        entityOwnershipChange = new EntityOwnershipChange(entity, false, true, true);
        txChainManager.activateTransactionManager(entityOwnershipChange);
        Mockito.when(txChain.newWriteOnlyTransaction()).thenReturn(writeTx);

        path = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
        Mockito.when(writeTx.submit()).thenReturn(Futures.<Void, TransactionCommitFailedException>immediateCheckedFuture(null));
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, txChainManager.getTransactionChainManagerStatus());

        Assert.assertEquals(txChainManager.getLastNode(), false);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(txChain, writeTx);
    }

    @Test
    public void testWriteToTransaction() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
    }

    @Test
    public void testSubmitTransaction() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.enableSubmit();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.submitWriteTransaction();

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx).submit();
    }

    /**
     * test of {@link TransactionChainManager#enableSubmit()}: no submit - counter is not active
     *
     * @throws Exception
     */
    @Test
    public void testEnableCounter1() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx, Mockito.times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx, Mockito.never()).submit();
    }

    @Test
    public void testOnTransactionChainFailed() throws Exception {
        txChainManager.onTransactionChainFailed(transactionChain, Mockito.mock(AsyncTransaction.class), Mockito.mock(Throwable.class));

        Mockito.verify(txChain).close();
        Mockito.verify(dataBroker, Mockito.times(2)).createTransactionChain(txChainManager);
    }

    @Test
    public void testOnTransactionChainSuccessful() throws Exception {
        txChainManager.onTransactionChainSuccessful(transactionChain);
        // NOOP
        Mockito.verifyZeroInteractions(transactionChain);
    }

    @Test
    public void testAddDeleteOperationTotTxChain() throws Exception {
        txChainManager.addDeleteOperationTotTxChain(LogicalDatastoreType.CONFIGURATION, path);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).delete(LogicalDatastoreType.CONFIGURATION, path);
    }


    @Test
    public void testActivateDeactivateTransactionManager() throws Exception {

        //set master and activate
        entityOwnershipChange = new EntityOwnershipChange(entity, false, true, true);
        txChainManager.activateTransactionManager(entityOwnershipChange);
        Assert.assertEquals(txChainManager.getTransactionChainManagerStatus(), TransactionChainManager.TransactionChainManagerStatus.WORKING);

        //try deactivate master
        txChainManager.deactivateTransactionManager(entityOwnershipChange);
        Assert.assertEquals(txChainManager.getTransactionChainManagerStatus(), TransactionChainManager.TransactionChainManagerStatus.WORKING);

        //set slave and deactivate
        entityOwnershipChange = new EntityOwnershipChange(entity, true, false, true);
        txChainManager.deactivateTransactionManager(entityOwnershipChange);
        Assert.assertEquals(txChainManager.getTransactionChainManagerStatus(), TransactionChainManager.TransactionChainManagerStatus.SLEEPING);

        //try activate slave
        txChainManager.deactivateTransactionManager(entityOwnershipChange);
        Assert.assertEquals(txChainManager.getTransactionChainManagerStatus(), TransactionChainManager.TransactionChainManagerStatus.SLEEPING);

    }

    @Test
    public void testCloseTransactionChain() throws Exception {
        //I am not the last entity
        entityOwnershipChange = new EntityOwnershipChange(entity, true, false, true);
        txChainManager.setMarkLastNode(entityOwnershipChange);
        Assert.assertNotEquals(txChainManager.getLastNode(), false);
        txChainManager.close();

        //I am the last entity
        entityOwnershipChange = new EntityOwnershipChange(entity, true, false, false);
        txChainManager.setMarkLastNode(entityOwnershipChange);
        Assert.assertNotEquals(txChainManager.getLastNode(), true);
        txChainManager.close();

        Assert.assertEquals(txChainManager.getTransactionChainManagerStatus(), TransactionChainManager.TransactionChainManagerStatus.SHUTTING_DOWN);
    }
}