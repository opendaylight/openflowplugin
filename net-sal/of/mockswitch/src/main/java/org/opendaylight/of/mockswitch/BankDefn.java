/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.FilePathUtils;
import org.opendaylight.util.ResourceUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.StringUtils.toCamelCase;

/**
 * Reads in a Switch Bank definition / scenario file,
 * encapsulating the information.
 *
 * @author Simon Hunt
 */
public class BankDefn extends AbstractDefn {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            BankDefn.class, "bankDefn");

    private static final String E_NO_SWITCH = RES.getString("e_no_switch");

    private static final char FS = '/';
    private final String dir;

    /** Constructs a switch-bank definition reader, for the given file path.
     *
     * @param path the bank definition file pathname
     * @param showOutput true if output is enabled
     */
    public BankDefn(String path, boolean showOutput) {
        super(new BankDefReader(path));
        dir = FilePathUtils.parentPath(path, FS);
        preProcessSwitches(showOutput);
        preProcessScenario();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOL).append("Switches:");
        for (SwitchInfo si: getSwitches())
            sb.append(EOLI).append(si);
        sb.append(EOL).append("Scenario:");
        for (ScenarioCommand sc: getCommands())
            sb.append(EOLI).append(sc);
        sb.append(EOLI_DASH);
        return sb.toString();
    }

    /** Iterates across the switch info definitions and instantiates
     * the mock switches.
     * @param showOutput true if output is enabled
     */
    private void preProcessSwitches(boolean showOutput) {
        for (SwitchInfo info: getSwitches()) {
            String path = FilePathUtils.combinePath(dir, info.getFname(), FS);
            MockOpenflowSwitch sw = new MockOpenflowSwitch(path, showOutput);
            info.setSwitch(sw);
        }
    }

    /** Iterates across the bank definition and prepares for running the
     * scenario.
     */
    private void preProcessScenario() {
        // TODO - Scenario pre-processing : validate "script"
        /*
         * For now, this is just a place-holder for being able to script
         * a test scenario with the mock switches.
         */
    }

    /** Returns an unmodifiable view of the switch bindings.
     *
     * @return the switch bindings
     */
    public List<SwitchInfo> getSwitches() {
        return Collections.unmodifiableList(((BankDefReader) reader).switches);
    }

    /** Returns an unmodifiable view of the scenario commands.
     *
     * @return the scenario commands
     */
    public List<ScenarioCommand> getCommands() {
        return Collections.unmodifiableList(((BankDefReader) reader).cmds);
    }

    /** Returns the mock switch with the specified datapath id.
     * If no such switch is configured, an exception is thrown.
     *
     * @param dpid the datapath ID to match
     * @return the corresponding switch
     * @throws NullPointerException if dpid is null
     * @throws IllegalArgumentException if dpid does not match a configured
     *          switch
     */
    public MockOpenflowSwitch findSwitch(DataPathId dpid) {
        notNull(dpid);
        for (SwitchInfo si: getSwitches())
            if (si.getSwitch().getDpid().equals(dpid))
                return si.getSwitch();
        throw new IllegalArgumentException(E_NO_SWITCH + dpid);
    }

    /** Returns the mock switch with the specified scenario variable name.
     * If no such switch is configured, an exception is thrown.
     *
     * @param var the variable name for the switch
     * @return the corresponding switch
     * @throws NullPointerException if var is null
     * @throws IllegalArgumentException if var does not match a configured
     *          switch
     */
    public MockOpenflowSwitch findSwitch(String var) {
        notNull(var);
        return ((BankDefReader) reader).getSwitchByVar(var);
    }

    // ===================================================================

    /** Encapsulates a binding of switch variable name to
     * definition filename.
     */
    public static class SwitchInfo {
        private final String var;
        private final String fname;
        private MockOpenflowSwitch sw;

        /** Constructs the binding.
         *
         * @param var the variable name
         * @param fname the base filename
         */
        public SwitchInfo(String var, String fname) {
            this.var = var;
            this.fname = fname;
        }

        @Override
        public String toString() {
            return var + " (" + fname + ") " + sw;
        }

        /** Returns the variable name.
         *
         * @return the variable name
         */
        public String getVar() {
            return var;
        }

        /** Returns the switch definition file base name.
         *
         * @return the file base name
         */
        public String getFname() {
            return fname;
        }

        /** Sets the mock-switch instance on this info.
         *
         * @param sw the switch instance
         */
        void setSwitch(MockOpenflowSwitch sw) {
            this.sw = sw;
        }

        /** Returns the mock switch instance.
         *
         * @return the mock switch
         */
        public MockOpenflowSwitch getSwitch() {
            return sw;
        }
    }

    // ===================================================================
    private static final Pattern RE_SECT = compile("\\[(\\w+)\\]");
    private static final Pattern RE_SW = compile("(\\w+)\\s*=\\s*([\\w\\./]+)");
    private static final Pattern RE_CMD = compile("(\\w+)\\s+(.*)\\s*");
    private static final Pattern RE_DIRECTIVE = compile("(\\w+)\\s*");

    private static enum Section {
        PREAMBLE,
        SWITCHES,
        SCENARIO,
        ;

        /** Returns the section constant that matches the given
         * lowercase name. If there is no match, null is returned.
         *
         * @param name the name to match
         * @return the corresponding constant
         */
        public static Section get(String name) {
            Section section = null;
            for (Section s: values())
                if (toCamelCase(s).equals(name)) {
                    section = s;
                    break;
                }
            return section;
        }
    }

    /** Implementation of a reader for the bank definition file. */
    private static class BankDefReader extends AbstractDefReader {

        private Map<String, SwitchInfo> switchMap;
        private List<SwitchInfo> switches;
        private List<ScenarioCommand> cmds;

        Section currentSection = Section.PREAMBLE;

        /**
         * Constructs the switch bank definition reader.
         *
         * @param path the path of the file
         */
        protected BankDefReader(String path) {
            super(path);
        }

        @Override
        protected void initStorage() {
            switchMap = new TreeMap<String, SwitchInfo>();
            switches = new ArrayList<SwitchInfo>();
            cmds = new ArrayList<ScenarioCommand>();
        }

        @Override
        protected String parseLogicalLine(LogicalLine line) {
            String text = line.getText();
            Matcher m = RE_SECT.matcher(text);
            if (m.matches()) {
                // we are changing sections
                String sectionName = m.group(1);
                Section s = Section.get(sectionName);
                if (s == null)
                    return E_UNK_SECT + sectionName + DQ;
                currentSection = s;
                return null;
            }
            
            // parsing inside a section
            if (currentSection == Section.SWITCHES) {
                m = RE_SW.matcher(text);
                if (m.matches())
                    return cacheSwitchInfo(m.group(1), m.group(2));
                return E_BAD_SW + text + DQ;
                
            } else if (currentSection == Section.SCENARIO) {
                m = RE_CMD.matcher(text);
                if (m.matches()) {
                    // create scenario command
                    String cmd = m.group(1);
                    String params = m.group(2);
                    CommandType cmdType = CommandType.get(cmd);
                    if (cmdType == null)
                        return E_UNK_CMD + cmd + DQ;
                    return cacheCommand(cmdType, params);
                }
                
                m = RE_DIRECTIVE.matcher(text);
                if (m.matches()) {
                    // create scenario directive
                    String cmd = m.group(1);
                    CommandType cmdType = CommandType.get(cmd);
                    if (cmdType == null)
                        return E_UNK_CMD + cmd + DQ;
                    return cacheCommand(cmdType, null);
                }
                
                return E_BAD_CMD_FMT + text + DQ;
            }
            return E_INV_FMT + text + DQ;
        }

        private String cacheCommand(CommandType cmdType, String params) {
            ScenarioCommand cmd = ScenarioCommand.create(cmdType, params);
            cmds.add(cmd);
            // TODO: pass 'this' into validate, so the command can check
            //  against the rest of the bank definition (eg. switch variable)
            return cmd.validate();
        }

        private String cacheSwitchInfo(String var, String fname) {
            SwitchInfo already = switchMap.get(var);
            if (already != null)
                return E_DUP_SWVAR + var + DQ;
            SwitchInfo si = new SwitchInfo(var, fname);
            switches.add(si);
            switchMap.put(var, si);
            return null;
        }

        /** Returns the switch associated with the specified variable.
         *
         * @param var the variable name assigned to the switch
         * @return the associated switch
         */
        MockOpenflowSwitch getSwitchByVar(String var) {
            SwitchInfo swinfo = switchMap.get(var);
            if (swinfo == null)
                throw new IllegalArgumentException(E_NO_SWITCH + var);
            return swinfo.getSwitch();
        }

        private static final String E_UNK_SECT = RES.getString("e_unk_sect");
        private static final String E_BAD_SW = RES.getString("e_bad_sw");
        private static final String E_DUP_SWVAR = RES.getString("e_dup_swvar");
        private static final String E_BAD_CMD_FMT = RES
                .getString("e_bad_cmd_fmt");
        private static final String E_UNK_CMD = RES.getString("e_unk_cmd");
        private static final String E_INV_FMT = RES.getString("e_inv_fmt");
        private static final String DQ = "\"";
    }
}