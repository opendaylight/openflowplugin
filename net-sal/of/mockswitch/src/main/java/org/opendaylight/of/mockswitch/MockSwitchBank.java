/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * Represents a bank of mock openflow switches; useful for
 * OpenFlow Controller and Application unit testing.
 * <p>
 * The mock switch bank is configured via a text-based definition file. This
 * file contains a number of sections:
 * <ul>
 *     <li>
 *         [switches] - defines the mock switches contained within the bank
 *         and possibly used in the scenario
 *     </li>
 *     <li>
 *         [packets] - defines packet payloads that may be used in the scenario
 *     </li>
 *     <li>
 *         [scenario] - defines a programmed set of actions that the bank will
 *         execute when it is activated.
 *     </li>
 * </ul>
 * <p>
 * <em>Notes:</em>
 * The {@code [packets]} functionality has not yet been implemented.
 * The {@code [scenario]} functionality is incomplete; a base set of
 *  action keywords will parse correctly, but the scenario engine has not yet
 *  been implemented. As a provisional workaround, when the bank is activated,
 *  the mock switches are activated one after the other with a short (50ms)
 *  delay between each activation.
 *  <p>
 *  An example definition file is shown below. Note that blank lines and
 *  lines beginning with "#" are considered comments and thus ignored by
 *  the parser.
 *  <pre>
 *  ## Switch bank definition file
 *  [switches]
 *  SW10 = simple10sw4port.def
 *  SW13 = simple13sw32port.def
 *  </pre>
 *  The records in the [switches] section take the form
 *  {@code LNAME = filename.def} where <em>LNAME</em> is the logical name
 *  that will be used to refer to the switch in the scenario, and
 *  <em>filename.def</em> is the name of the definition file describing
 *  the mock switch. The file is assumed to be in the same directory as
 *  the bank definition file.
 *
 *
 *  <p>
 *  <em>This example will be expanded once the [packets] and [scenario]
 *  functionality have been fully implemented.</em>
 * @see MockOpenflowSwitch
 * @author Simon Hunt
 */
public class MockSwitchBank {

    private static final String ARROW = " => ";

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MockSwitchBank.class, "mockSwitchBank");

    private static final String E_SW_ALREADY_ACTIVE = RES
            .getString("e_sw_already_active");
    private static final String E_SW_ALREADY_INACTIVE = RES
            .getString("e_sw_already_inactive");

    private static final int CORE_SIZE = 4;

    private final ScheduledExecutorService executorService =
        newScheduledThreadPool(CORE_SIZE, namedThreads("SwPool"));

    private final BankDefn defn;
    private final boolean showOutput;
    private final CommandProcessor processor;

    /** Constructs the switch bank from a bank definition object.
     *
     * @param path the bank definition file path
     * @param showOutput if true, output to STDOUT is permissible
     */
    public MockSwitchBank(String path, boolean showOutput) {
        this.defn = new BankDefn(path, showOutput);
        this.showOutput = showOutput;
        this.processor = new CommandProcessor(defn, showOutput);
    }

    /** Activates the switch bank, by starting the scenario script. */
    public void activate() {
        print("... activating Switch Bank : " + defn + " ...");
        executorService.schedule(processor, 0, MILLISECONDS);
    }

    /** Resumes the scenario processing after a scripted SUSPEND. */
    public void resume() {
        processor.resume();
    }

    /** Immediately shuts down all activated switches. */
    public void shutdown() {
        // implementation note: calling deactivate on a switch that
        //  is not connected, has no effect
        for (BankDefn.SwitchInfo si: defn.getSwitches())
            si.getSwitch().deactivate();
    }

    /** Prints the given string if output is enabled.
     *
     * @param s the string to print
     */
    private void print(String s) {
        if (showOutput)
            System.out.println(s);
    }

    /** Returns the number of configured mock switches.
     *
     * @return the number of switches in the bank
     */
    public int size() {
        return defn.getSwitches().size();
    }
    
    /**
     * Returns the number of mock switches that are expected to complete the
     * handshake process. The default implementation is the same as the number
     * of switches in the bank.
     * <p>
     * Implementors should override this to account for switches with
     * unsupported protocol versions.
     * 
     * @return number of switches expected to handshake
     */
    public int expectedToCompleteHandshake() {
        return size();
    }

    /** Returns the datapath IDs of the configured switches in an array.
     *
     * @return the datapath IDs
     */
    public DataPathId[] getDpids() {
        DataPathId[] dpids = new DataPathId[size()];
        int i = 0;
        for (BankDefn.SwitchInfo si: defn.getSwitches())
            dpids[i++] = si.getSwitch().getDpid();
        return dpids;
    }

    /** Activates the mock switch with the specified datapath ID.
     * If the switch is already active (connected to the controller) an
     * exception is thrown.
     *
     * @param dpid the switch's datapath ID
     * @throws NullPointerException if dpid is null
     * @throws IllegalArgumentException if there is no datapath with
     *          the specified ID
     * @throws IllegalStateException if the switch is already active
     */
    public void activate(DataPathId dpid) {
        MockOpenflowSwitch sw = defn.findSwitch(dpid);
        if (sw.isActive())
            throw new IllegalStateException(E_SW_ALREADY_ACTIVE + dpid);
        sw.activate();
    }

    /** Deactivates the mock switch with the specified datapath ID.
     * If the switch is already inactive (not connected to the contoller)
     * an exception is thrown.
     *
     * @param dpid the switch's datapath ID
     * @throws NullPointerException if dpid is null
     * @throws IllegalArgumentException if there is no datapath with
     *          the specified ID
     * @throws IllegalStateException if the switch is already inactive
     */
    public void deactivate(DataPathId dpid) {
        MockOpenflowSwitch sw = defn.findSwitch(dpid);
        if (!sw.isActive())
            throw new IllegalStateException(E_SW_ALREADY_INACTIVE + dpid);
        sw.deactivate();
    }

    /** Install a count down latch on the specified mock switch's message
     * handler.
     *
     * @param dpid the id of the switch
     * @param latch the latch to install
     */
    public void setInternalMsgLatch(DataPathId dpid, CountDownLatch latch) {
        MockOpenflowSwitch sw = defn.findSwitch(dpid);
        sw.setInternalMsgLatch(latch);
    }

    /** Ends the scenario by checking each mock switch for failed assertions.
     * Throws an AssertionError if there were issues.
     */
    public void endScenario() {
        StringBuilder sb = new StringBuilder();
        for (BankDefn.SwitchInfo swinfo: defn.getSwitches()) {
            MockOpenflowSwitch sw = swinfo.getSwitch();
            for (String s: sw.failedAssertions()) {
                sb.append(EOL).append(sw.dpid).append(ARROW).append(s);
            }
        }
        if (sb.length() > 0)
            throw new AssertionError(sb);
    }
}