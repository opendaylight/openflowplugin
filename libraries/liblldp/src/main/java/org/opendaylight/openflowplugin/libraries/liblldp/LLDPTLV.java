/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.libraries.liblldp;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that represents the LLDPTLV objects.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class LLDPTLV extends Packet {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPTLV.class);
    private static final String TYPE = "Type";
    private static final String LENGTH = "Length";
    private static final String VALUE = "Value";
    private static final int LLDPTLV_FIELDS = 3;

    /** OpenFlow OUI. */
    static final byte[] OFOUI = new byte[] { (byte) 0x00, (byte) 0x26, (byte) 0xe1 };

    /** Length of Organizationally defined subtype field of TLV in bytes.   */
    private static final byte CUSTOM_TLV_SUB_TYPE_LENGTH = (byte)1;

    /** OpenFlow subtype: nodeConnectorId of source. */
    private static final byte[] CUSTOM_TLV_SUB_TYPE_NODE_CONNECTOR_ID = new byte[] { 0 };

    /** OpenFlow subtype: custom sec = hash code of verification of origin of LLDP. */
    private static final byte[] CUSTOM_TLV_SUB_TYPE_CUSTOM_SEC = new byte[] { 1 };

    private static final int CUSTOM_TLV_OFFSET = OFOUI.length + CUSTOM_TLV_SUB_TYPE_LENGTH;
    private static final byte[] CHASSISID_SUB_TYPE = new byte[] { 4 }; // MAC address for the system
    private static final byte[] PORTID_SUB_TYPE = new byte[] { 7 }; // locally assigned

    public enum TLVType {
        Unknown((byte) 0), ChassisID((byte) 1), PortID((byte) 2), TTL((byte) 3), PortDesc(
                (byte) 4), SystemName((byte) 5), SystemDesc((byte) 6), SystemCapabilities((byte) 7),
                ManagementAddress((byte) 8), Custom((byte) 127);

        private final byte value;

        TLVType(final byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    private static final Map<String, Pair<Integer, Integer>> FIELD_COORDINATES = new LinkedHashMap<>();

    static {
        FIELD_COORDINATES.put(TYPE, new MutablePair<>(0, 7));
        FIELD_COORDINATES.put(LENGTH, new MutablePair<>(7, 9));
        FIELD_COORDINATES.put(VALUE, new MutablePair<>(16, 0));
    }

    protected Map<String, byte[]> fieldValues;

    /**
     * Default constructor that creates and sets the hash map values and sets the payload to null.
     */
    public LLDPTLV() {
        payload = null;
        fieldValues = new HashMap<>(LLDPTLV_FIELDS);
        hdrFieldCoordMap = FIELD_COORDINATES;
        hdrFieldsMap = fieldValues;
    }

    /**
     * Constructor that writes the passed LLDPTLV values to the hdrFieldsMap.
     */
    public LLDPTLV(final LLDPTLV other) {
        for (Map.Entry<String, byte[]> entry : other.hdrFieldsMap.entrySet()) {
            this.hdrFieldsMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the length of TLV.
     */
    public int getLength() {
        return (int) BitBufferHelper.toNumber(fieldValues.get(LENGTH),
                FIELD_COORDINATES.get(LENGTH).getRight());
    }

    /**
     * Returns the type of TLV.
     */
    public byte getType() {
        return BitBufferHelper.getByte(fieldValues.get(TYPE));
    }

    /**
     * Returns the value field of TLV.
     */
    public byte[] getValue() {
        return fieldValues.get(VALUE);
    }

    /**
     * Sets the type.
     *
     * @param type the type to set
     * @return LLDPTLV
     */
    public LLDPTLV setType(final byte type) {
        byte[] lldpTLVtype = { type };
        fieldValues.put(TYPE, lldpTLVtype);
        return this;
    }

    /**
     * Sets the length.
     *
     * @param length the length to set
     * @return LLDPTLV
     */
    public LLDPTLV setLength(final short length) {
        fieldValues.put(LENGTH, BitBufferHelper.toByteArray(length));
        return this;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     * @return LLDPTLV
     */
    public LLDPTLV setValue(final byte[] value) {
        fieldValues.put(VALUE, value);
        return this;
    }

    @Override
    public void setHeaderField(final String headerField, final byte[] readValue) {
        hdrFieldsMap.put(headerField, readValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + (fieldValues == null ? 0 : fieldValues.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LLDPTLV other = (LLDPTLV) obj;
        if (fieldValues == null) {
            if (other.fieldValues != null) {
                return false;
            }
        } else if (!fieldValues.equals(other.fieldValues)) {
            return false;
        }
        return true;
    }

    @Override
    public int getfieldnumBits(final String fieldName) {
        if (fieldName.equals(VALUE)) {
            return NetUtils.NUM_BITS_IN_A_BYTE * BitBufferHelper.getShort(
                    fieldValues.get(LENGTH), FIELD_COORDINATES.get(LENGTH).getRight());
        }
        return FIELD_COORDINATES.get(fieldName).getRight();
    }

    /**
     * Returns the size in bits of the whole TLV.
     *
     * @return int - size in bits of full TLV
     */
    public int getTLVSize() {
        return LLDPTLV.FIELD_COORDINATES.get(TYPE).getRight() + // static
                LLDPTLV.FIELD_COORDINATES.get(LENGTH).getRight() + // static
                getfieldnumBits(VALUE); // variable
    }

    /**
     * Creates the SystemName TLV value.
     *
     * @param nodeId
     *            node identifier string
     * @return the SystemName TLV value in byte array
     */
    public static byte[] createSystemNameTLVValue(final String nodeId) {
        return nodeId.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates the ChassisID TLV value including the subtype and ChassisID string.
     *
     * @param nodeId
     *            node identifier string
     * @return the ChassisID TLV value in byte array
     */
    public static byte[] createChassisIDTLVValue(final String nodeId) {
        byte[] nid = HexEncode.bytesFromHexString(nodeId);
        byte[] cid = new byte[6];
        int srcPos = 0;
        int dstPos = 0;

        if (nid.length > cid.length) {
            srcPos = nid.length - cid.length;
        } else {
            dstPos = cid.length - nid.length;
        }
        System.arraycopy(nid, srcPos, cid, dstPos, cid.length);

        byte[] cidValue = new byte[cid.length + CHASSISID_SUB_TYPE.length];

        System.arraycopy(CHASSISID_SUB_TYPE, 0, cidValue, 0,
                CHASSISID_SUB_TYPE.length);
        System.arraycopy(cid, 0, cidValue, CHASSISID_SUB_TYPE.length, cid.length);

        return cidValue;
    }

    /**
     * Creates the PortID TLV value including the subtype and PortID string.
     *
     * @param portId
     *            port identifier string
     * @return the PortID TLV value in byte array
     */
    public static byte[] createPortIDTLVValue(final String portId) {
        byte[] pid = portId.getBytes(Charset.defaultCharset());
        byte[] pidValue = new byte[pid.length + PORTID_SUB_TYPE.length];

        System.arraycopy(PORTID_SUB_TYPE, 0, pidValue, 0, PORTID_SUB_TYPE.length);
        System.arraycopy(pid, 0, pidValue, PORTID_SUB_TYPE.length, pid.length);

        return pidValue;
    }

    /**
     * Creates the custom TLV value including OUI, subtype and custom string.
     *
     * @param customString
     *            port identifier string
     * @return the custom TLV value in byte array
     * @see #createCustomTLVValue(byte[],byte[])
     */
    public static byte[] createCustomTLVValue(final String customString) {
        byte[] customByteArray = customString.getBytes(Charset.defaultCharset());
        return createCustomTLVValue(CUSTOM_TLV_SUB_TYPE_NODE_CONNECTOR_ID, customByteArray);
    }

    /**
     * Creates the custom TLV value including OUI, subtype and custom string.
     *
     * @param subtype openflow subtype
     * @param customByteArray
     *            port identifier string
     * @return the custom TLV value in byte array
     */
    public static byte[] createCustomTLVValue(final byte[] subtype, final byte[] customByteArray) {
        byte[] customValue = new byte[CUSTOM_TLV_OFFSET + customByteArray.length];

        System.arraycopy(OFOUI, 0, customValue, 0, OFOUI.length);
        System.arraycopy(subtype, 0, customValue, OFOUI.length, 1);
        System.arraycopy(customByteArray, 0, customValue, CUSTOM_TLV_OFFSET,
                customByteArray.length);

        return customValue;
    }

    /**
     * Creates a custom TLV value including OUI of sub type custom sec and custom bytes value.
     *
     * @param customValue the custom value
     * @return the custom TLV value in byte array
     */
    public static byte[] createSecSubTypeCustomTLVValue(final byte[] customValue) {
        return createCustomTLVValue(CUSTOM_TLV_SUB_TYPE_CUSTOM_SEC, customValue);
    }

    /**
     * Retrieves the string from TLV value and returns it in HexString format.
     *
     * @param tlvValue
     *            the TLV value
     * @param tlvLen
     *            the TLV length
     * @return the HexString
     */
    public static String getHexStringValue(final byte[] tlvValue, final int tlvLen) {
        byte[] cidBytes = new byte[tlvLen - CHASSISID_SUB_TYPE.length];
        System.arraycopy(tlvValue, CHASSISID_SUB_TYPE.length, cidBytes, 0,
                cidBytes.length);
        return HexEncode.bytesToHexStringFormat(cidBytes);
    }

    /**
     * Retrieves the string from TLV value.
     *
     * @param tlvValue
     *            the TLV value
     * @param tlvLen
     *            the TLV length
     * @return the string
     */
    public static String getStringValue(final byte[] tlvValue, final int tlvLen) {
        byte[] pidSubType = new byte[PORTID_SUB_TYPE.length];
        byte[] pidBytes = new byte[tlvLen - PORTID_SUB_TYPE.length];
        System.arraycopy(tlvValue, 0, pidSubType, 0,
                pidSubType.length);
        System.arraycopy(tlvValue, PORTID_SUB_TYPE.length, pidBytes, 0,
                pidBytes.length);
        if (pidSubType[0] == (byte) 0x3) {
            return HexEncode.bytesToHexStringFormat(pidBytes);
        } else {
            return new String(pidBytes, Charset.defaultCharset());
        }
    }

    /**
     * Retrieves the custom string from the Custom TLV value which includes OUI, subtype and custom string.
     *
     * @param customTlvValue
     *            the custom TLV value
     * @param customTlvLen
     *            the custom TLV length
     * @return the custom string
     */
    public static String getCustomString(final byte[] customTlvValue, final int customTlvLen) {
        byte[] vendor = new byte[3];
        System.arraycopy(customTlvValue, 0, vendor, 0, vendor.length);
        if (Arrays.equals(vendor, LLDPTLV.OFOUI)) {
            int customArrayLength = customTlvLen - CUSTOM_TLV_OFFSET;
            byte[] customArray = new byte[customArrayLength];
            System.arraycopy(customTlvValue, CUSTOM_TLV_OFFSET, customArray, 0, customArrayLength);
            return new String(customArray, StandardCharsets.UTF_8);
        }

        return "";
    }

    public static int extractCustomOUI(final LLDPTLV lldptlv) {
        byte[] value = lldptlv.getValue();
        return BitBufferHelper.getInt(ArrayUtils.subarray(value, 0, 3));
    }

    public static byte extractCustomSubtype(final LLDPTLV lldptlv) {
        byte[] value = lldptlv.getValue();
        return BitBufferHelper.getByte(ArrayUtils.subarray(value, 3, 4));
    }

    public static CustomTLVKey createPortSubTypeCustomTLVKey() {
        return new CustomTLVKey(BitBufferHelper.getInt(OFOUI), CUSTOM_TLV_SUB_TYPE_NODE_CONNECTOR_ID[0]);
    }

    public static CustomTLVKey createSecSubTypeCustomTLVKey() {
        return new CustomTLVKey(BitBufferHelper.getInt(LLDPTLV.OFOUI), LLDPTLV.CUSTOM_TLV_SUB_TYPE_CUSTOM_SEC[0]);
    }
}

