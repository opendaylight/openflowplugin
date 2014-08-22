/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch.misc;

import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.of.mockswitch.AbstractDefReader;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.opendaylight.util.StringUtils.*;

/**
 * Utility class to read and decode openflow messages from a text version of
 * a Wireshark packet capture.
 *
 * @author Simon Hunt
 */
public class SharkReader {

    private static final String FILE_EXT = ".wshex";
    private static final String EOLI = EOL + "  ";
    private static final String EOLI_DASH = EOLI + "---";

    private SharkFileParser parser;

    /**
     * Creates a reader that parses the specifieid wshex file.
     *
     * @param path path of the wshex file
     */
    public SharkReader(String path) {
        parser = new SharkFileParser(path);
    }

    /**
     * Returns a multi-line string representation of this reader.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOL).append("Sources:");
        for (Source s: getSources())
            sb.append(EOLI).append(s);
        sb.append(EOL).append("Frames:");
        for (Frame f: getFrames())
            sb.append(EOLI).append(f);
        sb.append(EOLI_DASH);
        return sb.toString();
    }

    /**
     * Returns an unmodifiable view of the source bindings.
     *
     * @return the source bindings
     */
    public List<Source> getSources() {
        return Collections.unmodifiableList(parser.sources);
    }

    /**
     * Returns an unmodifiable view of the frames.
     *
     * @return the frames
     */
    public List<Frame> getFrames() {
        return Collections.unmodifiableList(parser.frames);
    }

    // =======================================

    private static enum Section {
        PREAMBLE,
        SOURCES,
        FRAMES,
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


    private static final Pattern RE_SECT = compile("\\[(\\w+)\\]");
    private static final Pattern RE_ID = compile("(\\w+)\\s*=\\s*([\\w\\./]+)");
    private static final Pattern RE_FRAME =
            compile("(\\d+)\\s+([\\d\\.]+)\\s+(\\w+)\\s+([\\*0-9a-fA-F]+)\\s*");

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            SharkReader.class, "sharkReader");

    private static final String E_UNK_SECT = RES.getString("e_unk_sect");
    private static final String E_BAD_SW = RES.getString("e_bad_sw");
    private static final String E_DUP_SRCVAR = RES.getString("e_dup_srcvar");
    private static final String E_NO_SUCH_SOURCE = RES
            .getString("e_no_such_source");
    private static final String E_BAD_FRAME_FMT = RES
            .getString("e_bad_frame_fmt");
    private static final String E_INV_FMT = RES.getString("e_inv_fmt");
    private static final String DQ = "\"";

    /**
     * Parses an openflow shark capture file (.ofshark)
     */
    private static class SharkFileParser extends AbstractDefReader {
        private Map<String, Source> sourceLookup;
        private List<Source> sources;
        private List<Frame> frames;

        Section currentSection = Section.PREAMBLE;

        protected SharkFileParser(String path) {
            super(path);
        }

        @Override
        protected void initStorage() {
            sourceLookup = new TreeMap<String, Source>();
            sources = new ArrayList<Source>();
            frames = new ArrayList<Frame>();
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
            if (currentSection == Section.SOURCES) {
                m = RE_ID.matcher(text);
                if (m.matches())
                    return cacheSource(m.group(1), m.group(2));
                return E_BAD_SW + text + DQ;

            } else if (currentSection == Section.FRAMES) {
                m = RE_FRAME.matcher(text);
                if (m.matches()) {
                    // create frame
                    String frameId = m.group(1);
                    String time = m.group(2);
                    String sourceId = m.group(3);
                    String hex = m.group(4);

                    return cacheFrame(frameId, time, sourceId, hex);
                }
                return E_BAD_FRAME_FMT + text + DQ;
            }
            return E_INV_FMT + text + DQ;
        }

        private String cacheSource(String var, String label) {
            Source s = sourceLookup.get(var);
            if (s != null)
                return E_DUP_SRCVAR + var + DQ;
            s = new Source(var, label);
            sources.add(s);
            sourceLookup.put(var, s);
            return null;
        }

        private String cacheFrame(String frameId, String time, String sourceId,
                                  String hex) {
            Source src = sourceLookup.get(sourceId);
            if (src == null)
                return E_NO_SUCH_SOURCE + sourceId + DQ;

            Frame f = new Frame(frameId, time, src, hex);
            frames.add(f);
            return null;
        }
    }

    /**
     * Represents a "source" binding of identifier and label.
     */
    private static class Source {
        private final String var;
        private final String label;

        public Source(String var, String label) {
            this.var = var;
            this.label = label;
        }

        @Override
        public String toString() {
            return var + " (" + label + ")";
        }
    }

    private static final String STAR = "*";
    private static final String MARKER = "<<<>>> ";

    /**
     * Represents a "packet capture" frame from the shark hex file.
     */
    private static class Frame {
        private final String id;
        private final String time;
        private final Source src;
        private final String ofMsgHex;

        private final OpenflowMessage msg;
        private final MessageParseException mpe;
        private final String msgStr;

        public Frame(String id, String time, Source src, String ofMsgHex) {
            this.id = id;
            this.time = time;
            this.src = src;
            this.ofMsgHex = ofMsgHex;

            OpenflowMessage msg = null;
            MessageParseException mpe = null;
            String msgStr;

            if (STAR.equals(ofMsgHex)) {
                msgStr = STAR;
            } else {
                try {
                    msg = MessageFactory.parseMessage(pktRdr(ofMsgHex));
                    msgStr = msg.toString();
                } catch (MessageParseException e) {
                    mpe = e;
                    msgStr = MARKER + e.getMessage();
                }
            }
            this.msg = msg;
            this.mpe = mpe;
            this.msgStr = msgStr;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(align(id, 3)).append("] ")
                    .append(align(time, 12)).append("   ")
                    .append(src.var).append("   ")
                    .append(msgStr);
            return sb.toString();
        }

        private String align(Object what, int width) {
            return StringUtils.pad(what.toString(), width, Align.RIGHT);
        }

        private OfPacketReader pktRdr(String hex) {
            return new OfPacketReader(ByteUtils.parseHex(hex));
        }

    }
}