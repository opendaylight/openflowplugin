/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.libraries.liblldp;

import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that represents the LLDP frame objects.
 */
public class LLDP extends Packet {
    private static final Logger LOG = LoggerFactory.getLogger(LLDP.class);
    private static final String CHASSISID = "ChassisId";
    private static final String SYSTEMNAMEID = "SystemNameID";
    private static final String PORTID = "PortId";
    private static final String TTL = "TTL";
    private static final String SYSTEMDESC = "SystemDesc";
    private static final String PORTDESC = "PortDesc";
    private static final String SYSTEMCAPABILITIES = "SystemCapabilities";
    private static final String MANAGEMENTADDRESS = "ManagementAddress";
    private static final int LLDP_DEFAULT_TLVS = 3;
    private static final LLDPTLV EMPTY_TLV = new LLDPTLV().setLength((short) 0).setType((byte) 0);
    @SuppressFBWarnings("MS_PKGPROTECT")
    public static final byte[] LLDP_MULTICAST_MAC = { 1, (byte) 0x80, (byte) 0xc2, 0, 0, (byte) 0xe };

    private final Map<Byte, LLDPTLV> mandatoryTLVs = new LinkedHashMap<>(LLDP_DEFAULT_TLVS);
    private final Map<Byte, LLDPTLV> optionalTLVs = new LinkedHashMap<>();
    private final Map<CustomTLVKey, LLDPTLV> customTLVs = new LinkedHashMap<>();

    /**
     * Default constructor that creates the tlvList LinkedHashMap.
     */
    public LLDP() {

    }

    /**
     * Constructor that creates the tlvList LinkedHashMap and sets the write access for the same.
     */
    public LLDP(final boolean writeAccess) {
        super(writeAccess);
    }

    /**
     * Returns the TLV byte type.
     *
     * @param typeDesc description of the type of TLV
     * @return byte type of TLV
     */
    private static byte getType(final String typeDesc) {
        switch (typeDesc) {
            case CHASSISID:
                return LLDPTLV.TLVType.ChassisID.getValue();
            case PORTID:
                return LLDPTLV.TLVType.PortID.getValue();
            case TTL:
                return LLDPTLV.TLVType.TTL.getValue();
            case SYSTEMNAMEID:
                return LLDPTLV.TLVType.SystemName.getValue();
            case SYSTEMDESC:
                return LLDPTLV.TLVType.SystemDesc.getValue();
            case PORTDESC:
                return LLDPTLV.TLVType.PortDesc.getValue();
            case SYSTEMCAPABILITIES:
                return LLDPTLV.TLVType.SystemCapabilities.getValue();
            case MANAGEMENTADDRESS:
                return LLDPTLV.TLVType.ManagementAddress.getValue();
            default:
                return LLDPTLV.TLVType.Unknown.getValue();
        }
    }

    private LLDPTLV getFromTLVs(final Byte type) {
        LLDPTLV tlv;
        tlv = mandatoryTLVs.get(type);
        if (tlv == null) {
            tlv = optionalTLVs.get(type);
        }
        return tlv;
    }

    private void putToTLVs(final Byte type, final LLDPTLV tlv) {
        if (type == LLDPTLV.TLVType.ChassisID.getValue() || type == LLDPTLV.TLVType.PortID.getValue()
                || type == LLDPTLV.TLVType.TTL.getValue()) {
            mandatoryTLVs.put(type, tlv);
        } else if (type != LLDPTLV.TLVType.Custom.getValue()) {
            optionalTLVs.put(type, tlv);
        }
    }

    /**
     * Gets the full LLDPTLV.
     *
     * @param type description of the type of TLV
     * @return LLDPTLV - full TLV
     */
    public LLDPTLV getTLV(final String type) {
        return getFromTLVs(getType(type));
    }

    public LLDPTLV getCustomTLV(final CustomTLVKey key) {
        return customTLVs.get(key);
    }

    /**
     * Sets the LLDPTLV for a type.
     *
     * @param type description of the type of TLV
     * @param tlv tlv to set
     */
    public void setTLV(final String type, final LLDPTLV tlv) {
        putToTLVs(getType(type), tlv);
    }

    /**
     * Returns the chassisId TLV.
     */
    public LLDPTLV getChassisId() {
        return getTLV(CHASSISID);
    }

    public LLDP setChassisId(final LLDPTLV chassisId) {
        setTLV(CHASSISID, chassisId);
        return this;
    }

    /**
     * Returns the SystemName TLV.
     */
    public LLDPTLV getSystemNameId() {
        return getTLV(SYSTEMNAMEID);
    }

    public LLDP setSystemNameId(final LLDPTLV systemNameId) {
        setTLV(SYSTEMNAMEID, systemNameId);
        return this;
    }

    /**
     * Returns the portId TLV.
     */
    public LLDPTLV getPortId() {
        return getTLV(PORTID);
    }

    public LLDP setPortId(final LLDPTLV portId) {
        setTLV(PORTID, portId);
        return this;
    }

    /**
     * Return the ttl TLV.
     */
    public LLDPTLV getTtl() {
        return getTLV(TTL);
    }

