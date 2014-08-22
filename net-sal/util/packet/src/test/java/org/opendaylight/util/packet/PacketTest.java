/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.ICMPv4Type;
import org.opendaylight.util.net.ICMPv6Type;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.IpProtocol;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.PortNumber;


/**
 * Base class for unit tests.
 *
 * @author Frank Wood
 */
public abstract class PacketTest {

    protected static IpAddress ip(String s) {return IpAddress.valueOf(s);}
    protected static MacAddress mac(String s) {return MacAddress.valueOf(s);}

    private static final String E_BAD_FILE = "Couldn't read test data from: ";
    private static final String TEST_FILE_ROOT = "org/opendaylight/util/packet/";

    private final ClassLoader cl = getClass().getClassLoader();
    
    protected static final String PACKET_DATA = "packet.hex";
    protected static final int B = 256;

    protected static final byte[] EXP_MAGIC = {0xca-B, 0xfe-B, 0xba-B, 0xbe-B };

    protected static final IpAddress EXP_IPv4 = ip("15.255.125.36");
    protected static final byte[] EXP_IPv4_ARRAY = {0xf, 0xff-B, 0x7d, 0x24};

    protected static final IpAddress EXP_IPv6 = ip("cafe::b00b");
    protected static final byte[] EXP_IPv6_ARRAY = {
            0xca-B, 0xfe-B, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0xb0-B, 0xb
    };

    protected static final MacAddress EXP_MAC = mac("00:01:e7:fa:1a:1a");
    protected static final byte[] EXP_MAC_ARRAY =
            {0x00, 0x01, 0xe7-B, 0xfa-B, 0x1a, 0x1a};

    protected static final EthernetType EXP_ETHER = EthernetType.SNMP;
    protected static final byte[] EXP_ETHER_ARRAY = {0x81-B, 0x4c};

    protected static final IpProtocol EXP_IPP = IpProtocol.TCP;
    protected static final byte[] EXP_IPP_ARRAY = {0x06};

    protected static final ICMPv4Type EXP_ICMPv4 = ICMPv4Type.valueOf("echo_req");
    protected static final byte[] EXP_ICMPv4_ARRAY = {0x08};

    protected static final ICMPv6Type EXP_ICMPv6 = ICMPv6Type.NBR_SOL;
    protected static final byte[] EXP_ICMPv6_ARRAY = {0x87-B};

    protected static final PortNumber EXP_PORT = PortNumber.valueOf(0x42);
    protected static final byte[] EXP_PORT_ARRAY = {0x0, 0x42};

    protected static final BigPortNumber EXP_BPORT =
            BigPortNumber.valueOf(0x11223344L);
    protected static final byte[] EXP_BPORT_ARRAY = {0x11, 0x22, 0x33, 0x44};

    protected static final byte EXP_BYTE = 0xff-B;
    protected static final byte[] EXP_BYTE_ARRAY = {0xff-B};

    protected static final byte EXP_SKIP_BYTE = 9;
    protected static final byte[] SKIPPED_BYTES = { 9, 9, 9, 9, 9, 9 };
    protected static final byte[] EXP_ZEROED_BYTES = { 0, 0, 0, 0, 9, 9 };

    protected static final int EXP_INT = -3;
    protected static final byte[] EXP_INT_ARRAY = {0xff-B, 0xff-B, 0xff-B, 0xfd-B};

    protected static final long EXP_LONG = -6;
    protected static final byte[] EXP_LONG_ARRAY = {
            0xff-B, 0xff-B, 0xff-B, 0xff-B, 0xff-B, 0xff-B, 0xff-B, 0xfa-B
    };

    protected static final short EXP_U8 = 129;
    protected static final byte[] EXP_U8_ARRAY = {0x81-B};

    protected static final int EXP_U16 = 65530;
    protected static final byte[] EXP_U16_ARRAY = {0xff-B, 0xfa-B};

    protected static final int EXP_U24 = 0xfa1a1a;
    protected static final byte[] EXP_U24_ARRAY = {0xfa-B, 0x1a, 0x1a};

    protected static final long EXP_U32 = 0xc234abcdL;
    protected static final byte[] EXP_U32_ARRAY = {0xc2-B, 0x34, 0xab-B, 0xcd-B};

    protected static final String EXP_STR = "Lord of the Rings, by J.R.R. Tolkien";
    protected static final int STR_FIELD_LEN = 48;
    protected static final byte[] EXP_STR_ARRAY = {
            0x4c, 0x6f, 0x72, 0x64, 0x20, 0x6f, 0x66, 0x20,
            0x74, 0x68, 0x65, 0x20, 0x52, 0x69, 0x6e, 0x67,
            0x73, 0x2c, 0x20, 0x62, 0x79, 0x20, 0x4a, 0x2e,
            0x52, 0x2e, 0x52, 0x2e, 0x20, 0x54, 0x6f, 0x6c,
            0x6b, 0x69, 0x65, 0x6e, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };
    
    /**
     * Returns a byte array slurped from a test .hex file.
     * The given path is relative to com/hp/util/pkt/.
     *
     * @param path the test file path
     * @return a byte array containing the data
     */
    protected byte[] slurpedBytes(String path) {
        String filename = TEST_FILE_ROOT + path;
        byte[] packet = null;
        try {
            packet = ByteUtils.slurpBytesFromHexFile(filename, cl);
            if (packet == null) {
                fail(E_BAD_FILE + filename);
            }
        } catch (IOException e) {
            fail(E_BAD_FILE + filename);
        }
        return packet;
    }

    /** Returns a packet reader wrapping a byte array slurped from a test file.
     * The given path is relative to com/hp/sdn/of/.
     *
     * @param path the test file path
     * @return a packet reader for the given file
     */
    protected PacketReader getPacketReader(String path) {
        return new PacketReader(ByteBuffer.wrap(slurpedBytes(path)));
    }

}
