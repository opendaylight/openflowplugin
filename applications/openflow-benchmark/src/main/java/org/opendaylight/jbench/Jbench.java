/*
 * Copyright (c) 2015 Intel Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.jbench;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This is the driver class for Jbench tool. The methods in this class provide
 * CLI using Jcommander library, instantiate objects of SdnController class and
 * instantiate FakeSwitchGroup threads.
 * </p>
 *
 * @author Raksha Madhava Bangera
 */
public class Jbench {

    private static final Logger LOG = LoggerFactory.getLogger("Jbench");

    @Parameter(names = { "-c", "--controller" }, variableArity = true, description = "controller ip and port number")
    private List<String> controllerIp = new ArrayList<>();

    @Parameter(names = { "-d", "--debug" }, description = "enable debugging")
    private int debug = 0;

    @Parameter(names = { "-h", "--help" }, help = true, description = "print this message")
    private boolean help;

    @Parameter(names = { "-l", "--loops" }, description = "loops per test")
    private int loops = 16;

    @Parameter(names = { "-m", "--ms-per-test" }, description = "test length in ms")
    private int msPerTest = 1000;

    @Parameter(names = { "-n", "--number" }, validateWith = CheckAboveZero.class, description = "number of controllers")
    private int numControllers = 1;

    @Parameter(names = { "-s", "--switches" }, validateWith = CheckAboveZero.class, description = "number of switches")
    private int numSwitches = 16;

    @Parameter(names = { "-w", "--warmup" }, description = "loops to be disregarded on test start (warmup)")
    private int warmup = 1;

    @Parameter(names = { "-C", "--cooldown" }, description = "loops to be disregarded at test end (cooldown)")
    private int cooldown = 0;

    @Parameter(names = { "-D",
            "--delay" }, description = "delay starting testing after features_reply is received (in ms)")
    private int delay = 0;

    @Parameter(names = { "-M", "--mac-addresses" }, description = "unique source mac addresses per switch")
    private int macCountPerSwitch = 100000;

    @Parameter(names = { "-O",
            "--operation-mode" }, required = true, validateWith = OperationMode.class, description = "latency or "
                    + "throughput mode")
    private String operationMode;

    private int numThreads = Runtime.getRuntime().availableProcessors();

    private static SdnController[] controllerArray;

    private FakeSwitch[] fakeSwitches;

    private long startTime = 0;

    private long endTime = 0;

    private int startIndex = 0;

    private int endIndex = 0;

    private double aggregateFlowSum = 0;

    private double maxFlow = 0;

    private double minFlow = 0;

    /**
     * <p>
     * This is the entry point of Jbench tool. It parses the command-line
     * options passed by the user and stores it as respective name value pairs.
     * </p>
     *
     * @param args
     * command line options to Jbench program passed by the user
     */
    public static void main(String[] args) {

        Jbench jbench = new Jbench();
        JCommander jcommander = new JCommander(jbench);
        jcommander.setProgramName("Jbench");
        try {
            jcommander.parse(args);
            if (jbench.help) {
                LOG.info("help message");
                jcommander.usage();
                return;
            } else if (jbench.controllerIp.size() != jbench.numControllers) {
                LOG.info("Number of Controller Ip:port tuples supplied and number of controllers didn't match");
                jcommander.usage();
                return;
            }

            controllerArray = jbench.returnControllerArray();
            LOG.info("Jbench: Java-based controller benchmarking tool");
            LOG.info("\trunning in mode '{}'", jbench.operationMode.toLowerCase());
            LOG.info("\tconnecting to controller at:");
            for (int controllerCount = 0; controllerCount < jbench.numControllers; controllerCount++) {
                LOG.info("\t{} : {}", controllerArray[controllerCount].getHost(),
                        controllerArray[controllerCount].getPort());
            }
            LOG.info("\tfaking {} switches {} tests each, {} ms per test", jbench.numSwitches, jbench.loops,
                    jbench.msPerTest);
            LOG.info("\twith {} unique source MACs per switch", jbench.macCountPerSwitch);
            LOG.info("\tstarting test with {} ms delay after features_reply", jbench.delay);
            LOG.info("\tignoring first {} warmup and last {} cooldown loops", jbench.warmup, jbench.cooldown);
            if (jbench.debug == 0) {
                LOG.info("\tdebugging info is off");
            } else {
                LOG.info("\tdebugging info is on");
            }
        } catch (ParameterException ex) {
            LOG.error(ex.getMessage());
            jcommander.usage();
            return;
        }
        jbench.runTests();
    }

    private SdnController[] returnControllerArray() {

        SdnController[] controllerArray = new SdnController[numControllers];

        for (int controllerCount = 0; controllerCount < numControllers; controllerCount++) {
            controllerArray[controllerCount] = new SdnController();
            controllerArray[controllerCount].extractIpAndPort(controllerIp.get(controllerCount));
        }
        return controllerArray;
    }

