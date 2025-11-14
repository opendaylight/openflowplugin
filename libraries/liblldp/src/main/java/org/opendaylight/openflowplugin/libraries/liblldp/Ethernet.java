/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.libraries.liblldp;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Class that represents the Ethernet frame objects.
 */
public class Ethernet extends Packet {
    private static final String DMAC = "DestinationMACAddress";
    private static final String SMAC = "SourceMACAddress";
    private static final String ETHT = "EtherType";

    /**
     * Constant holding the broadcast MAC address.
     */
    private static final byte[] BROADCAST_MAC_ADDR = { -1, -1, -1, -1, -1, -1 };

    // TODO: This has to be outside and it should be possible for osgi
    // to add new coming packet classes
    private static final Map<Short, Supplier<Packet>> ETHER_TYPE_CLASS_MAP = ImmutableMap.of(
        EtherTypes.LLDP.shortValue(), LLDP::new);

    private static final Map<String, Pair<Integer, Integer>> FIELD_COORDINATES = ImmutableMap.of(
        DMAC, new ImmutablePair<>(0, 48),
        SMAC, new ImmutablePair<>(48, 48),
        ETHT, new ImmutablePair<>(96, 16));

    private final Map<String, byte[]> fieldValues = new HashMap<>(4);

    /**
     * Default constructor that creates and sets the HashMap.
     */
    public Ethernet() {
        hdrFieldCoordMap = FIELD_COORDINATES;
        hdrFieldsMap = fieldValues;
    }

    /**
     * Constructor that sets the access level for the packet and creates and sets the HashMap.
     */
    public Ethernet(final boolean writeAccess) {
        super(writeAccess);
        hdrFieldCoordMap = FIELD_COORDINATES;
        hdrFieldsMap = fieldValues;
    }

    @Override
    public void setHeaderField(final String headerField, final byte[] readValue) {
        if (headerField.equals(ETHT)) {
            payloadFactory = ETHER_TYPE_CLASS_MAP.get(BitBufferHelper.getShort(readValue));
        }
        hdrFieldsMap.put(headerField, readValue);
    }

    /**
     * Gets the destination MAC address stored.
     *
     * @return byte[] - the destinationMACAddress
     */
    public byte[] getDestinationMACAddress() {
        return fieldValues.get(DMAC);
    }

    /**
     * Gets the source MAC address stored.
     *
     * @return byte[] - the sourceMACAddress
     */
    public byte[] getSourceMACAddress() {
        return fieldValues.get(SMAC);
    }

    /**
     * Gets the etherType stored.
     *
     * @return short - the etherType
     */
    public short getEtherType() {
        return BitBufferHelper.getShort(fieldValues.get(ETHT));
    }

    public boolean isBroadcast() {
        return Arrays.equals(getDestinationMACAddress(), BROADCAST_MAC_ADDR);
    }

    public boolean isMulticast() {
        final var macAddress = getDestinationMACAddress();
        return macAddress.length == BROADCAST_MAC_ADDR.length && (macAddress[0] & 1) != 0
            && !Arrays.equals(macAddress, BROADCAST_MAC_ADDR);
    }

    /**
     * Sets the destination MAC address for the current Ethernet object instance.
     *
     * @param destinationMACAddress the destinationMACAddress to set
     */
    public Ethernet setDestinationMACAddress(final byte[] destinationMACAddress) {
        fieldValues.put(DMAC, destinationMACAddress);
        return this;
    }

    /**
     * Sets the source MAC address for the current Ethernet object instance.
     *
     * @param sourceMACAddress the sourceMACAddress to set
     */
    public Ethernet setSourceMACAddress(final byte[] sourceMACAddress) {
        fieldValues.put(SMAC, sourceMACAddress);
        return this;
    }

    /**
     * Sets the etherType for the current Ethernet object instance.
     *
     * @param etherType the etherType to set
     */
    public Ethernet setEtherType(final short etherType) {
        byte[] ethType = BitBufferHelper.toByteArray(etherType);
        fieldValues.put(ETHT, ethType);
        return this;
    }
}
