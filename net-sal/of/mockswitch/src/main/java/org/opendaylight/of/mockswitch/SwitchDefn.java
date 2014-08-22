/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.of.lib.msg.PortConfig;
import org.opendaylight.of.lib.msg.PortFeature;
import org.opendaylight.of.lib.msg.SupportedAction;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacPrefix;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a mock-switch definition file and encapsulates the configuration.
 *
 * @author Simon Hunt
 */
public class SwitchDefn extends AbstractDefn {

    private DataPathId dpid;
    private CfgHello cfgHello;
    private CfgBase cfgBase = new CfgBase();
    private CfgFeat cfgFeat = new CfgFeat();
    private CfgDesc cfgDesc = new CfgDesc();

    /** Constructs a switch definition, from the specified definition file.
     *
     * @param path the definition file path name
     */
    public SwitchDefn(String path) {
        super(new SwitchDefReader(path));
        processDirectives();
    }


    private void processDirectives() {
        for (Directive d: ((SwitchDefReader)reader).directives) {
            switch (d.key) {
                case DPID:
                    dpid = DataPathId.valueOf(d.data);
                    break;
                case HELLO:
                    cfgHello = createHello(d.data);
                    break;
                case CONTROLLER:
                    setControllerParams(d.data);
                    break;
                case BUFFERS:
                    setBuffers(d.data);
                    break;
                case TABLES:
                    setTables(d.data);
                    break;
                case CAPABILITIES:
                    setCapabilities(d.data);
                    break;
                case SUPPORTED_ACTIONS:
                    setSupportedActions(d.data);
                    break;
                case PORT_COUNT:
                    setPortCount(d.data);
                    break;
                case PORT_MAC:
                    setPortMac(d.data);
                    break;
                case PORT_CONFIG:
                    setPortConfig(d);
                    break;
                case PORT_FEATURE:
                    setPortFeature(d);
                    break;

                case DESC_MFR:
                case DESC_HW:
                case DESC_SW:
                case DESC_SERIAL:
                case DESC_DP:
                    cfgDesc.setDesc(d.key, d.data);
                    break;
            }
        }
    }


    private static final String RE_SPACES = "\\s+";
    private static final String EAGER = "EAGER";
    private static final String LAZY = "LAZY";
    private static final String LEGACY = "LEGACY";

    private CfgHello createHello(String data) {
        String[] bits = data.split(RE_SPACES);
        Set<ProtocolVersion> vers = new HashSet<ProtocolVersion>();
        boolean eager = false;
        boolean legacy = false;
        for (String s: bits) {
            if (s.equals(LAZY)) {
                eager = false;
            } else if (s.equals(EAGER)) {
                eager = true;
            } else if (s.equals(LEGACY)) {
                legacy = true;
            } else {
                vers.add(ProtocolVersion.valueOf(s));
            }
        }

        CfgHello h = new CfgHello((eager ? CfgHello.Behavior.EAGER
                : CfgHello.Behavior.LAZY),
                vers.toArray(new ProtocolVersion[vers.size()]));
        if (legacy)
            h.legacy();
        return h;
    }

    private void setControllerParams(String data) {
        String[] bits = data.split(RE_SPACES);
        IpAddress addr = IpAddress.valueOf(bits[0]);
        int port = Integer.parseInt(bits[1]);
        if (bits.length > 2) {
            int tlsPort = Integer.parseInt(bits[2]);
            cfgBase.setOpenflowTlsPort(tlsPort);
        }
        if (bits.length > 3) {
            int udpPort = Integer.parseInt(bits[3]);
            cfgBase.setOpenflowUdpPort(udpPort);
        }
        cfgBase.setControllerAddress(addr);
        cfgBase.setOpenflowPort(port);
    }

    private void setBuffers(String data) {
        cfgBase.setBufferCount(Integer.parseInt(data));
    }

    private void setTables(String data) {
        cfgBase.setTableCount(Integer.parseInt(data));
    }

    private void setCapabilities(String data) {
        Set<Capability> caps = new HashSet<Capability>();
        String[] bits = data.split(RE_SPACES);
        for (String s: bits)
            caps.add(Capability.valueOf(s));
        cfgBase.setCapabilities(caps);
    }