    public LLDP setTtl(final LLDPTLV ttl) {
        setTLV(TTL, ttl);
        return this;
    }

    /**
     * Return the SystemDesc TLV.
     */
    public LLDPTLV getSystemDesc() {
        return getTLV(SYSTEMDESC);
    }

    public LLDP setSystemDesc(final LLDPTLV systemDesc) {
        setTLV(SYSTEMDESC, systemDesc);
        return this;
    }

    /**
     * Return the PortDesc TLV.
     */
    public LLDPTLV getPortDesc() {
        return getTLV(PORTDESC);
    }

    public LLDP setPortDesc(final LLDPTLV portDesc) {
        setTLV(PORTDESC, portDesc);
        return this;
    }

    /**
     * Return the SystemCapabilities TLV.
     */
    public LLDPTLV getSystemCapabilities() {
        return getTLV(SYSTEMCAPABILITIES);
    }

    public LLDP setSystemCapabilities(final LLDPTLV systemCapabilities) {
        setTLV(SYSTEMCAPABILITIES, systemCapabilities);
        return this;
    }

    /**
     * Return the ManagementAddress TLV.
     */
    public LLDPTLV getManagementAddress() {
        return getTLV(MANAGEMENTADDRESS);
    }

    public LLDP setManagementAddress(final LLDPTLV managementAddress) {
        setTLV(MANAGEMENTADDRESS, managementAddress);
        return this;
    }

    /**
     * Returns the optionalTLVList.
     */
    public Iterable<LLDPTLV> getOptionalTLVList() {
        return optionalTLVs.values();
    }

    /**
     * Returns the customTlvList.
     */
    public Iterable<LLDPTLV> getCustomTlvList() {
        return customTLVs.values();
    }

    public LLDP setOptionalTLVList(final List<LLDPTLV> optionalTLVList) {
        for (LLDPTLV tlv : optionalTLVList) {
            optionalTLVs.put(tlv.getType(), tlv);
        }
        return this;
    }

    public LLDP addCustomTLV(final LLDPTLV customTLV) {
        CustomTLVKey key = new CustomTLVKey(LLDPTLV.extractCustomOUI(customTLV),
                LLDPTLV.extractCustomSubtype(customTLV));
        customTLVs.put(key, customTLV);

        return this;
    }

    @Override
    public Packet deserialize(final byte[] data, final int bitOffset, final int size) throws PacketException {
        int lldpOffset = bitOffset; // LLDP start
        int lldpSize = size; // LLDP size

        if (LOG.isTraceEnabled()) {
            LOG.trace("LLDP: {} (offset {} bitsize {})", HexEncode.bytesToHexString(data), lldpOffset, lldpSize);
        }
        /*
         * Deserialize the TLVs until we reach the end of the packet
         */
        while (lldpSize > 0) {
            LLDPTLV tlv = new LLDPTLV();
            tlv.deserialize(data, lldpOffset, lldpSize);
            if (tlv.getType() == 0 && tlv.getLength() == 0) {
                break;
            }
            int tlvSize = tlv.getTLVSize(); // Size of current TLV in bits
            lldpOffset += tlvSize;
            lldpSize -= tlvSize;
            if (tlv.getType() == LLDPTLV.TLVType.Custom.getValue()) {
                addCustomTLV(tlv);
            } else {
                this.putToTLVs(tlv.getType(), tlv);
            }
        }
        return this;
    }

    @Override
    public byte[] serialize() throws PacketException {
        int startOffset = 0;
        byte[] serializedBytes = new byte[getLLDPPacketLength()];

        final Iterable<LLDPTLV> allTlvs = Iterables.concat(mandatoryTLVs.values(), optionalTLVs.values(),
                customTLVs.values());
        for (LLDPTLV tlv : allTlvs) {
            int numBits = tlv.getTLVSize();
            try {
                BitBufferHelper.copyBitsFromLsb(serializedBytes, tlv.serialize(), startOffset, numBits);
            } catch (final BufferException e) {
                throw new PacketException("Error from setBytes", e);
            }
            startOffset += numBits;
        }
        // Now add the empty LLDPTLV at the end
        try {
            BitBufferHelper.copyBitsFromLsb(serializedBytes, LLDP.EMPTY_TLV.serialize(), startOffset,
                    LLDP.EMPTY_TLV.getTLVSize());
        } catch (final BufferException e) {
            throw new PacketException("Error from setBytes", e);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("LLDP: serialized: {}", HexEncode.bytesToHexString(serializedBytes));
        }
        return serializedBytes;
    }

    /**
     * Returns the size of LLDP packet in bytes.
     *
     * @return int - LLDP Packet size in bytes
     */
    private int getLLDPPacketLength() {
        int len = 0;

        for (LLDPTLV lldptlv : Iterables.concat(mandatoryTLVs.values(), optionalTLVs.values(), customTLVs.values())) {
            len += lldptlv.getTLVSize();
        }

        len += LLDP.EMPTY_TLV.getTLVSize();

        return len / Byte.SIZE;
    }
}

