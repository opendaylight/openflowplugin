/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.json.AbstractJsonCodec;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.util.TimeUtils.rfc822Timestamp;

/**
 * A JSON codec for {@link DataPathInfo}. Requires {@link PortCodec} as a
 * pre-requisite.
 *
 * @author Liem Nguyen
 * @author Narasimha Reddy Vaka
 * @author Simon Hunt
 */
public class DataPathInfoCodec extends AbstractJsonCodec<DataPathInfo> {

    private static final String EMPTY = "";

    private static final String DEVICE_PORT = "device_port";
    private static final String DEVICE_IP = "device_ip";
    private static final String CAPS = "capabilities";
    private static final String NUM_TABLES = "num_tables";
    private static final String NUM_BUFFERS = "num_buffers";
    private static final String LAST_MESSAGE = "last_message";
    private static final String READY = "ready";
    private static final String NEGOTIATED_VERSION = "negotiated_version";
    private static final String DPID = "dpid";
    private static final String MFR = "mfr";
    private static final String HW = "hw";
    private static final String SW = "sw";
    private static final String SERIAL = "serial";
    private static final String DESC = "desc";
    
    static final String ROOTS = "datapaths";
    static final String ROOT = "datapath";

    /**
     * Constructs a DataPathInfo Codec.
     */
    protected DataPathInfoCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(DataPathInfo dpi) {
        ObjectNode node = objectNode();
        node.put(DPID, dpi.dpid().toString());
        node.put(NEGOTIATED_VERSION, dpi.negotiated().toDisplayString());
        node.put(READY, rfc822Timestamp(dpi.readyAt()));
        node.put(LAST_MESSAGE, rfc822Timestamp(dpi.lastMessageAt()));

        node.put(NUM_BUFFERS, dpi.numBuffers());
        node.put(NUM_TABLES, dpi.numTables());

        node.put(MFR, dpi.manufacturerDescription());
        node.put(HW, dpi.hardwareDescription());
        node.put(SW, dpi.softwareDescription());
        node.put(SERIAL, dpi.serialNumber());
        node.put(DESC, dpi.datapathDescription());
        
        // Remote switch's IP and port
        if (dpi.remoteAddress() != null)
            node.put(DEVICE_IP, dpi.remoteAddress().toShortString());
        if (dpi.remotePort() != null)
            node.put(DEVICE_PORT, dpi.remotePort().toInt());

        Set<Capability> caps = dpi.capabilities();
        if (caps != null)
            node.put(CAPS, fromEnums(caps));

        return node;
    }

    // TODO - move these utility methods to a superclass.

    /**
     * Returns the {@link ObjectNode#textValue()} of a property on the given
     * object node for the given key. If the property does not exist, an
     * empty string is returned.
     *
     * @param node the object node
     * @param key the property key
     * @return the text value of the property (or empty string)
     */
    protected String getText(ObjectNode node, String key) {
        JsonNode jn = node.get(key);
        return jn == null ? EMPTY : jn.textValue();
    }

    /**
     * Reads the property with the given key from the given object node and
     * attempts to interpret it as an RFC822 timestamp, returning a value
     * representing the number of milliseconds since
     * January 1, 1970, 00:00:00 GMT.
     *
     * @param node the object node
     * @param key the property key
     * @return epoch timestamp value
     * @throws IllegalArgumentException if the value is not correctly formatted
     * @see java.util.Date#getTime()
     */
    protected long getTime(ObjectNode node, String key) {
        String tstr = getText(node, key);
        try {
            return rfc822Timestamp(tstr).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException(E_BAD_TS + tstr);
        }
    }

    private static final String E_BAD_TS = "Badly formatted timestamp: ";

    /**
     * Reads the property with the given key from the given object node and
     * attempts to interpret it as an OpenFlow protocol version.
     *
     * @param node the object node
     * @param key the property key
     * @return protocol version
     * @throws IllegalArgumentException if the value is not recognized
     */
    protected ProtocolVersion getPv(ObjectNode node, String key) {
        String pvstr = getText(node, key);
        ProtocolVersion pv = ProtocolVersion.fromString(pvstr);
        if (pv == null)
            throw new IllegalArgumentException(E_BAD_PV + pvstr);
        return pv;
    }

    private static final String E_BAD_PV = "Unrecognized OF protocol version: ";