    private void setSupportedActions(String data) {
        Set<SupportedAction> suppActs = new HashSet<SupportedAction>();
        String[] bits = data.split(RE_SPACES);
        for (String s: bits)
            suppActs.add(SupportedAction.valueOf(s));
        cfgFeat.setSuppActs(suppActs);
    }

    private void setPortCount(String data) {
        cfgFeat.setPortCount(Integer.parseInt(data));
    }

    private void setPortMac(String data) {
        if (!data.endsWith(":*"))
            throw new IllegalArgumentException("Bad Wildcard MAC - " +
                    "doesn't end in \":*\"");
        int len = data.length();
        MacPrefix pfx = MacPrefix.valueOf(data.substring(0, len-2));
        cfgFeat.setPortMac(pfx);
    }

    private void setPortConfig(Directive d) {
        if (d.index == NO_INDEX) {
            cfgFeat.setPortConfig(parsePortConfig(d.data));
        } else {
            cfgFeat.overridePortConfig(d.index, parsePortConfig(d.data));
        }
    }

    private Set<PortConfig> parsePortConfig(String data) {
        Set<PortConfig> pc = new HashSet<PortConfig>();
        if (data.length() > 0) {
            String[] bits = data.split(RE_SPACES);
            for (String s: bits)
                pc.add(PortConfig.valueOf(s));
        }
        return pc;
    }

    private void setPortFeature(Directive d) {
        if (d.index == NO_INDEX) {
            cfgFeat.setPortFeatures(parsePortFeatures(d.data));
        } else {
            cfgFeat.overridePortFeatures(d.index, parsePortFeatures(d.data));
        }
    }

    private Set<PortFeature> parsePortFeatures(String data) {
        Set<PortFeature> pf = new HashSet<PortFeature>();
        if (data.length() > 0) {
            String[] bits = data.split(RE_SPACES);
            for (String s: bits)
                pf.add(PortFeature.valueOf(s));
        }
        return pf;
    }

// ====================================================================
// GETTERS

    /** Returns a multi-line string representation of this parsed
     * switch definition.
     *
     * @return a multi-line string representation
     */
    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        final String eoli = StringUtils.EOL + "  ";
        final String dash = eoli + "---";

        for (Directive d: ((SwitchDefReader)reader).directives)
            sb.append(eoli).append(d);
        sb.append(dash);
        List<CustomProp> props = getCustomProps();
        if (props.size() > 0) {
            sb.append(" Custom Properties ---");
            for (CustomProp p: props)
                sb.append(eoli).append(p);
            sb.append(dash);
        }
        return sb.toString();
    }

    /** Returns the datapath id.
     *
     * @return the datapath id
     */
    public DataPathId getDpid() {
        return dpid;
    }

    /** Returns the hello config object.
     *
     * @return the hello config
     */
    public CfgHello getCfgHello() {
        return cfgHello;
    }

    /** Returns the base config object.
     *
     * @return the base config
     */
    public CfgBase getCfgBase() {
        return cfgBase;
    }

    /** Returns the features config object.
     *
     * @return the features config
     */
    public CfgFeat getCfgFeat() {
        return cfgFeat;
    }

    /** Returns the description config object.
     *
     * @return the description config
     */
    public CfgDesc getCfgDesc() {
        return cfgDesc;
    }

    /** Returns an unmodifiable view of the custom properties.
     *
     * @return the custom properties
     */
    public List<CustomProp> getCustomProps() {
        return Collections.unmodifiableList(((SwitchDefReader) reader).props);
    }

// ====================================================================

    /** Represents a property from the "custom" section of the
     * mock switch definition file.
     */
    public static class CustomProp {
        private final String name;
        private final String value;

        private CustomProp(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "{" + name + ": " + value + "}";
        }

        /** Returns the name of this property.
         *
         * @return the name
         */
        public String name() {
            return name;
        }

        /** Returns the value of this property.
         *
         * @return the value
         */
        public String value() {
            return value;
        }
    }

