/**
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import static org.opendaylight.openflowjava.util.ByteBufUtils.macAddressToString;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractDropTest implements PacketProcessingListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDropTest.class);

    protected static final Integer PRIORITY = 4;
    protected static final Long BUFFER_ID = 0L;
    protected static final Integer HARD_TIMEOUT = 300;
    protected static final Integer IDLE_TIMEOUT = 240;
    protected static final short TABLE_ID = 0;

    static final long STARTUP_LOOP_TICK = 500L;
    static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private static final int PROCESSING_POOL_SIZE = 10000;

    private final int POOL_THREAD_AMOUNT = 8;
    private final ExecutorService executorService;


    private static final AtomicIntegerFieldUpdater<AbstractDropTest> SENT_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "sent");
    private volatile int sent;

    private static final AtomicIntegerFieldUpdater<AbstractDropTest> RCVD_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "rcvd");
    private volatile int rcvd;

    private static final AtomicIntegerFieldUpdater<AbstractDropTest> EXCS_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "excs");
    private volatile int excs;

    protected static final AtomicIntegerFieldUpdater<AbstractDropTest> RPC_FUTURE_SUCCESS_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "ftrSuccess");
    protected volatile int ftrSuccess;

    protected static final AtomicIntegerFieldUpdater<AbstractDropTest> RPC_FUTURE_FAIL_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "ftrFailed");
    protected volatile int ftrFailed;

    protected static final AtomicIntegerFieldUpdater<AbstractDropTest> RUNABLES_EXECUTED = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "runablesExecuted");
    protected volatile int runablesExecuted;

    protected static final AtomicIntegerFieldUpdater<AbstractDropTest> RUNABLES_REJECTED = AtomicIntegerFieldUpdater.newUpdater(AbstractDropTest.class, "runablesRejected");
    protected volatile int runablesRejected;

    public final DropTestStats getStats() {
        return new DropTestStats(this.sent, this.rcvd, this.excs, this.ftrFailed, this.ftrSuccess, this.runablesExecuted, this.runablesRejected);
    }

    public AbstractDropTest() {
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(PROCESSING_POOL_SIZE);
        final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(POOL_THREAD_AMOUNT, POOL_THREAD_AMOUNT, 0,
                TimeUnit.MILLISECONDS,
                workQueue);
        threadPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("dropTest-%d").build());
        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
                try {
                    workQueue.put(r);
                } catch (final InterruptedException e) {
                    throw new RejectedExecutionException("Interrupted while waiting on queue", e);
                }
            }
        });

        executorService = threadPool;
    }

    public final void clearStats() {
        this.sent = 0;
        this.rcvd = 0;
        this.excs = 0;
        this.ftrSuccess = 0;
        this.ftrFailed = 0;
        this.runablesExecuted = 0;
        this.runablesRejected = 0;
    }

    private final void incrementRunableExecuted() {
        RUNABLES_EXECUTED.incrementAndGet(this);
    }

    private final void incrementRunableRejected() {
        RUNABLES_REJECTED.incrementAndGet(this);
    }

    @Override
    public final void onPacketReceived(final PacketReceived notification) {
        LOG.debug("onPacketReceived - Entering - {}", notification);

        RCVD_UPDATER.incrementAndGet(this);

        try {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    incrementRunableExecuted();
                    processPacket(notification);
                }
            });
        } catch (final Exception e) {
            incrementRunableRejected();
        }
        LOG.debug("onPacketReceived - Leaving", notification);
    }

    private static final Instructions DROP_INSTRUCTIONS = makeStaticDropActionInstructions();

    private static Instructions makeStaticDropActionInstructions() {
        // Create an DropAction
        final DropActionCase dropAction = new DropActionCaseBuilder().setDropAction(new DropActionBuilder().build()).build();
        // Create an Action
        final Action ab = new ActionBuilder().setOrder(0).setAction(dropAction).build();
        // Create an Apply Action
        final ApplyActions aab = new ApplyActionsBuilder().setAction(Collections.singletonList(ab)).build();
        // Wrap our Apply Action in an Instruction
        final Instruction ib = new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab).build()).setOrder(0).build();
        // Put our Instruction in a list of Instructions
        return new InstructionsBuilder().setInstruction(Collections.singletonList(ib)).build();
    }

    private void processPacket(final PacketReceived notification) {
        try {
            final byte[] rawPacket = notification.getPayload();
            final byte[] srcMac = Arrays.copyOfRange(rawPacket, 6, 12);

            final MatchBuilder match = new MatchBuilder();
            final EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
            final EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();

            //TODO: use HEX, use binary form
            //Hex.decodeHex("000000000001".toCharArray());

            ethSourceBuilder.setAddress(new MacAddress(macAddressToString(srcMac)));
            ethernetMatch.setEthernetSource(ethSourceBuilder.build());
            match.setEthernetMatch(ethernetMatch.build());

            // Get the Ingress nodeConnectorRef
            final NodeConnectorRef ncr = notification.getIngress();

            // Get the instance identifier for the nodeConnectorRef
            final InstanceIdentifier<?> ncri = ncr.getValue();

            processPacket(ncri.firstIdentifierOf(Node.class), match.build(), DROP_INSTRUCTIONS);

            SENT_UPDATER.incrementAndGet(this);
        } catch (final Exception e) {
            LOG.warn("Failed to process packet: {}", e.getMessage());
            LOG.debug("Failed to process packet.. ", e);
            EXCS_UPDATER.incrementAndGet(this);
        }
    }

    protected abstract void processPacket(InstanceIdentifier<Node> node, Match match, Instructions instructions);


    @Override
    public void close() {
        executorService.shutdown();
    }

    public void countFutureSuccess() {
        RPC_FUTURE_SUCCESS_UPDATER.incrementAndGet(this);
    }

    public void countFutureError() {
        RPC_FUTURE_FAIL_UPDATER.incrementAndGet(this);
    }
}
