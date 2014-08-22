/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch.misc;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.TallyMap;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.packet.Codec;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolException;

import java.io.File;
import java.nio.BufferUnderflowException;
import java.util.*;

import static org.opendaylight.of.lib.dt.BufferId.NO_BUFFER;
import static org.opendaylight.util.Log.stackTraceSnippet;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * Reads a Wireshark Packet Details file (PDML.xml) and grabs Openflow messages
 * from it.
 *
 * @author Simon Hunt
 */
public class SharkOpenflow {

    private static final String PDML = "pdml";
    private static final String PACKET = "packet";
    private static final String PROTO = "proto";
    private static final String FIELD = "field";

    private static final String NAME = "[@name]";
    private static final String VALUE = "[@value]";

    private static final String FRAME = "frame";
    private static final String IP = "ip";
    private static final String TCP = "tcp";
    private static final String FFW = "fake-field-wrapper";

    private static final String FRAME_NUMBER = "frame.number";
    private static final String FRAME_TIME_RELATIVE = "frame.time_relative";
    private static final String SHOW = "[@show]";
    private static final String IP_SRC = "ip.src";
    private static final String TCP_SRCPORT = "tcp.srcport";
    private static final String DATA = "data";

    private static final int CONTROLLER_PORT = 6633;
    private static final String FAILED_PARSE_TAG = "<<MPE>> ";

    private final String path;
    private final List<SharkPacket> sharkPackets = new ArrayList<>();
    private final TallyMap<MessageType> msgTally = new TallyMap<>();
    private final TallyMap<ErrorType> errTally = new TallyMap<>();
    private final TallyMap<MultipartType> mpReqTally = new TallyMap<>();
    private final TallyMap<MultipartType> mpRepTally = new TallyMap<>();

    private int packetCount;
    private int ofmCount;
    private int parseFailCount = 0;

    // FIXME: need to parameterize this on the command line
    private static final Integer[] detailFor = {};
    private static final Set<Integer> detailSet =
            new HashSet<>(Arrays.asList(detailFor));

    /**
     * Reads in a a packet details XML file and builds a model of openflow
     * messages contained within.
     *
     * @param path the path of the XML file
     */
    public SharkOpenflow(String path) {
        this.path = path;
        File f = new File(path);
        XMLConfiguration conf = config();
        try {
            conf.load(f);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Unable to load XML", e);
        }
        parseXml(conf);
    }

    private void parseXml(XMLConfiguration conf) {
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> packets = conf.configurationsAt(PACKET);
        packetCount = packets.size();
        for (HierarchicalConfiguration pkt: packets)
            sharkPackets.add(parsePacket(pkt));
    }

    private SharkPacket parsePacket(HierarchicalConfiguration pkt) {
        SharkPacket sp = new SharkPacket();
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> protos = pkt.configurationsAt(PROTO);
        for (HierarchicalConfiguration pro: protos)
            parseProto(sp, pro);
        sp.tallyMessages();
        return sp;
    }

    private void parseProto(SharkPacket sp, HierarchicalConfiguration pro) {
        String name = pro.getString(NAME);
        switch (name) {
            case FRAME:
                sp.parseFrame(pro);
                break;
            case IP:
                sp.parseIp(pro);
                break;
            case TCP:
                sp.parseTcp(pro);
                break;
            case FFW:
                sp.parseData(pro);
                break;
            default:
                break;
        }
    }

