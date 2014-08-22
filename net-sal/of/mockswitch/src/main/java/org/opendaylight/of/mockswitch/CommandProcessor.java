/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.TimeUtils;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

/**
 * Implements the execution of {@link ScenarioCommand}s during the
 * activation of a {@link MockSwitchBank}.
 *
 * @author Simon Hunt
 */
public class CommandProcessor implements Runnable {
    private static final TimeUtils TIME = TimeUtils.getInstance();

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            CommandProcessor.class, "commandProcessor");

    private static final String E_NYI = RES.getString("e_nyi");
    private static final String E_UNEX_CMD = RES.getString("e_unex_cmd");
    private static final String E_SUSP_INTR = RES.getString("e_susp_intr");

    private final BankDefn defn;
    private final boolean showOutput;

    private volatile CountDownLatch suspendLatch;

    /** Constructs a processor, ready to execute the scenario commands.
     *
     * @param defn our associated bank definition
     * @param showOutput if true, output to STDOUT is permissible
     */
    public CommandProcessor(BankDefn defn, boolean showOutput) {
        this.defn = defn;
        this.showOutput = showOutput;
    }

    @Override
    public void run() {
        for (ScenarioCommand cmd: defn.getCommands())
            execute(cmd);
    }

    /** Execute the specified command, in the context of the given definition.
     *
     * @param cmd the command to execute
     */
    // NOTE: We know the casts in the following method are safe, because of
    //        the mapping of command type to concrete class. But we have to
    //        suppress the warnings here to keep FindBugs happy.
    public void execute(ScenarioCommand cmd) {
        print(cmd);
        switch (cmd.getType()) {
            case ACTIVATE:
                activate((CmdSwitchVerb) cmd);
                break;
            case DEACTIVATE:
                deactivate((CmdSwitchVerb) cmd);
                break;
            case HELLO_SUCCESS:
                helloSuccess((CmdSwitchVerb) cmd);
                break;
            case DELAY:
                delay((CmdDelay) cmd);
                break;
            case SUSPEND:
                suspend();
                break;

            case ECHO_REQUEST:
                echoRequest((CmdEchoRequest) cmd);
                break;

            case PORT_STATUS:
                portStatus((CmdPortStatus) cmd);
                break;

            case FLOW_REMOVED:
            case PACKET_IN:
                print(E_NYI + cmd);
                break;

            default:
                print(E_UNEX_CMD + cmd);
                break;
        }
    }

    /** Execute a delay for the number of milliseconds defined in the command.
     *
     * @param cmd the command
     */
    private void delay(CmdDelay cmd) {
        try {
            Thread.sleep(cmd.getMsDelay());
        } catch (InterruptedException e) {
            throw new RuntimeException("Oops, interrupted from sleep", e);
        }
    }

    /** Suspend the scenario, waiting to be awoken via a call to resume(). */
    private void suspend() {
        suspendLatch = new CountDownLatch(1);
        try {
            suspendLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(E_SUSP_INTR, e);
        }
    }

    /** Invoked by the mock switch bank when it is time to resume the
     * scenario.
     */
    void resume() {
        print("resume()");
        if (suspendLatch != null)
            suspendLatch.countDown();
    }

    /** Activate the switch defined in the specified command. That is,
     * initiate connection to the controller.
     *
     * @param cmd the command
     */
    private void activate(CmdSwitchVerb cmd) {
        MockOpenflowSwitch sw = defn.findSwitch(cmd.getVar());
        sw.activate();
    }

    /** Deactivate the switch defined in the specified command. That is,
     * initiate disconnection from the controller.
     *
     * @param cmd the command
     */
    private void deactivate(CmdSwitchVerb cmd) {
        MockOpenflowSwitch sw = defn.findSwitch(cmd.getVar());
        sw.deactivate();
    }

    /** Wait for the handshake to complete successfully for the switch
     * defined in the specified command.
     *
     * @param cmd the command
     */
    private void helloSuccess(CmdSwitchVerb cmd) {
        MockOpenflowSwitch sw = defn.findSwitch(cmd.getVar());
        sw.waitForHandshake(true);
    }


    /** Send an EchoRequest message to the controller as defined by the
     *  specified command.
     *
     * @param cmd the command
     */
    private void echoRequest(CmdEchoRequest cmd) {
        // get the switch
        MockOpenflowSwitch sw = defn.findSwitch(cmd.getSwid());
        // get the data
        byte[] data = cmd.getPayload();

        // construct an echoRequest message
        OfmMutableEchoRequest msg = (OfmMutableEchoRequest)
                MessageFactory.create(sw.negotiated, MessageType.ECHO_REQUEST);
        if (data != null)
            msg.data(data);
        OpenflowMessage ereq = msg.toImmutable();
        sw.cache(MessageType.ECHO_REQUEST, ereq);
        sw.send(ereq);
    }


    /** Send a PortStatus message to the controller, as defined by the
     * specified command.
     *
     * @param cmd the command
     */
    private void portStatus(CmdPortStatus cmd) {
        // get the switch
        MockOpenflowSwitch sw = defn.findSwitch(cmd.getSwid());
        // get the port number
        int pnum = cmd.getPortNum();
        // get the new state for the port (UP/DOWN)
        CmdPortStatus.State state = cmd.getState();

        // Tell the switch to remember the new state.
        sw.setPortState(pnum, state);
        Port updatedPort = sw.getPort(pnum);

        // construct a portStatus message
        OfmMutablePortStatus msg = (OfmMutablePortStatus)
                MessageFactory.create(sw.negotiated,
                        MessageType.PORT_STATUS, PortReason.MODIFY);
        try {
            msg.port(updatedPort);
        } catch (IncompleteStructureException e) {
            throw new RuntimeException(e);
        }

        sw.send(msg.toImmutable());
    }

    /** Prints the given thing (with a timestamp) to STDOUT, if we are
     * configured to show output.
     *
     * @param what the thing to print
     */
    private void print(Object what) {
        if (showOutput)
            System.out.println(StringUtils.format("CmdProc:: {} {}",
                    TIME.hhmmssnnn(), what));
    }
}