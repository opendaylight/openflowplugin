/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kramesha on 8/27/15.
 */
public class SalRoleServiceImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(SalRoleServiceImplTest.class);

    @Mock
    private RequestContextStack mockRequestContextStack;

    @Mock
    private DeviceContext mockDeviceContext;

    @Mock
    private ConnectionAdapter mockConnectionAdapter;

    @Mock
    private FeaturesReply mockFeaturesReply;

    @Mock
    private ConnectionContext mockConnectionContext;

    @Mock
    private MessageSpy mockMessageSpy;

    @Mock
    private RequestContext<RoleRequestOutput> mockRequestContext;

    @Mock
    private OutboundQueue mockOutboundQueue;

    private final NodeId testNodeId = new NodeId(Uri.getDefaultInstance("openflow:1"));

    private static final long testXid = 100L;

    private NodeRef nodeRef;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        final short testVersion = 4;

        Mockito.when(mockDeviceContext.getPrimaryConnectionContext()).thenReturn(mockConnectionContext);
        Mockito.when(mockConnectionContext.getFeatures()).thenReturn(mockFeaturesReply);
        Mockito.when(mockConnectionContext.getNodeId()).thenReturn(testNodeId);
        Mockito.when(mockFeaturesReply.getVersion()).thenReturn(testVersion);
        Mockito.when(mockDeviceContext.getMessageSpy()).thenReturn(mockMessageSpy);
        Mockito.when(mockRequestContextStack.<RoleRequestOutput>createRequestContext()).thenReturn(mockRequestContext);
        Mockito.when(mockRequestContext.getXid()).thenReturn(new Xid(testXid));
        Mockito.when(mockConnectionContext.getOutboundQueueProvider()).thenReturn(mockOutboundQueue);
        Mockito.when(mockDeviceContext.getPrimaryConnectionContext().getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);

        final NodeKey key = new NodeKey(testNodeId);
        final InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, key)
                .build();
        nodeRef = new NodeRef(path);
    }

    @Test
    public void testSetRole() throws Exception {
        final RoleRequestOutput roleRequestOutput = (new RoleRequestOutputBuilder())
                .setXid(testXid).setGenerationId(BigInteger.valueOf(1)).build();
        final ListenableFuture<RpcResult<RoleRequestOutput>> futureOutput =
                RpcResultBuilder.<RoleRequestOutput>success().withResult(roleRequestOutput).buildFuture();

        Mockito.when(mockRequestContext.getFuture()).thenReturn(futureOutput);


        final SalRoleService salRoleService = new SalRoleServiceImpl(mockRequestContextStack, mockDeviceContext);

        final SetRoleInput setRoleInput = new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMESLAVE)
                .setNode(nodeRef)
                .build();

        final Future<RpcResult<SetRoleOutput>> future = salRoleService.setRole(setRoleInput);

        final RpcResult<SetRoleOutput> roleOutputRpcResult = future.get(5, TimeUnit.SECONDS);
        assertNotNull("RpcResult from future cannot be null.", roleOutputRpcResult);
        assertTrue("RpcResult from future is not successful.", roleOutputRpcResult.isSuccessful());

        final SetRoleOutput setRoleOutput = roleOutputRpcResult.getResult();
        assertNotNull(setRoleOutput);
        assertEquals(BigInteger.valueOf(testXid), setRoleOutput.getTransactionId().getValue());

    }

    private static class SleeperThread implements Runnable
    {
        private final int i;

        private SleeperThread(final int i)
        {
            this.i = i;
        }

        public void run()
        {
            try
            {
                if ((i % 1000) == 0) {
                    LOG.debug("Thread {} about to sleep", this.i);
                }
                Thread.sleep(1000 * 60 * 60);
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * First if we want to be able to run this test on different environment, we need to:
     * - create maximum possible threads
     * - stop the last two threads
     * - a try to run tested method more times than two
     * - if there is an Runtime exception we call at the end of catch clause, its ok and there was no java.lang.OutOfMemoryError: unable to create new native thread
     * -----------------------------------
     * - this test is very unsafe and should be use only on local machine
     *      because is working with OS or VM threads and cause fail in asynchronous testing environment
     *      for this reason is this test on ignore
     * @throws Exception
     */
    @Ignore
    @Test(expected = RuntimeException.class)
    public synchronized void howManyThreads() throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(final Thread t, final Throwable e)
            {
                LOG.debug(e.getMessage());
                System.exit(1);
            }
        });
        //It is possible make more than 70k threads?
        final int numThreads = 70000;
        final Thread [] thread = new Thread[70000];
        for (int i = 0; i < numThreads; i++)
        {
            thread[i] = null;
            try
            {
                thread[i] = new Thread(new SleeperThread(i));
                thread[i].start();
            }
            //Maximum thread reached ....
            catch (final OutOfMemoryError e)
            {
                //Fancy output
                LOG.debug("Threads are out of memory on thread {}", i);
                thread[i].interrupt();
                thread[i-1].interrupt();
                LOG.debug("Stopped last 2 threads");
                LOG.debug("============================================");
                LOG.debug("Running first time setRole");
                testSetRole();
                LOG.debug("FIRST RUN OK ....");
                LOG.debug("============================================");
                LOG.debug("Running second time setRole");
                testSetRole();
                LOG.debug("SECOND RUN OK ....");
                LOG.debug("============================================");
                LOG.debug("Running third time setRole");
                testSetRole();
                LOG.debug("THIRD RUN OK ....");
                LOG.debug("============================================");
                LOG.debug("Running fourth time setRole");
                testSetRole();
                LOG.debug("FOURTH RUN OK ...");
                throw new RuntimeException(String.format("Out of Memory Error on Thread %d", i), e);
            }
        }
    }

    @Test
    public void testDuplicateRoles() throws Exception {
        // set role to slave

        final RoleRequestOutput roleRequestOutput = (new RoleRequestOutputBuilder())
                .setXid(testXid).setGenerationId(BigInteger.valueOf(1)).build();
        final ListenableFuture<RpcResult<RoleRequestOutput>> futureOutput =
                RpcResultBuilder.<RoleRequestOutput>success().withResult(roleRequestOutput).buildFuture();

        Mockito.when(mockRequestContext.getFuture()).thenReturn(futureOutput);

        final SalRoleService salRoleService = new SalRoleServiceImpl(mockRequestContextStack, mockDeviceContext);

        final SetRoleInput setRoleInput = new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMESLAVE)
                .setNode(nodeRef)
                .build();

        final Future<RpcResult<SetRoleOutput>> future = salRoleService.setRole(setRoleInput);

        final RpcResult<SetRoleOutput> roleOutputRpcResult = future.get(5, TimeUnit.SECONDS);
        assertNotNull("RpcResult from future cannot be null.", roleOutputRpcResult);
        assertTrue("RpcResult from future is not successful.", roleOutputRpcResult.isSuccessful());

        final SetRoleOutput setRoleOutput = roleOutputRpcResult.getResult();
        assertNotNull(setRoleOutput);
        assertEquals(BigInteger.valueOf(testXid), setRoleOutput.getTransactionId().getValue());

        // make another role change with the same role - slave
        final Future<RpcResult<SetRoleOutput>> future2 = salRoleService.setRole(setRoleInput);
        final RpcResult<SetRoleOutput> roleOutputRpcResult2 = future2.get(5, TimeUnit.SECONDS);
        assertNotNull("RpcResult from future cannot be null.", roleOutputRpcResult2);
        assertTrue("RpcResult from future for duplicate role is not successful.", roleOutputRpcResult2.isSuccessful());

    }
}
