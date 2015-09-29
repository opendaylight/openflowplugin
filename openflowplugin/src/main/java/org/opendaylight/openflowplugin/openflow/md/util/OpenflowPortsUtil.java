/*
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.collect.ImmutableBiMap;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValuesV10;

/**
 * Class which integrates the port constants defined and used by MDSAL and the ports defined in openflow java
 * This class is responsible for converting MDSAL given logical names to port numbers and back.
 * Any newer version of openflow can have a similar mapping or can/should be extended.
 *
 * @author Kamal Rameshan on 5/2/14.
 */
public class OpenflowPortsUtil {

    static ImmutableBiMap<OpenflowVersion, ImmutableBiMap<String, Long>> versionPortMap;

    static final String MAX = "MAX";

    // the init gets called from MDController at the start
    public static void init() {

        // v1.0 ports
        final ImmutableBiMap<String, Long> OFv10 = new ImmutableBiMap.Builder<String, Long>()
                .put(OutputPortValues.MAX.toString(), Long.valueOf(PortNumberValuesV10.MAX.getIntValue())) //0xff00
                .put(OutputPortValues.INPORT.toString(), Long.valueOf(PortNumberValuesV10.INPORT.getIntValue())) //0xfff8
                .put(OutputPortValues.TABLE.toString(), Long.valueOf(PortNumberValuesV10.TABLE.getIntValue())) //0xfff9
                .put(OutputPortValues.NORMAL.toString(), Long.valueOf(PortNumberValuesV10.NORMAL.getIntValue())) //0xfffa
                .put(OutputPortValues.FLOOD.toString(), Long.valueOf(PortNumberValuesV10.FLOOD.getIntValue())) //0xfffb
                .put(OutputPortValues.ALL.toString(), Long.valueOf(PortNumberValuesV10.ALL.getIntValue())) //0xfffc
                .put(OutputPortValues.CONTROLLER.toString(), Long.valueOf(PortNumberValuesV10.CONTROLLER.getIntValue())) //0xfffd
                .put(OutputPortValues.LOCAL.toString(), Long.valueOf(PortNumberValuesV10.LOCAL.getIntValue())) //0xfffe
                .put(OutputPortValues.NONE.toString(), Long.valueOf(PortNumberValuesV10.NONE.getIntValue())) //0xfffe
                .build();

        // openflow 1.3 reserved ports.
        // PortNumberValues are defined in OFJava yang. And yang maps an int to all enums. Hence we need to create longs from (-ve) ints
        // TODO: do we need to define these ports in yang?
        final ImmutableBiMap<String, Long> OFv13 = new ImmutableBiMap.Builder<String, Long>()
                .put(OutputPortValues.MAX.toString(), BinContent.intToUnsignedLong(PortNumberValues.MAX.getIntValue())) //0xffffff00
                .put(OutputPortValues.INPORT.toString(), BinContent.intToUnsignedLong(PortNumberValues.INPORT.getIntValue())) //0xfffffff8
                .put(OutputPortValues.TABLE.toString(), BinContent.intToUnsignedLong(PortNumberValues.TABLE.getIntValue())) //0xfffffff9
                .put(OutputPortValues.NORMAL.toString(), BinContent.intToUnsignedLong(PortNumberValues.NORMAL.getIntValue())) //0xfffffffa
                .put(OutputPortValues.FLOOD.toString(), BinContent.intToUnsignedLong(PortNumberValues.FLOOD.getIntValue())) //0xfffffffb
                .put(OutputPortValues.ALL.toString(), BinContent.intToUnsignedLong(PortNumberValues.ALL.getIntValue())) //0xfffffffc
                .put(OutputPortValues.CONTROLLER.toString(), BinContent.intToUnsignedLong(PortNumberValues.CONTROLLER.getIntValue())) //0xfffffffd
                .put(OutputPortValues.LOCAL.toString(), BinContent.intToUnsignedLong(PortNumberValues.LOCAL.getIntValue())) //0xfffffffe
                .put(OutputPortValues.ANY.toString(), BinContent.intToUnsignedLong(PortNumberValues.ANY.getIntValue())) //0xffffffff
                .build();

        versionPortMap = new ImmutableBiMap.Builder<OpenflowVersion, ImmutableBiMap<String, Long>>()
                .put(OpenflowVersion.OF10, OFv10)
                .put(OpenflowVersion.OF13, OFv13)
                .build();

    }

    public static void close() {
        versionPortMap = null;
    }

    public static String getPortLogicalName(final short ofVersion, final long portNumber) {
        return versionPortMap.get(OpenflowVersion.get(ofVersion)).inverse().get(portNumber);
    }

    public static String getPortLogicalName(final OpenflowVersion ofVersion, final Long portNumber) {
        if (ofVersion.equals(OpenflowVersion.UNSUPPORTED)) {
            return null;
        }
        return versionPortMap.get(ofVersion).inverse().get(portNumber);
    }

    public static Long getPortFromLogicalName(final OpenflowVersion ofVersion, final String logicalNameOrPort) {
        Long port = versionPortMap.get(ofVersion).get(logicalNameOrPort);
        if (port == null) {
            try {
                port = Long.decode(logicalNameOrPort);
            } catch (final NumberFormatException ne) {
                //ignore, sent null back.
                if (logicalNameOrPort.contains(":")) {
                    port = Long.parseLong(logicalNameOrPort.substring(logicalNameOrPort.lastIndexOf(":") + 1));
                }
            }
        }
        return port;
    }

    public static PortNumberUni getProtocolAgnosticPort(final OpenflowVersion ofVersion, final Long portNumber) {
        final String reservedPortLogicalName = getPortLogicalName(ofVersion, portNumber);
        return (reservedPortLogicalName == null ? new PortNumberUni(portNumber) :
                new PortNumberUni(reservedPortLogicalName));
    }

    public static Long getProtocolPortNumber(final OpenflowVersion ofVersion, final PortNumberUni port) {
        final String portLogicalName = port.getString();

        if (portLogicalName != null) {
            return versionPortMap.get(ofVersion).get(portLogicalName);
        } else {
            return port.getUint32();
        }
    }

    public static Long getMaxPortForVersion(final OpenflowVersion ofVersion) {
        return getPortFromLogicalName(ofVersion, MAX);
    }

    public static boolean isPortReserved(final OpenflowVersion ofVersion, final Long portNumber) {
        return versionPortMap.get(ofVersion).inverse().containsKey(portNumber);
    }

    /**
     * @param ofVersion OpenFlow version of the switch
     * @param portNumber port number
     * @return true if port number is valid for given protocol version
     */
    public static boolean checkPortValidity(final OpenflowVersion ofVersion, final Long portNumber) {
        boolean portIsValid = true;
        if (portNumber == null) {
            portIsValid = false;
        } else if (portNumber < 0) {
            portIsValid = false;
        } else if (portNumber > OpenflowPortsUtil.getMaxPortForVersion(ofVersion)) {
            if (!OpenflowPortsUtil.isPortReserved(ofVersion, portNumber)) {
                portIsValid = false;
            }
        }
        return portIsValid;
    }

    /**
     * @param portNumber port number
     * @return string containing number or logical name
     */
    public static String portNumberToString(final PortNumberUni portNumber) {
        String result = null;
        if (portNumber.getUint32() != null) {
            result = String.valueOf(portNumber.getUint32());
        } else if (portNumber.getString() != null) {
            result = portNumber.getString();
        }
        return result;
    }
}
