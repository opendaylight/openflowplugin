/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp.utils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.opendaylight.controller.liblldp.BitBufferHelper;
import org.opendaylight.controller.liblldp.CustomTLVKey;
import org.opendaylight.controller.liblldp.Ethernet;
import org.opendaylight.controller.liblldp.LLDP;
import org.opendaylight.controller.liblldp.LLDPTLV;
import org.opendaylight.controller.liblldp.NetUtils;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.applications.topology.lldp.LLDPActivator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LLDPDiscoveryUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPDiscoveryUtils.class);

    private static final short MINIMUM_LLDP_SIZE = 61;
    public static final short ETHERNET_TYPE_VLAN = (short) 0x8100;
    public static final short ETHERNET_TYPE_LLDP = (short) 0x88cc;
    private static final short ETHERNET_TYPE_OFFSET = 12;
    private static final short ETHERNET_VLAN_OFFSET = ETHERNET_TYPE_OFFSET + 4;

    public static String macToString(byte[] mac) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            b.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }

        return b.toString();
    }

    /**
     * @param payload
     * @return nodeConnectorId - encoded in custom TLV of given lldp
     * @see LLDPDiscoveryUtils#lldpToNodeConnectorRef(byte[], boolean)
     */
    public static NodeConnectorRef lldpToNodeConnectorRef(byte[] payload)  {
        return lldpToNodeConnectorRef(payload, false);
    }

    /**
     * @param payload
     * @param useExtraAuthenticatorCheck make it more secure (CVE-2015-1611 CVE-2015-1612)
     * @return nodeConnectorId - encoded in custom TLV of given lldp
     */
    public static NodeConnectorRef lldpToNodeConnectorRef(byte[] payload, boolean useExtraAuthenticatorCheck)  {
        NodeConnectorRef nodeConnectorRef = null;

        if (isLLDP(payload)) {
            Ethernet ethPkt = new Ethernet();
            try {
                ethPkt.deserialize(payload, 0, payload.length * NetUtils.NumBitsInAByte);
            } catch (Exception e) {
                LOG.warn("Failed to decode LLDP packet {}", e);
                return nodeConnectorRef;
            }

            LLDP lldp = (LLDP) ethPkt.getPayload();

            try {
                NodeId srcNodeId = null;
                NodeConnectorId srcNodeConnectorId = null;

                final LLDPTLV systemIdTLV = lldp.getSystemNameId();
                if (systemIdTLV != null) {
                    String srcNodeIdString = new String(systemIdTLV.getValue(), Charset.defaultCharset());
                    srcNodeId = new NodeId(srcNodeIdString);
                } else {
                    throw new Exception("Node id wasn't specified via systemNameId in LLDP packet.");
                }

                final LLDPTLV nodeConnectorIdLldptlv = lldp.getCustomTLV(
                        new CustomTLVKey(BitBufferHelper.getInt(LLDPTLV.OFOUI), LLDPTLV.CUSTOM_TLV_SUB_TYPE_NODE_CONNECTOR_ID[0]));
                if (nodeConnectorIdLldptlv != null) {
                    srcNodeConnectorId = new NodeConnectorId(LLDPTLV.getCustomString(
                            nodeConnectorIdLldptlv.getValue(), nodeConnectorIdLldptlv.getLength()));
                } else {
                    throw new Exception("Node connector wasn't specified via Custom TLV in LLDP packet.");
                }

                if (useExtraAuthenticatorCheck) {
                    boolean secure = checkExtraAuthenticator(lldp, srcNodeConnectorId);
                    if (!secure) {
                        LOG.warn("SECURITY ALERT: there is probably a LLDP spoofing attack in progress.");
                        throw new Exception("Attack. LLDP packet with inconsistent extra authenticator field was received.");
                    }
                }

                InstanceIdentifier<NodeConnector> srcInstanceId = InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class, new NodeKey(srcNodeId))
                        .child(NodeConnector.class, new NodeConnectorKey(srcNodeConnectorId))
                        .toInstance();
                nodeConnectorRef = new NodeConnectorRef(srcInstanceId);
            } catch (Exception e) {
                LOG.debug("Caught exception while parsing out lldp optional and custom fields: {}", e.getMessage(), e);
            }
        }
        return nodeConnectorRef;
    }

    /**
     * @param nodeConnectorId
     * @return extra authenticator for lldp security
     * @throws NoSuchAlgorithmException
     */
    public static byte[] getValueForLLDPPacketIntegrityEnsuring(final NodeConnectorId nodeConnectorId) throws NoSuchAlgorithmException {
        String finalKey;
        if(LLDPActivator.getLldpSecureKey() !=null && !LLDPActivator.getLldpSecureKey().isEmpty()) {
            finalKey = LLDPActivator.getLldpSecureKey();
        } else {
            finalKey = ManagementFactory.getRuntimeMXBean().getName();
        }
        final String pureValue = nodeConnectorId + finalKey;

        final byte[] pureBytes = pureValue.getBytes();
        HashFunction hashFunction = Hashing.md5();
        Hasher hasher = hashFunction.newHasher();
        HashCode hashedValue = hasher.putBytes(pureBytes).hash();
        return hashedValue.asBytes();
    }

    /**
     * @param lldp
     * @param srcNodeConnectorId
     * @throws NoSuchAlgorithmException
     */
    private static boolean checkExtraAuthenticator(LLDP lldp, NodeConnectorId srcNodeConnectorId) throws NoSuchAlgorithmException {
        final LLDPTLV hashLldptlv = lldp.getCustomTLV(
                new CustomTLVKey(BitBufferHelper.getInt(LLDPTLV.OFOUI), LLDPTLV.CUSTOM_TLV_SUB_TYPE_CUSTOM_SEC[0]));
        boolean secAuthenticatorOk = false;
        if (hashLldptlv != null) {
            byte[] rawTlvValue = hashLldptlv.getValue();
            byte[] lldpCustomSecurityHash = ArrayUtils.subarray(rawTlvValue, 4, rawTlvValue.length);
            byte[] calculatedHash = getValueForLLDPPacketIntegrityEnsuring(srcNodeConnectorId);
            secAuthenticatorOk = Arrays.equals(calculatedHash, lldpCustomSecurityHash);
        } else {
            LOG.debug("Custom security hint wasn't specified via Custom TLV in LLDP packet.");
        }

        return secAuthenticatorOk;
    }

    private static boolean isLLDP(final byte[] packet) {
        if (Objects.isNull(packet) || packet.length < MINIMUM_LLDP_SIZE) {
            return false;
        }

        final ByteBuffer bb = ByteBuffer.wrap(packet);

        short ethernetType = bb.getShort(ETHERNET_TYPE_OFFSET);

        if (ethernetType == ETHERNET_TYPE_VLAN) {
            ethernetType = bb.getShort(ETHERNET_VLAN_OFFSET);
        }

        return (ethernetType == ETHERNET_TYPE_LLDP);
    }

    public static boolean isEntityOwned(final EntityOwnershipService eos, final String nodeId) {
        Preconditions.checkNotNull(eos, "Entity ownership service must not be null");

        EntityOwnershipChange change = null;
        Optional<EntityOwnershipChange> status = getCurrentOwnershipStatus(eos, nodeId);
        if (status.isPresent()) {
            change = status.get();
        } else {
            LOG.error("Fetching ownership status failed for node {}", nodeId);
        }
        return change != null? change.isOwner():false;
    }

    private static Optional<EntityOwnershipChange> getCurrentOwnershipStatus(final EntityOwnershipService eos, final
    String
            nodeId) {
        Entity entity = createNodeEntity(nodeId);
        Optional<EntityOwnershipState> ownershipStatus = eos.getOwnershipState(entity);

        if(ownershipStatus.isPresent()) {
            LOG.debug("Fetched ownership status for node {} is {}", nodeId, ownershipStatus.get());
            return Optional.of(new EntityOwnershipChange(entity, false, ownershipStatus.get().isOwner(), ownershipStatus
                    .get().hasOwner(), false));
        }
        return Optional.absent();
    }

    private static Entity createNodeEntity(final String nodeId) {
        DOMEntity domEntity = new DOMEntity("org.opendaylight.mdsal.ServiceEntityType",
                ServiceGroupIdentifier.create(nodeId).getValue());
        return new Entity(domEntity.getType(), domEntity.getIdentifier());
    }
}