    private XMLConfiguration config() {
        XMLConfiguration conf = new XMLConfiguration();
        conf.setRootElementName(PDML);
        conf.setAttributeSplittingDisabled(true);
        conf.setDelimiterParsingDisabled(true);
        return conf;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{SharkOpenflow: path=");
        sb.append(path).append("}");
        sb.append(EOL).append("Packets read in: ").append(packetCount);
        sb.append(EOL).append("OpenFlow Messages: ").append(ofmCount);
        sb.append(EOL).append("Failed Parses: ").append(FAILED_PARSE_TAG)
                .append(parseFailCount);
        sb.append(EOL).append("Msgs: ").append(msgTally);
        sb.append(EOL).append("  Errors: ").append(errTally);
        sb.append(EOL).append("  MP/Requests: ").append(mpReqTally);
        sb.append(EOL).append("  MP/Replies: ").append(mpRepTally);
        sb.append(EOL).append("---");

        for (SharkPacket sp: sharkPackets) {
            boolean detailRequired = detailSet.contains(sp.frameNumber());
            sb.append(EOL).append(sp.displayString(detailRequired));
        }

        sb.append(EOL).append("---");
        return  sb.toString();
    }


    private static boolean isControllerPort(int port) {
        return port == CONTROLLER_PORT;
    }

    //======================================================================

    private static final int PREAMBLE = 45;
    private static final String PREAMBLE_SPACER = StringUtils.spaces(PREAMBLE);
    private static final String MORE_SPACE = "    ";

    private class SharkPacket {
        Frame frame = new Frame();
        Ip ip = new Ip();
        Tcp tcp = new Tcp();
        Data data = new Data();

        public int frameNumber() {
            return frame.number;
        }

        @Override
        public String toString() {
            return displayString(false);
        }

        public String displayString(boolean detailRequired) {
            StringBuilder sb = new StringBuilder();
            sb.append(frame);
            sb.append(ip);
            sb.append(tcp);
            int padding = PREAMBLE - sb.length();
            sb.append(StringUtils.spaces(Math.max(padding, 1)));
            sb.append(data.displayString(detailRequired));
            return sb.toString();
        }

        public void parseFrame(HierarchicalConfiguration pro) {
            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> fields = pro.configurationsAt(FIELD);
            for (HierarchicalConfiguration f: fields) {
                String name = f.getString(NAME);
                if (FRAME_NUMBER.equals(name))
                    frame.number = f.getInt(SHOW);
                else if (FRAME_TIME_RELATIVE.equals(name))
                    frame.timeRelative = f.getString(SHOW);
            }
        }

        public void parseIp(HierarchicalConfiguration pro) {
            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> fields = pro.configurationsAt(FIELD);
            for (HierarchicalConfiguration f: fields) {
                String name = f.getString(NAME);
                if (IP_SRC.equals(name))
                    ip.ipSrc = getIpAddress(f.getString(SHOW));
            }
        }

        private IpAddress getIpAddress(String s) {
            try {
                return IpAddress.valueOf(s);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public void parseTcp(HierarchicalConfiguration pro) {
            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> fields = pro.configurationsAt(FIELD);
            for (HierarchicalConfiguration f: fields) {
                String name = f.getString(NAME);
                if (TCP_SRCPORT.equals(name))
                    tcp.srcPort = f.getInt(SHOW);
            }
        }

        public void parseData(HierarchicalConfiguration pro) {
            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> fields = pro.configurationsAt(FIELD);
            for (HierarchicalConfiguration f: fields) {
                String name = f.getString(NAME);
                if (DATA.equals(name))
                    data.parse(f.getString(VALUE));
            }
        }

        public void tallyMessages() {
            for (OpenflowMessage msg: data.msgs.values()) {
                MessageType mt = msg.getType();
                msgTally.inc(mt);
                if (mt == MessageType.ERROR) {
                    OfmError err = (OfmError) msg;
                    errTally.inc(err.getErrorType());
                } else if (mt == MessageType.MULTIPART_REQUEST) {
                    OfmMultipartRequest req = (OfmMultipartRequest) msg;
                    mpReqTally.inc(req.getMultipartType());
                } else if (mt == MessageType.MULTIPART_REPLY) {
                    OfmMultipartReply rep = (OfmMultipartReply) msg;
                    mpRepTally.inc(rep.getMultipartType());
                }
            }
            ofmCount += data.msgs.size();
            parseFailCount += data.mpes.size();
        }
    }

    private static class Frame {
        int number;
        String timeRelative;

        @Override
        public String toString() {
            return "[" + number + "] <" + timeRelative + "> ";
        }
    }

    private static class Ip {
        IpAddress ipSrc;

        @Override
        public String toString() {
            return ipSrc + " ";
        }
    }

    private static class Tcp {
        int srcPort;

        @Override
        public String toString() {
            return isControllerPort(srcPort) ? "(controller) " : "(mockswitch) ";
        }
    }

    private static class Data {
        int msgCount = 0;
        Map<Integer, OpenflowMessage> msgs = new TreeMap<>();
        Map<Integer, MessageParseException> mpes = new TreeMap<>();
        String ofmsgs = "*";

        public String displayString(boolean detailRequired) {
            return detailRequired ? detail() : summary();
        }

        private String summary() {
            return ofmsgs;
        }

        private String detail() {
            // produce detail string on demand
            List<String> results = new ArrayList<>(msgCount);
            String indent = "";
            for (int i=0; i<msgCount; i++) {
                OpenflowMessage m = msgs.get(i);
                if (m != null) {
                    results.add(indent + detailFor(m));

                } else {
                    Throwable t = mpes.get(i);
                    if (t != null)
                        results.add(indent + stackTraceSnippet(t));
                    else
                        results.add(NEITHER_MSG_NOR_MPE);
                }
                indent = EOL + PREAMBLE_SPACER;
            }
            return StringUtils.join(results, "");
        }

        private String detailFor(OpenflowMessage m) {
            StringBuilder sb = new StringBuilder(m.toDebugString());
            switch (m.getType()) {
                case PACKET_IN:
                    sb.append(EOL).append(packetInDetail((OfmPacketIn) m));
                    break;
                case PACKET_OUT:
                    sb.append(EOL).append(packetOutDetail((OfmPacketOut) m));
                    break;
                default:
                    break;
            }
            return sb.toString();
        }

        private String packetInDetail(OfmPacketIn pi) {
            return packetDetail(pi.getData(), pi.getBufferId());
        }

        private String packetOutDetail(OfmPacketOut po) {
            return packetDetail(po.getData(), po.getBufferId());
        }

        private String packetDetail(byte[] data, BufferId bufferId) {
            Packet pkt = decodePacket(data, bufferId);
            return pkt == null ? UNABLE_TO_DECODE_PACKET : pkt.toDebugString();
        }

        private Packet decodePacket(byte[] data, BufferId bufferId) {
            Packet pkt = null;
            try {
                pkt = Codec.decodeEthernet(data);
            } catch (ProtocolException e) {
                if (!NO_BUFFER.equals(bufferId)
                        && e.packet() != null
                        && e.rootCause() instanceof BufferUnderflowException)
                    pkt = e.packet();
            }
            return pkt;
        }

        private static final String UNABLE_TO_DECODE_PACKET =
                "(Unable to decode packet data)";
        private static final String NEITHER_MSG_NOR_MPE = "(no msg nor mpe?!)";

        @Override
        public String toString() {
            return summary();
        }

        // return true if parse was successful
        public void parse(String hex) {
            byte[] bytes = ByteUtils.parseHex(hex);

            // implementation note: there may be more than one openflow message
            // in the data. We need to parse them all..
            OfPacketReader pkt = new OfPacketReader(bytes);
            List<String> results = new ArrayList<>();
            OpenflowMessage m;
            String indent = "";
            while (pkt.readableBytes() > 0) {
                try {
                    m = MessageFactory.parseMessage(pkt);
                    msgs.put(msgCount, m);
                    results.add(indent + summaryFor(m));
                } catch (MessageParseException e) {
                    mpes.put(msgCount, e);
                    results.add(indent + FAILED_PARSE_TAG + e.toString());
                }
                msgCount++;
                indent = EOL + PREAMBLE_SPACER;
            }
            ofmsgs = StringUtils.join(results, "");
        }

        private String summaryFor(OpenflowMessage m) {
            StringBuilder sb = new StringBuilder(m.toString());
            switch (m.getType()) {
                case PACKET_IN:
                    sb.append(EOL).append(PREAMBLE_SPACER).append(MORE_SPACE)
                            .append(packetInSummary((OfmPacketIn) m));
                    break;
                case PACKET_OUT:
                    sb.append(EOL).append(PREAMBLE_SPACER).append(MORE_SPACE)
                            .append(packetOutSummary((OfmPacketOut) m));
                    break;
                default:
                    break;
            }
            return sb.toString();
        }

        private String packetInSummary(OfmPacketIn pi) {
            return packetSummary(pi.getData(), pi.getBufferId());
        }

        private String packetOutSummary(OfmPacketOut po) {
            return packetSummary(po.getData(), po.getBufferId());
        }

        private String packetSummary(byte[] data, BufferId bufferId) {
            Packet pkt = decodePacket(data, bufferId);
            return pkt == null ? UNABLE_TO_DECODE_PACKET
                               : pkt.protocolIds().toString();
        }
    }
}