    /**
     * Reads the property with the given key from the given object node and
     * attempts to interpret it as a datapath identifier.
     *
     * @param node the object node
     * @param key the property key
     * @return datapath ID
     * @throws IllegalArgumentException if the value is malformed
     */
    protected DataPathId getDpid(ObjectNode node, String key) {
        return DataPathId.dpid(getText(node, key));
    }

    /**
     * Returns the property with the given key from the given object node,
     * interpreted as a long value. If no such property exists then zero is
     * returned.
     *
     * @param node the object node
     * @param key the property key
     * @return the value
     */
    protected long getLongOrZero(ObjectNode node, String key) {
        return node.has(key) ? node.get(key).asLong() : 0;
    }

    /**
     * Returns the property with the given key from the given object node,
     * interpreted as an int value. If no such property exists then zero is
     * returned.
     *
     * @param node the object node
     * @param key the property key
     * @return the value
     */
    protected int getIntOrZero(ObjectNode node, String key) {
        return node.has(key) ? node.get(key).asInt() : 0;
    }

    /**
     * Returns the property with the given key from the given object node,
     * interpreted as an IP address.
     *
     * @param node the object node
     * @param key the property key
     * @return the IP address
     * @throws IllegalArgumentException if the value is malformed
     */
    protected IpAddress getIp(ObjectNode node, String key) {
        return IpAddress.valueOf(getText(node, key));
    }

    /**
     * Returns the property with the given key from the given object node,
     * interpreted as a TCP port number.
     *
     * @param node the object node
     * @param key the property key
     * @return the port number
     * @throws IllegalArgumentException if the value is not valid
     */
    protected PortNumber getPortNumber(ObjectNode node, String key) {
        return PortNumber.valueOf(getIntOrZero(node, key));
    }

    @Override
    public DataPathInfo decode(ObjectNode node) {
        if (node == null)
            return null;

        DataPathInfoDTO dpInfo = new DataPathInfoDTO();

        dpInfo.dpid = getDpid(node, DPID);
        dpInfo.pv = getPv(node, NEGOTIATED_VERSION);

        dpInfo.rt = getTime(node, READY);
        dpInfo.mt = getTime(node, LAST_MESSAGE);

        dpInfo.nBuff = getLongOrZero(node, NUM_BUFFERS);
        dpInfo.nTab = getIntOrZero(node, NUM_TABLES);

        dpInfo.mfr = getText(node, MFR);
        dpInfo.hw = getText(node, HW);
        dpInfo.sw = getText(node, SW);
        dpInfo.serial = getText(node, SERIAL);
        dpInfo.desc = getText(node, DESC);

        dpInfo.ipa = getIp(node, DEVICE_IP);
        dpInfo.pNum = getPortNumber(node, DEVICE_PORT);

        dpInfo.caps = toEnums((ArrayNode) node.get(CAPS), Capability.class);
        return dpInfo;
    }

    /*
     * Dummy implementation for {@link DataPathInfo}.
     *
     * This is required to implement DataPathInfo decode for sideways REST.
     */
    private static class DataPathInfoDTO implements DataPathInfo {
        private ProtocolVersion pv;
        private DataPathId dpid;
        private long rt;
        private long mt;
        private long nBuff;
        private int nTab;
        private IpAddress ipa;
        private PortNumber pNum;
        private Set<Capability> caps;
        private String mfr;
        private String hw;
        private String sw;
        private String serial;
        private String desc;

        @Override public DataPathId dpid() { return dpid; }
        @Override public ProtocolVersion negotiated() { return pv; }
        @Override public long readyAt() { return rt; }
        @Override public long lastMessageAt() { return mt; }

        @Override
        public List<Port> ports() {
            // note: just return an empty list in this case.
            return Collections.emptyList();
        }

        @Override public long numBuffers() { return nBuff; }
        @Override public int numTables() { return nTab; }
        @Override public Set<Capability> capabilities() { return caps; }
        @Override public IpAddress remoteAddress() { return ipa; }
        @Override public PortNumber remotePort() { return pNum; }
        @Override public String datapathDescription() { return desc; }
        @Override public String manufacturerDescription() { return mfr; }
        @Override public String hardwareDescription() { return hw; }
        @Override public String softwareDescription() { return sw; }
        @Override public String serialNumber() { return serial; }
        @Override public String deviceTypeName() { return null; }
    }
}
