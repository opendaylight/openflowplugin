/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import java.util.Arrays;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class PacketUtils {

    /**
     * size of MAC address in octets (6*8 = 48 bits).
     */
    private static final int MAC_ADDRESS_SIZE = 6;

    /**
     * start position of destination MAC address in array.
     */
    private static final int DST_MAC_START_POSITION = 0;

    /**
     * end position of destination MAC address in array.
     */
    private static final int DST_MAC_END_POSITION = 6;

    /**
     * start position of source MAC address in array.
     */
    private static final int SRC_MAC_START_POSITION = 6;

    /**
     * end position of source MAC address in array.
     */
    private static final int SRC_MAC_END_POSITION = 12;

    /**
     * start position of ethernet type in array.
     */
    private static final int ETHER_TYPE_START_POSITION = 12;

    /**
     * end position of ethernet type in array.
     */
    private static final int ETHER_TYPE_END_POSITION = 14;

    private PacketUtils() {
        //prohibite to instantiate this class
    }

    /**
     * Extracts the destination MAC address.
     *
     * @param payload the payload bytes
     * @return destination MAC address
     */
    public static byte[] extractDstMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_MAC_START_POSITION, DST_MAC_END_POSITION);
    }

    /**
     * Extracts the source MAC address.
     *
     * @param payload the payload bytes
     * @return source MAC address
     */
    public static byte[] extractSrcMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_MAC_START_POSITION, SRC_MAC_END_POSITION);
    }

    /**
     * Extracts the ethernet type.
     *
     * @param payload the payload bytes
     * @return source MAC address
     */
    public static byte[] extractEtherType(final byte[] payload) {
        return Arrays.copyOfRange(payload, ETHER_TYPE_START_POSITION, ETHER_TYPE_END_POSITION);
    }

    /**
     * Converts a raw MAC bytes to a MacAddress.
     *
     * @param rawMac the raw bytes
     * @return {@link MacAddress} wrapping string value, baked upon binary MAC address
     */
    public static MacAddress rawMacToMac(final byte[] rawMac) {
        MacAddress mac = null;
        if (rawMac != null && rawMac.length == MAC_ADDRESS_SIZE) {
            StringBuilder sb = new StringBuilder();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            mac = new MacAddress(sb.substring(1));
        }
        return mac;
    }

    /**
     * Returns the port wrapped into {@link NodeConnectorRef}.
     */
    public static NodeConnectorRef createNodeConnRef(final InstanceIdentifier<Node> nodeInstId, final NodeKey nodeKey,
            final String port) {
        StringBuilder builder = new StringBuilder(nodeKey.getId().getValue()).append(':').append(port);
        NodeConnectorKey connKey = new NodeConnectorKey(new NodeConnectorId(builder.toString()));
        InstanceIdentifier<NodeConnector> portPath = nodeInstId.child(NodeConnector.class, connKey);
        return new NodeConnectorRef(portPath);
    }
}