// ====================================================================

    private static final Pattern DIRECTIVE =
            Pattern.compile("^(\\w+):\\s*(.*)");
    private static final Pattern INDEXED_DIRECTIVE =
            Pattern.compile("^(\\w+)\\[(\\d+)\\]:\\s*(.*)");
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            SwitchDefn.class, "switchDefn");

    private static final String E_INV_FMT = RES.getString("e_inv_fmt");
    private static final String E_UNK_KEY = RES.getString("e_unk_key");
    private static final String E_KEY_NON_IDX = RES.getString("e_key_non_idx");
    private static final String E_BAD_IDX = RES.getString("e_bad_idx");
    private static final int MAX_IDX = 255;

    private static final String DQ = "\"";
    private static final int NO_INDEX = -1;
    private static final String CUSTOM = "[custom]";

    /** Interprets a definition file as a bunch of directives. */
    private static class SwitchDefReader extends AbstractDefReader {

        private List<Directive> directives;
        private List<CustomProp> props;
        private boolean customSection = false;

        protected SwitchDefReader(String path) {
            super(path);
        }

        @Override
        protected void initStorage() {
            directives = new ArrayList<Directive>();
            props = new ArrayList<CustomProp>();
        }

        @Override
        protected String parseLogicalLine(LogicalLine line) {
            String key;
            String data;
            String idxStr;
            int idx;
            Matcher m = DIRECTIVE.matcher(line.getText());

            // if in custom section, treat everything as a custom property
            if (customSection) {
                if (m.matches()) {
                    props.add(new CustomProp(m.group(1), m.group(2)));
                    return null;
                }
                return E_INV_FMT + line.getText() + DQ;
            }

            // test to see if we are entering the custom section
            if (line.getText().equals(CUSTOM)) {
                customSection = true;
                return null;
            }

            if (m.matches()) {
                // interpret as a directive
                key = m.group(1);
                data = m.group(2);
                Keyword k = Keyword.get(key);
                if (k == null)
                    return E_UNK_KEY + key + DQ;
                directives.add(new Directive(k, NO_INDEX, data));
                return null;
            }
            
            m = INDEXED_DIRECTIVE.matcher(line.getText());
            if (m.matches()) {
                // interpret as an indexed directive
                key = m.group(1);
                idxStr = m.group(2);
                data = m.group(3);
                try {
                    idx = Integer.parseInt(idxStr);
                    if (idx < 0 || idx > MAX_IDX)
                        return E_BAD_IDX + idxStr + DQ;
                } catch (NumberFormatException nfe) {
                    return E_BAD_IDX + idxStr + DQ;
                }
                Keyword k = Keyword.get(key);
                if (k == null)
                    return E_UNK_KEY + key + DQ;
                else if (idx != NO_INDEX && !k.indexable)
                    return E_KEY_NON_IDX + key + DQ;
                directives.add(new Directive(k, idx, data));
                return null;
            }
            return E_INV_FMT + line.getText() + DQ;
        }
    }

    // ===================================================================
    static enum Keyword {
        DPID,
        HELLO,
        CONTROLLER,
        BUFFERS,
        TABLES,
        CAPABILITIES,
        SUPPORTED_ACTIONS,
        PORT_COUNT,
        PORT_MAC,
        PORT_CONFIG(true),
        PORT_FEATURE(true),
        DESC_MFR,
        DESC_HW,
        DESC_SW,
        DESC_SERIAL,
        DESC_DP,
        ;

        private final boolean indexable;

        Keyword() {
            indexable = false;
        }

        Keyword(boolean b) {
            indexable = b;
        }

        public static Keyword get(String s) {
            String fcc = StringUtils.fromCamelCase(s);
            for (Keyword k: values())
                if (k.name().equals(fcc))
                    return k;
            return null;
        }

        @Override
        public String toString() {
            return StringUtils.toCamelCase(this);
        }
    }

    // ====================================================================
    private static class Directive {
        private final Keyword key;
        private final int index;
        private final String data;

        private Directive(Keyword key, int index, String data) {
            this.key = key;
            this.index = index;
            this.data = data;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(key.toString());
            if (index >= 0)
                sb.append("[").append(index).append("]");
            sb.append(": ").append(data);
            return sb.toString();
        }
    }
}