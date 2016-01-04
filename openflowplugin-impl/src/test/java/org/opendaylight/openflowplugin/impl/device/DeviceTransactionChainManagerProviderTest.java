/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class DeviceTransactionChainManagerProviderTest {


    @Mock
    DataBroker dataBroker;
    @Mock
    ConnectionContext connectionContext;
    @Mock
    ConnectionContext concurrentConnectionContex;
    @Mock
    private BindingTransactionChain txChain;
    @Mock
    DeviceManager deviceManager;
    @Mock
    private WriteTransaction writeTx;
    @Mock
    private ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler;

    private static final NodeId nodeId = new NodeId("OPF:TEST");
    private DeviceTransactionChainManagerProvider deviceTransactionChainManagerProvider;

    @Before
    public void setup() {
        deviceTransactionChainManagerProvider = new DeviceTransactionChainManagerProvider(dataBroker);
        Mockito.when(connectionContext.getNodeId()).thenReturn(nodeId);
        Mockito.when(concurrentConnectionContex.getNodeId()).thenReturn(nodeId);

        final ReadOnlyTransaction readOnlyTx = Mockito.mock(ReadOnlyTransaction.class);
        //final CheckedFuture<Optional<Node>, ReadFailedException> noExistNodeFuture = Futures.immediateCheckedFuture(Optional.<Node>absent());
//        Mockito.when(readOnlyTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent)).thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTx);
        Mockito.when(dataBroker.createTransactionChain(Matchers.any(TransactionChainListener.class)))
                .thenReturn(txChain);

//        nodeKeyIdent = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
//        txChainManager = new TransactionChainManager(dataBroker, nodeKeyIdent, registration);
        Mockito.when(txChain.newWriteOnlyTransaction()).thenReturn(writeTx);

//        path = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
//        Mockito.when(writeTx.submit()).thenReturn(Futures.<Void, TransactionCommitFailedException>immediateCheckedFuture(null));
    }

    /**
     * This test verifies code path for registering new connection when no {@link org.opendaylight.openflowplugin.impl.device.TransactionChainManager}
     * is present in registry.
     *
     * @throws Exception
     */
    @Test
    public void testProvideTransactionChainManagerOrWaitForNotification1() throws Exception {
        DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration transactionChainManagerRegistration = deviceTransactionChainManagerProvider.provideTransactionChainManager(connectionContext);
        final TransactionChainManager txChainManager = transactionChainManagerRegistration.getTransactionChainManager();

        Assert.assertTrue(transactionChainManagerRegistration.ownedByInvokingConnectionContext());
        Assert.assertNotNull(txChainManager);
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, txChainManager.getTransactionChainManagerStatus());
    }

    /**
     * This test verifies code path for registering new connection when {@link org.opendaylight.openflowplugin.impl.device.TransactionChainManager}
     * is present in registry.
     *
     * @throws Exception
     */
    @Test
    public void testProvideTransactionChainManagerOrWaitForNotification2() throws Exception {
        DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration transactionChainManagerRegistration_1 = deviceTransactionChainManagerProvider.provideTransactionChainManager(connectionContext);
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, transactionChainManagerRegistration_1.getTransactionChainManager().getTransactionChainManagerStatus());
        DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration transactionChainManagerRegistration_2 = deviceTransactionChainManagerProvider.provideTransactionChainManager(concurrentConnectionContex);
        Assert.assertFalse(transactionChainManagerRegistration_2.ownedByInvokingConnectionContext());
    }

    /**
     * This test verifies code path for registering new connection when {@link org.opendaylight.openflowplugin.impl.device.TransactionChainManager}
     * is present in registry and in SHUTTING_DOWN state (finished).
     *
     * @throws Exception
     */
    @Test
    public void testProvideTransactionChainManagerRecreate1() throws Exception {
        DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration txChainManagerRegistration_1 = deviceTransactionChainManagerProvider.provideTransactionChainManager(connectionContext);
        final TransactionChainManager txChainManager = txChainManagerRegistration_1.getTransactionChainManager();
        Assert.assertTrue(txChainManagerRegistration_1.ownedByInvokingConnectionContext());
        Assert.assertNotNull(txChainManager);
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING,
                txChainManagerRegistration_1.getTransactionChainManager().getTransactionChainManagerStatus());

        CheckedFuture<Void, TransactionCommitFailedException> checkedSubmitCleanFuture = Futures.immediateCheckedFuture(null);
        Mockito.when(writeTx.submit()).thenReturn(checkedSubmitCleanFuture);
        txChainManager.close();
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.WAITING_TO_BE_SHUT,
                txChainManagerRegistration_1.getTransactionChainManager().getTransactionChainManagerStatus());
        txChainManager.attemptToRegisterHandler(readyForNewTransactionChainHandler);
        Mockito.verify(readyForNewTransactionChainHandler, Mockito.never()).onReadyForNewTransactionChain();
    }


    /**
     * This test verifies code path for registering new connection when {@link org.opendaylight.openflowplugin.impl.device.TransactionChainManager}
     * is present in registry and in SHUTTING_DOWN state (unfinished).
     *
     * @throws Exception
     */
    @Test
    public void testProvideTransactionChainManagerRecreate2() throws Exception {
        DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration txChainManagerRegistration_1 = deviceTransactionChainManagerProvider.provideTransactionChainManager(connectionContext);
        final TransactionChainManager txChainManager = txChainManagerRegistration_1.getTransactionChainManager();
        Assert.assertTrue(txChainManagerRegistration_1.ownedByInvokingConnectionContext());
        Assert.assertNotNull(txChainManager);
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING,
                txChainManagerRegistration_1.getTransactionChainManager().getTransactionChainManagerStatus());

        SettableFuture<Void> submitCleanFuture = SettableFuture.create();
        CheckedFuture<Void, TransactionCommitFailedException> checkedSubmitCleanFuture =
                Futures.makeChecked(submitCleanFuture, new Function<Exception, TransactionCommitFailedException>() {
                    @Nullable
                    @Override
                    public TransactionCommitFailedException apply(Exception input) {
                        return new TransactionCommitFailedException("tx failed..", input);
                    }
                });
        Mockito.when(writeTx.submit()).thenReturn(checkedSubmitCleanFuture);
        txChainManager.cleanupPostClosure(true);
        Assert.assertEquals(TransactionChainManager.TransactionChainManagerStatus.SHUTTING_DOWN,
                txChainManagerRegistration_1.getTransactionChainManager().getTransactionChainManagerStatus());
        txChainManager.attemptToRegisterHandler(readyForNewTransactionChainHandler);
        Mockito.verify(readyForNewTransactionChainHandler, Mockito.never()).onReadyForNewTransactionChain();

        submitCleanFuture.set(null);
        Mockito.verify(readyForNewTransactionChainHandler).onReadyForNewTransactionChain();
    }

}