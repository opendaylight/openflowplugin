/*
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import com.google.common.collect.ImmutableBiMap;
import java.util.Objects;
import javax.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValuesV10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which integrates the port constants defined and used by MDSAL and the ports defined in openflow java
 * This class is responsible for converting MDSAL given logical names to port numbers and back.
 * Any newer version of openflow can have a similar mapping or can/should be extended.
 *
 * @author Kamal Rameshan on 5/2/14.
 */
public class OpenflowPortsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPortsUtil.class);
    private static final ImmutableBiMap<Short, ImmutableBiMap<String, Long>> versionPortMap;
    private static final ImmutableBiMap<Short, ImmutableBiMap<Long, String>> versionInversePortMap;

    private static boolean inportWarnignAlreadyFired = false;

    static {
        // v1.0 ports
        final ImmutableBiMap<String, Long> ofv10ports = new ImmutableBiMap.Builder<String, Long>()
                .put(OutputPortValues.MAX.getName(), (long) PortNumberValuesV10.MAX.getIntValue()) //0xff00
                .put(OutputPortValues.INPORT.getName(), (long) PortNumberValuesV10.INPORT.getIntValue()) //0xfff8
                .put(OutputPortValues.TABLE.getName(), (long) PortNumberValuesV10.TABLE.getIntValue()) //0xfff9
                .put(OutputPortValues.NORMAL.getName(), (long) PortNumberValuesV10.NORMAL.getIntValue()) //0xfffa
                .put(OutputPortValues.FLOOD.getName(), (long) PortNumberValuesV10.FLOOD.getIntValue()) //0xfffb
                .put(OutputPortValues.ALL.getName(), (long) PortNumberValuesV10.ALL.getIntValue()) //0xfffc
                .put(OutputPortValues.CONTROLLER.getName(), (long) PortNumberValuesV10.CONTROLLER.getIntValue()) //0xfffd
                .put(OutputPortValues.LOCAL.getName(), (long) PortNumberValuesV10.LOCAL.getIntValue()) //0xfffe
                .put(OutputPortValues.NONE.getName(), (long) PortNumberValuesV10.NONE.getIntValue()) //0xffff
                .build();

        // openflow 1.3 reserved ports.
        // PortNumberValues are defined in OFJava yang. And yang maps an int to all enums. Hence we need to create longs from (-ve) ints
        // TODO: do we need to define these ports in yang?
        final ImmutableBiMap<String, Long> ofv13ports = new ImmutableBiMap.Builder<String, Long>()
                .put(OutputPortValues.MAX.getName(), BinContent.intToUnsignedLong(PortNumberValues.MAX.getIntValue())) //0xffffff00
                .put(OutputPortValues.INPORT.getName(), BinContent.intToUnsignedLong(PortNumberValues.INPORT.getIntValue())) //0xfffffff8
                .put(OutputPortValues.TABLE.getName(), BinContent.intToUnsignedLong(PortNumberValues.TABLE.getIntValue())) //0xfffffff9
                .put(OutputPortValues.NORMAL.getName(), BinContent.intToUnsignedLong(PortNumberValues.NORMAL.getIntValue())) //0xfffffffa
                .put(OutputPortValues.FLOOD.getName(), BinContent.intToUnsignedLong(PortNumberValues.FLOOD.getIntValue())) //0xfffffffb
                .put(OutputPortValues.ALL.getName(), BinContent.intToUnsignedLong(PortNumberValues.ALL.getIntValue())) //0xfffffffc
                .put(OutputPortValues.CONTROLLER.getName(), BinContent.intToUnsignedLong(PortNumberValues.CONTROLLER.getIntValue())) //0xfffffffd
                .put(OutputPortValues.LOCAL.getName(), BinContent.intToUnsignedLong(PortNumberValues.LOCAL.getIntValue())) //0xfffffffe
                .put(OutputPortValues.ANY.getName(), BinContent.intToUnsignedLong(PortNumberValues.ANY.getIntValue())) //0xffffffff
                .build();

        versionPortMap = new ImmutableBiMap.Builder<Short, ImmutableBiMap<String, Long>>()
                .put(OFConstants.OFP_VERSION_1_0, ofv10ports)
                .put(OFConstants.OFP_VERSION_1_3, ofv13ports)
                .build();

        versionInversePortMap = new ImmutableBiMap.Builder<Short, ImmutableBiMap<Long, String>>()
                .put(OFConstants.OFP_VERSION_1_0, ofv10ports.inverse())
                .put(OFConstants.OFP_VERSION_1_3, ofv13ports.inverse())
                .build();
    }

    public static String getPortLogicalName(final short ofVersion, final Long portNumber) {
        return versionInversePortMap.get(ofVersion).get(portNumber);
    }

    public static String getPortLogicalName(final OpenflowVersion ofVersion, final Long portNumber) {
        return ofVersion.equals(OpenflowVersion.UNSUPPORTED)
                ? null
                : getPortLogicalName(ofVersion.getVersion(), portNumber);
    }

    @Nullable
    static Long getPortFromLogicalName(final OpenflowVersion ofVersion, final String logicalNameOrPort) {

        //The correct keyword defined in openflow specification in IN_PORT so we need to allow to use it
        //for legacy reasons we can't just simply drop the misspelled INPORT
        //TODO: Consider to remove 'INPORT' keyword
        Long port;
        if (Objects.equals(logicalNameOrPort, "INPORT")) {
            if (!inportWarnignAlreadyFired) {
                LOG.warn("Using '{}' in port field is not recommended use 'IN_PORT' instead", logicalNameOrPort);
                inportWarnignAlreadyFired = true;
            }
            port = versionPortMap.get(ofVersion.getVersion()).get(OutputPortValues.INPORT.getName());
        } else {
            port = versionPortMap.get(ofVersion.getVersion()).get(logicalNameOrPort);
        }
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

        return reservedPortLogicalName == null
                ? new PortNumberUni(portNumber)
                : new PortNumberUni(reservedPortLogicalName);
    }

    public static Long getProtocolPortNumber(final OpenflowVersion ofVersion, final PortNumberUni port) {
        final String portLogicalName = port.getString();

        return portLogicalName != null
                ? versionPortMap.get(ofVersion.getVersion()).get(portLogicalName)
                : port.getUint32();
    }

    public static Long getMaxPortForVersion(final OpenflowVersion ofVersion) {
        return getPortFromLogicalName(ofVersion, OutputPortValues.MAX.getName());
    }

    public static boolean isPortReserved(final OpenflowVersion ofVersion, final Long portNumber) {
        return versionInversePortMap.get(ofVersion.getVersion()).containsKey(portNumber);
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
        } else if (portNumber > getMaxPortForVersion(ofVersion)) {
            if (!isPortReserved(ofVersion, portNumber)) {
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

    /**
     * Converts port number to Uri
     * @param version openflow version
     * @param portNumber port number
     * @return port number uri
     */
    public static Uri getProtocolAgnosticPortUri(final short version, final long portNumber) {
        return new Uri(portNumberToString(getProtocolAgnosticPort(OpenflowVersion.get(version), portNumber)));
    }

}