    private void initializeFakeSwitches() {
        fakeSwitches = new FakeSwitch[numSwitches];
        for (int index = 0; index < this.numSwitches; index++) {
            fakeSwitches[index] = new FakeSwitch(this, index);
            fakeSwitches[index].connectToController();
            fakeSwitches[index].sendHello();
            int type = fakeSwitches[index].getResponseFromController();
            //Wait for Features request and reply exchange
            while (type != 5) {
                type = fakeSwitches[index].getResponseFromController();
            }
        }
    }

    private void closeFakeSwitchConnections() {
        for (int switchCount = 0; switchCount < numSwitches; switchCount++) {
            FakeSwitch fakeSwitch = getFakeSwitch(switchCount);
            fakeSwitch.closeSocket();
        }
    }

    private void runTests() {
        initializeFakeSwitches();
        double averageFlows = 0;
        int effectiveLoops = 0;
        for (int loopCount = 0; loopCount < loops; loopCount++) {
            resetFakeSwitches();
            startTime = System.currentTimeMillis();
            endTime = startTime + msPerTest;
            startIndex = endIndex = 0;
            createFakeSwitchGroup();
            displayPerLoopResults(loopCount);
        }
        closeFakeSwitchConnections();
        effectiveLoops = (loops - warmup - cooldown);
        averageFlows = (aggregateFlowSum) / effectiveLoops;
        LOG.info("Total {} switches {} tests min/max/average = {}/{}/{}/ responses per ms",
                numSwitches, effectiveLoops, minFlow, maxFlow, averageFlows);
    }

    private void resetFakeSwitches() {
        for (int index = 0; index < this.numSwitches; index++) {
            fakeSwitches[index].resetFlowModeCount();
        }
    }

    private void createFakeSwitchGroup() {
        startIndex = endIndex;
        if (this.numSwitches < this.numThreads) {
            this.numThreads = this.numSwitches;
        }
        FakeSwitchGroup[] fakeSwitchGroup = new FakeSwitchGroup[numThreads];
        int switchesPerGroup = this.numSwitches / this.numThreads;
        int balanceSwitches = this.numSwitches % this.numThreads;
        for (int threadCount = 0; threadCount < numThreads; threadCount++) {
            if (threadCount == (numThreads - 1)) {
                switchesPerGroup += balanceSwitches;
            }
            endIndex += switchesPerGroup;
            fakeSwitchGroup[threadCount] = new FakeSwitchGroup("FakeSwitchGroup-" + threadCount, this, startIndex,
                    endIndex);
            fakeSwitchGroup[threadCount].start();
        }
        for (int threadCount = 0; threadCount < numThreads; threadCount++) {
            try {
                fakeSwitchGroup[threadCount].join();
            } catch (InterruptedException e) {
                LOG.info("Interrupted Exception occured {}", e);
            }
        }
    }

    private void displayPerLoopResults(int loop) {
        Date date = new Date();
        String result;
        double flowsPerSecLoop = 0;
        double totalflowsPerMsLoop = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
        String logMessage = sdf.format(date);
        String switchesString = String.valueOf(numSwitches);

        logMessage += " " + switchesString + " switches flows/sec:";
        for (int index = 0; index < this.numSwitches; index++) {
            result = String.valueOf((fakeSwitches[index].getFlowModCount() * 1000) / msPerTest);
            flowsPerSecLoop += (fakeSwitches[index].getFlowModCount() * 1000) / msPerTest;
            logMessage = logMessage + result + "   ";
        }
        totalflowsPerMsLoop = flowsPerSecLoop / 1000;
        LOG.info("{} total {} flows per ms", logMessage, totalflowsPerMsLoop);
        if ((loop >= warmup) && (loop < (loops - cooldown))) {
            if (loop == warmup) {
                minFlow = totalflowsPerMsLoop;
            }
            aggregateFlowSum += (totalflowsPerMsLoop);
            if ((totalflowsPerMsLoop) > maxFlow) {
                maxFlow = totalflowsPerMsLoop;
            } else if ((totalflowsPerMsLoop) < minFlow) {
                minFlow = (totalflowsPerMsLoop);
            }
        }
    }

    /**
     * This method returns the number of controllers
     *
     * @return numControllers - number of controllers
     */
    public int getNumberOfControllers() {
        return numControllers;
    }

    /**
     * This method returns the SdnController Array
     *
     * @return controllerArray - SdnController array
     */
    public SdnController[] getControllerArray() {
        return controllerArray;
    }

    /**
     * This method returns the number of mac addresses per FakeSwitch
     * @return macCountPerSwitch - mac addresses per FakeSwitch simulated by the tool
     */
    public int getNumberOfMacAddresses() {
        return macCountPerSwitch;
    }

    /**
     * This method returns the number of milliseconds per test loop
     * @return msPerTest - milliseconds per each test loop
     */
    public int getMsPerTest() {
        return msPerTest;
    }

    /**
     * @param index - index of the FakeSwitch
     * @return - fakeSwitch object at index
     */
    public FakeSwitch getFakeSwitch(int index) {
        return fakeSwitches[index];
    }

    /**
     * @return - end time for the current test loop
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return - whether debugging is on/off
     */
    public int getDebug() {
        return debug;
    }
}