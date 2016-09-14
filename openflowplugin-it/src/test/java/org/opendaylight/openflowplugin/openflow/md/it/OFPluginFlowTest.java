/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.it;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowjava.protocol.impl.clients.ClientEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
import org.opendaylight.openflowjava.protocol.impl.clients.SleepEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.WaitForMessageEvent;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * covers basic handshake scenarios
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OFPluginFlowTest {

    static final Logger LOG = LoggerFactory
            .getLogger(OFPluginFlowTest.class);

    private static final ArrayBlockingQueue<Runnable> SCENARIO_POOL_QUEUE = new ArrayBlockingQueue<>(1);

    @Inject @Filter(timeout=60000)
    OpenflowPluginProvider openflowPluginProvider;

    @Inject @Filter(timeout=60000)
    BundleContext ctx;

    @Inject @Filter(timeout=60000)
    static DataBroker dataBroker;

    @Inject @Filter(timeout=60000)
    NotificationProviderService notificationService;

    private SimpleClient switchSim;
    private ThreadPoolLoggingExecutor scenarioPool;

    /**
     * test setup
     * @throws InterruptedException
     */
    @Before
    public void setUp() throws InterruptedException {
        LOG.debug("openflowPluginProvider: "+openflowPluginProvider);
        scenarioPool = new ThreadPoolLoggingExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, SCENARIO_POOL_QUEUE, "scenario");
        //FIXME: plugin should provide service exposing startup result via future
        Thread.sleep(5000L);
    }

    /**
     * test tear down
     */
    @After
    public void tearDown() {
        try {
            LOG.debug("tearing down simulator");
            switchSim.getScenarioDone().get(getFailSafeTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String msg = "waiting for scenario to finish failed: "+e.getMessage();
            LOG.error(msg, e);
            Assert.fail(msg);
        } finally {
            scenarioPool.shutdownNow();
            SCENARIO_POOL_QUEUE.clear();
        }

        try {
            LOG.debug("checking if simulator succeeded to connect to controller");
            boolean simulatorWasOnline = switchSim.getIsOnlineFuture().get(100, TimeUnit.MILLISECONDS);
            Assert.assertTrue("simulator failed to connect to controller", simulatorWasOnline);
        } catch (Exception e) {
            String message = "simulator probably failed to connect to controller";
            LOG.error(message, e);
            Assert.fail(message);
        }
    }

    final class TriggerTestListener implements DataTreeChangeListener<FlowCapableNode> {

        public TriggerTestListener() {
            // NOOP
        }

        @Override
        public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNode>> modifications) {

            for (DataTreeModification modification : modifications) {
                if (modification.getRootNode().getModificationType() == ModificationType.WRITE) {
                    InstanceIdentifier<FlowCapableNode> ii = modification.getRootPath().getRootIdentifier();
                    if (ii != null) {
                        LOG.info("Node was added (brm) {}", ii);
                        writeFlow(createTestFlow(), ii);
                        break;
                    }
                }
            }
        }
    }

    /**
     * test basic integration with OFLib running the handshake
     * @throws Exception
     */
    @Test
    public void testFlowMod() throws Exception {
        LOG.debug("testFlowMod integration test");
        TriggerTestListener brmListener = new TriggerTestListener();

        final DataTreeIdentifier<FlowCapableNode> dataTreeIdentifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, getWildcardPath());
        dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, brmListener);

        switchSim = createSimpleClient();
        switchSim.setSecuredClient(false);
        Deque<ClientEvent> handshakeScenario = ScenarioFactory.createHandshakeScenarioVBM(
                ScenarioFactory.VERSION_BITMAP_13, (short) 0, ScenarioFactory.VERSION_BITMAP_10_13, false);
        handshakeScenario.addFirst(new SleepEvent(6000L));
        ScenarioFactory.appendPostHandshakeScenario(handshakeScenario, true);
        WaitForMessageEvent flowModEvent = new WaitForMessageEvent(ByteBufUtils
                .hexStringToBytes(
                        "04 0e 00 58 00 00 00 03 00 00 00 00 00 00 00 0a "
                        + "00 00 00 00 00 00 00 0a 00 00 00 00 00 00 80 00 "
                        + "ff ff ff ff ff ff ff ff ff ff ff ff 00 01 00 00 "
                        + "00 01 00 16 80 00 0a 02 08 00 80 00 19 08 0a 00 "
                        + "00 01 ff ff ff 00 00 00 00 04 00 10 00 00 00 00 "
                        + "00 18 00 08 00 00 00 00"));
        handshakeScenario.addFirst(flowModEvent);
        ScenarioHandler scenario = new ScenarioHandler(handshakeScenario);
        switchSim.setScenarioHandler(scenario);
        scenarioPool.execute(switchSim);
        LOG.info("finishing testFlowMod");
    }

    private static InstanceIdentifier<?> getWildcardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class);
    }

    /**
     * @return
     */
    private static SimpleClient createSimpleClient() {
        return new SimpleClient("localhost", 6653);
    }

    /**
     * @return timeout for case of failure
     */
    private static long getFailSafeTimeout() {
        return 20000;
    }


    /**
     * @return bundle options
     */
    @Configuration
    public Option[] config() {
        LOG.info("configuring...");
        return options(
                systemProperty("osgi.console").value("2401"),
                systemProperty("osgi.bundles.defaultStartLevel").value("4"),
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),

                OFPaxOptionsAssistant.osgiConsoleBundles(),
                OFPaxOptionsAssistant.loggingBudles(),
                OFPaxOptionsAssistant.ofPluginBundles());
    }

    static FlowBuilder createTestFlow() {
        short tableId = 0;
        FlowBuilder flow = new FlowBuilder();
        flow.setMatch(createMatch1().build());
        flow.setInstructions(createDecNwTtlInstructions().build());

        FlowId flowId = new FlowId("127");
        FlowKey key = new FlowKey(flowId);
        if (null == flow.isBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        BigInteger value = BigInteger.TEN;
        flow.setCookie(new FlowCookie(value));
        flow.setCookieMask(new FlowCookie(value));
        flow.setHardTimeout(0);
        flow.setIdleTimeout(0);
        flow.setInstallHw(false);
        flow.setStrict(false);
        flow.setContainerName(null);
        flow.setFlags(new FlowModFlags(false, false, false, false, true));
        flow.setId(flowId);
        flow.setTableId(tableId);

        flow.setKey(key);
        flow.setFlowName("Foo" + "X" + "f1");

        return flow;
    }

    private static MatchBuilder createMatch1() {
        MatchBuilder match = new MatchBuilder();
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4Prefix prefix = new Ipv4Prefix("10.0.0.1/24");
        ipv4Match.setIpv4Destination(prefix);
        Ipv4Match i4m = ipv4Match.build();
        match.setLayer3Match(i4m);

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        match.setEthernetMatch(eth.build());
        return match;
    }

    private static InstructionsBuilder createDecNwTtlInstructions() {
        DecNwTtlBuilder ta = new DecNwTtlBuilder();
        DecNwTtl decNwTtl = ta.build();
        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl).build());
        ab.setKey(new ActionKey(0));
        // Add our drop action to a list
        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setKey(new InstructionKey(0));
        ib.setOrder(0);

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        ib.setKey(new InstructionKey(0));
        isb.setInstruction(instructions);
        return isb;
    }

    static void writeFlow(FlowBuilder flow, InstanceIdentifier<FlowCapableNode> flowNodeIdent) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        final InstanceIdentifier<Flow> path1 = flowNodeIdent.child(Table.class, new TableKey(flow.getTableId()))
                .child(Flow.class, flow.getKey());
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, flow.build(), true);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LOG.debug("Write of flow on device succeeded.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Write of flow on device failed.", throwable);
            }
        });
    }

    //TODO move to separate test util class
    private final static Flow readFlow(InstanceIdentifier<Flow> flow) {
        Flow searchedFlow = null;
        ReadTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Flow>, ReadFailedException> flowFuture =
            rt.read(LogicalDatastoreType.CONFIGURATION, flow);

        try {
          Optional<Flow> maybeFlow = flowFuture.checkedGet(500, TimeUnit.SECONDS);
          if(maybeFlow.isPresent()) {
              searchedFlow = maybeFlow.get();
          }
        } catch (TimeoutException e) {
          LOG.error("Future timed out. Getting FLOW from DataStore failed.", e);
        } catch (ReadFailedException e) {
          LOG.error("Something wrong happened in DataStore. Getting FLOW for userId {} failed.", e);
        }

        return searchedFlow;
    }
}
