/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net.otherpackage;

import org.opendaylight.util.net.*;
import org.junit.Test;

import java.io.*;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * Simple tests to make sure the data-type classes are serializable from
 * another package. (That is, ensure serialization works without access
 * to package private members).
 *
 * @author Simon Hunt
 */
public class SerializationTestSuite {

    private static final String DONT_DELETE_PREFIX = "DD_";
    private static final String TEMP_FILE_PREFIX = "datatype";
    private static final String TEMP_FILE_SUFFIX = ".obj";

    private static long fileSizeInBytes;


    /** Private helper method to take an object, serialize it into
     * a temporary file, and return the name of the temp file.
     *
     * @param datatype the object to be serialized
     * @return the name of the temporary file created
     * @throws java.io.IOException if there was a problem
     */
    private static String save(Object datatype) throws IOException {
        return save(datatype, keepTempFiles());
    }

    /** Private helper method to take an object, serialize it into
     * a temporary file, and return the name of the temp file.
     *
     * @param datatype the object to be serialized
     * @param dontDelete if true, the temp file is not deleted, and its
     *                   name is written to stdout
     * @return the name of the temporary file created
     * @throws IOException if there was a problem
     */
    private static String save(Object datatype, boolean dontDelete)
            throws IOException {
        String filePrefix = dontDelete ? DONT_DELETE_PREFIX + TEMP_FILE_PREFIX
                                       : TEMP_FILE_PREFIX;
        File tempFile = File.createTempFile(filePrefix, TEMP_FILE_SUFFIX);
        FileOutputStream fos = new FileOutputStream(tempFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(datatype);
        oos.close();
        fos.close();
        if (dontDelete)
            print("Serialized File: " + tempFile.getPath());
        return tempFile.getPath();
    }

    /** Private helper method to take the name of a temp file, and
     * deserialize the object contained within.
     *
     * @param aFileName the name of the temp file
     * @return the deserialized object
     * @throws IOException if there was a problem
     * @throws ClassNotFoundException if the deserialized class was not found
     */
    private static Object load(String aFileName)
            throws IOException, ClassNotFoundException {
        File tempFile = new File(aFileName);
        fileSizeInBytes = tempFile.length();
        FileInputStream fis = new FileInputStream(tempFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object datatype = ois.readObject();
        ois.close();
        fis.close();
        if (!tempFile.getName().startsWith(DONT_DELETE_PREFIX))
            tempFile.delete();
        return datatype;
    }

    /** helper method to compare the relative sizes of the serialized
     * object vs. its toString() representation. It is assumed that the
     * object passed in here was the one that was just {@link #load loaded}.
     *
     * @param o the object
     * @throws java.io.IOException if issues
     * @throws ClassNotFoundException if issues
     */
    private static void compareSizes(Object o)
            throws IOException, ClassNotFoundException {
        long serSize = fileSizeInBytes;

        // long strLen = o.toString().getBytes().length;
        // NOTE: String.getBytes() converts the string to a byte array using
        // a character encoding but strings use Unicode chars which
        // are 2 bytes per char, plus some overhead, so we
        //   serialize the string to a file and examine the file size
        String asString = o.toString();
        String tmp = save(asString);
        load(tmp);
        long stringSize = fileSizeInBytes;

        print(" Comparing sizes for " + o);
        StringBuilder sb = new StringBuilder(" SIZES: String[")
                .append(asString.length())
                .append("] = ")
                .append(stringSize)
                .append(" bytes, Ser.Object = ")
                .append(serSize)
                .append(" bytes.  Delta: ")
                .append(serSize - stringSize)
                .append(EOL);
        print(sb);
    }


    //============================
    //=== IpProtocol
    //============================

    @Test
    public void serializedIpProtocolTcp1()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpProtocolTcp1()");
        IpProtocol tcp = IpProtocol.TCP;
        String tmp = save(tcp);
        IpProtocol ippCopy = (IpProtocol) load(tmp);
        compareSizes(ippCopy);

        assertSame(AM_NSR, tcp, ippCopy);
    }

    @Test
    public void serializedIpProtocolTcp2()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpProtocolTcp2()");
        IpProtocol tcp = IpProtocol.valueOf("tcp");
        String tmp = save(tcp);
        IpProtocol ippCopy = (IpProtocol) load(tmp);
        compareSizes(ippCopy);

        assertSame(AM_NSR, tcp, ippCopy);
    }

    @Test
    public void serializedIpProtocolTcp3()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpProtocolTcp3()");
        IpProtocol tcp = IpProtocol.valueOf("tcp");
        String tmp = save(tcp);
        IpProtocol ippCopy = (IpProtocol) load(tmp);
        compareSizes(ippCopy);

        assertSame(AM_NSR, IpProtocol.TCP, ippCopy);
    }


    //============================
    //=== TcpUdpPort
    //============================

    @Test
    public void serializedTcpUdpPortFtp1()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedTcpUdpPortFtp1()");
        TcpUdpPort ftp = TcpUdpPort.valueOf("21/tcp");
        String tmp = save(ftp);
        TcpUdpPort ftpCopy = (TcpUdpPort) load(tmp);
        compareSizes(ftpCopy);

        TcpUdpPort ftp2 = TcpUdpPort.tcpPort(21);
        assertSame(AM_NSR, ftp2, ftpCopy);
    }


    //==============================
    //=== AlphaNumericName
    //==============================

    @Test
    public void serializedAlphaNumericName()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedAlphaNumericName()");
        AlphaNumericName ann = AlphaNumericName.valueOf("B7");
        String tmp = save(ann);
        AlphaNumericName annCopy = (AlphaNumericName) load(tmp);
        compareSizes(annCopy);

        assertSame(AM_NSR, ann, annCopy);
    }


    //============================
    //=== DnsName
    //============================

    @Test
    public void serializedDnsName()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedDnsName()");
        DnsName simon = DnsName.valueOf("simon.rose.hp.com");
        String tmp = save(simon);
        DnsName simonCopy = (DnsName) load(tmp);
        compareSizes(simonCopy);
        assertSame(AM_NSR, simon, simonCopy);

        DnsName jesse = DnsName.valueOf("jesse.rose.hp.com");
        tmp = save(jesse);
        DnsName jesseCopy = (DnsName) load(tmp);
        compareSizes(jesseCopy);
        assertSame(AM_NSR, jesse, jesseCopy);

        assertSame(AM_NSR, simonCopy.getDomainName(),
                jesseCopy.getDomainName()); // uses StringPool
    }

    @Test
    public void serializedUnresolvableDnsName()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedUnresolvableDnsName()");
        DnsName unres = DnsName.UNRESOLVABLE;
        String tmp = save(unres);
        DnsName copy = (DnsName) load(tmp);
        compareSizes(copy);
        assertSame(AM_NSR, DnsName.UNRESOLVABLE, copy);
    }


    //============================
    //=== MacAddress
    //============================
    @Test
    public void serializedMacAddress()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedMacAddress()");
        MacAddress macOrig = MacAddress.valueOf("00004e-0102ff");
        long longOrig = macOrig.toLong();
        String strOrig = macOrig.toString();
        byte[] byteOrig = macOrig.toByteArray();

        String tmp = save(macOrig);
        MacAddress macCopy = (MacAddress) load(tmp);
        compareSizes(macCopy);

        assertEquals(AM_NSR, macOrig, macCopy);

        assertEquals(AM_HUH, longOrig, macCopy.toLong());
        assertEquals(AM_HUH, strOrig, macCopy.toString());
        assertArrayEquals(AM_HUH, byteOrig, macCopy.toByteArray());
    }


    //============================
    //=== IpAddress
    //============================
    @Test
    public void serializedIpV4Address()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpV4Address()");
        IpAddress ipOrig = IpAddress.valueOf("15.3.2.56");
        byte[] bytesOrig = ipOrig.toByteArray();
        String strOrig = ipOrig.toString();

        String tmp = save(ipOrig);
        IpAddress ipCopy = (IpAddress) load(tmp);
        compareSizes(ipCopy);

        assertEquals(AM_NSR, ipOrig, ipCopy);
        assertArrayEquals(AM_HUH, bytesOrig, ipCopy.toByteArray());
        assertEquals(AM_HUH, strOrig, ipCopy.toString());

    }

    @Test
    public void serializedIpV4Loopback()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpV4Loopback()");
        IpAddress lb = IpAddress.LOOPBACK_IPv4;
        String tmp = save(lb);
        IpAddress copy = (IpAddress) load(tmp);
        compareSizes(copy);

        assertEquals(AM_HUH, IpAddress.Family.IPv4, copy.getFamily());
        assertTrue(AM_HUH, copy.isLoopback());
        assertEquals(AM_HUH, lb, copy);
    }

    @Test
    public void serializedIpV4Undetermined()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpV4Undetermined()");
        IpAddress un = IpAddress.UNDETERMINED_IPv4;
        String tmp = save(un);
        IpAddress copy = (IpAddress) load(tmp);
        compareSizes(copy);

        assertEquals(AM_HUH, IpAddress.Family.IPv4, copy.getFamily());
        assertTrue(AM_HUH, copy.isUndetermined());
        assertEquals(AM_NSR, un, copy);
    }

    @Test
    public void serializedIpV6Address()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpV6Address()");
        IpAddress ipOrig = IpAddress.valueOf("FEDC:0:1::AB:23");
        byte[] bytesOrig = ipOrig.toByteArray();
        String strOrig = ipOrig.toString();

        String tmp = save(ipOrig);
        IpAddress ipCopy = (IpAddress) load(tmp);
        compareSizes(ipCopy);

        assertEquals(AM_NSR, ipOrig, ipCopy);
        assertArrayEquals(AM_HUH, bytesOrig, ipCopy.toByteArray());
        assertEquals(AM_HUH, strOrig, ipCopy.toString());

    }

    @Test
    public void serializedIpV6Loopback()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpV6Loopback()");
        IpAddress lb = IpAddress.LOOPBACK_IPv6;
        String tmp = save(lb);
        IpAddress copy = (IpAddress) load(tmp);
        compareSizes(copy);

        assertEquals(AM_HUH, IpAddress.Family.IPv6, copy.getFamily());
        assertTrue(AM_HUH, copy.isLoopback());
        assertEquals(AM_NSR, lb, copy);
    }

    @Test
    public void serializedIpV6Undetermined()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpV6Undetermined()");
        IpAddress un = IpAddress.UNDETERMINED_IPv6;
        String tmp = save(un);
        IpAddress copy = (IpAddress) load(tmp);
        compareSizes(copy);

        assertEquals(AM_HUH, IpAddress.Family.IPv6, copy.getFamily());
        assertTrue(AM_HUH, copy.isUndetermined());
        assertEquals(AM_NSR, un, copy);
    }


    //============================
    //=== IpDnsPair
    //============================
    @Test
    public void serializedIpDnsPair1()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpDnsPair1()");
        IpAddress ipOrig = IpAddress.valueOf("FEDC:0:1::AB:23");
        IpDnsPair pairOrig = IpDnsPair.valueOf(ipOrig);

        String tmp = save(pairOrig);
        IpDnsPair pairCopy = (IpDnsPair) load(tmp);
        compareSizes(pairCopy);

        assertEquals(AM_NSR, pairOrig, pairCopy);
        assertEquals(AM_NSR, ipOrig, pairCopy.getIp());
    }

    @Test
    public void serializedIpDnsPair2()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpDnsPair2()");
        IpAddress ipOrig = IpAddress.valueOf("15.45.23.100");
        DnsName dnsOrig = DnsName.valueOf("frodo.rose.hp.com");
        IpDnsPair pairOrig = IpDnsPair.valueOf(ipOrig, dnsOrig);

        String tmp = save(pairOrig);
        IpDnsPair pairCopy = (IpDnsPair) load(tmp);
        compareSizes(pairCopy);

        assertEquals(AM_NSR, pairOrig, pairCopy);
        IpAddress ipCopy = pairCopy.getIp();
        DnsName dnsCopy = pairCopy.getDns();
        assertEquals(AM_NSR, ipOrig, ipCopy);
        assertEquals(AM_NSR, dnsOrig, dnsCopy);
    }


    //============================
    //=== TcpUdpPortPair
    //============================
    @Test
    public void serializedPortPair1()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedPortPair1()");
        IpAddress srcIpOrig = IpAddress.valueOf("FEDC:0:1::AB:23");
        TcpUdpPort srcPortOrig = TcpUdpPort.valueOf("17/udp");
        IpAddress dstIpOrig = IpAddress.valueOf("15.23.45.67");
        TcpUdpPort dstPortOrig = TcpUdpPort.valueOf("19999/udp");

        TcpUdpPortPair pairOrig = TcpUdpPortPair.valueOf(srcIpOrig, srcPortOrig,
                dstIpOrig, dstPortOrig);

        String tmp = save(pairOrig);
        TcpUdpPortPair pairCopy = (TcpUdpPortPair) load(tmp);
        compareSizes(pairCopy);

        assertSame(AM_NSR, pairOrig, pairCopy);

        IpAddress srcIpCopy = pairCopy.getSourceIp();
        TcpUdpPort srcPortCopy = pairCopy.getSourcePort();
        IpAddress dstIpCopy = pairCopy.getDestinationIp();
        TcpUdpPort dstPortCopy = pairCopy.getDestinationPort();

        assertSame(AM_NSR, srcIpOrig, srcIpCopy);
        assertSame(AM_NSR, srcPortOrig, srcPortCopy);
        assertSame(AM_NSR, dstIpOrig, dstIpCopy);
        assertSame(AM_NSR, dstPortOrig, dstPortCopy);
    }

    @Test
    public void serializedPortPair2()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedPortPair2()");
        // undetermined source
        IpAddress srcIpOrig = IpAddress.UNDETERMINED_IPv4;
        TcpUdpPort srcPortOrig = TcpUdpPort.UNDETERMINED_UDP;
        // "listening" on dest
        IpAddress dstIpOrig = IpAddress.valueOf("1.2.3.4");
        TcpUdpPort dstPortOrig = TcpUdpPort.valueOf("17/udp");

        TcpUdpPortPair pairOrig = TcpUdpPortPair.valueOf(srcIpOrig, srcPortOrig,
                dstIpOrig, dstPortOrig);

        String tmp = save(pairOrig);
        TcpUdpPortPair pairCopy = (TcpUdpPortPair) load(tmp);
        compareSizes(pairCopy);

        assertSame(AM_NSR, pairOrig, pairCopy);

        assertTrue(AM_HUH, pairCopy.getSourceIp().isUndetermined());
        assertTrue(AM_HUH, pairCopy.getSourcePort().isUndetermined());

        assertSame(AM_NSR, dstIpOrig, pairCopy.getDestinationIp());
        assertSame(AM_NSR, dstPortOrig, pairCopy.getDestinationPort());
    }


    //============================
    //=== IpRange
    //============================

    @Test
    public void serializedIpRange()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedIpRange()");
        String spec = "15.37.12-13.*";
        IpRange rangeOrig = IpRange.valueOf(spec);
        print(rangeOrig.toDebugString());

        String tmp = save(rangeOrig);
        IpRange rangeCopy = (IpRange) load(tmp);
        compareSizes(rangeCopy);
        print(rangeCopy.toDebugString());

        assertSame(AM_NSR, rangeOrig, rangeCopy);
        // if the above assertion is true, then the following must be true
        assertEquals(AM_NEQ, rangeOrig, rangeCopy);
        assertEquals(AM_NEQ, rangeOrig.first(), rangeCopy.first());
        assertEquals(AM_NEQ, rangeOrig.last(), rangeCopy.last());
        assertEquals(AM_NEQ, rangeOrig.size(), rangeCopy.size());
    }


    //============================
    //=== MacRange
    //============================

    @Test
    public void serializedMacRange()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedMacRange()");
        String spec = "15:37:12:15-18:*:0a-1a";
        MacRange rangeOrig = MacRange.valueOf(spec);
        print(rangeOrig.toDebugString());

        String tmp = save(rangeOrig);
        MacRange rangeCopy = (MacRange) load(tmp);
        compareSizes(rangeCopy);
        print(rangeCopy.toDebugString());

        assertSame(AM_NSR, rangeOrig, rangeCopy);
        // if the above assertion is true, then the following must be true
        assertEquals(AM_NEQ, rangeOrig, rangeCopy);
        assertEquals(AM_NEQ, rangeOrig.first(), rangeCopy.first());
        assertEquals(AM_NEQ, rangeOrig.last(), rangeCopy.last());
        assertEquals(AM_NEQ, rangeOrig.size(), rangeCopy.size());
    }


    //============================
    //=== MacPrefix
    //============================

    @Test
    public void serializedMacPrefix()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedMacPrefix()");
        String spec = "ff:ee:ab:12";
        MacPrefix prefixOrig = MacPrefix.valueOf(spec);
        print(prefixOrig);

        String tmp = save(prefixOrig);
        MacPrefix prefixCopy = (MacPrefix) load(tmp);
        compareSizes(prefixCopy);
        print(prefixCopy);

        assertSame(AM_NSR, prefixOrig, prefixCopy);
        // if the above assertion is true, then the following must be true
        verifyEqual(prefixOrig, prefixCopy);
    }


    //============================
    //=== SubnetMask
    //============================

    @Test
    public void serializedSubnetMask()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedSubnetMask()");
        SubnetMask mask = SubnetMask.MASK_255_255_0_0;
        int bits = mask.getOneBitCount();
        print(mask.toDebugString());

        String tmp = save(mask);
        SubnetMask copy = (SubnetMask) load(tmp);
        compareSizes(copy);
        print(copy.toDebugString());

        verifyEqual(mask, copy);
        assertSame(AM_NSR, mask, copy);
        assertEquals(AM_NEQ, bits, copy.getOneBitCount());
    }


    //============================
    //=== Subnet
    //============================

    @Test
    public void serializedSubnet()
            throws IOException, ClassNotFoundException {
        print (EOL + "serializedSubnet()");
        final String s = "192.168.3.0/18";
        Subnet subnet = Subnet.valueOf(s);
        print(subnet.toDebugString());

        String tmp = save(subnet);
        Subnet copy = (Subnet) load(tmp);
        compareSizes(copy);
        print(copy.toDebugString());

        verifyEqual(subnet, copy);
        assertSame(AM_NSR, subnet, copy);

        // should be comparing "192.168.3.0/18" with "192.168.0.0/18"
        //   since the subnet mask (/18 CIDR) clears the bottom 6 bits
        //   of the third byte (the 3 becomes a 0).
        verifyNotEqual(s, copy.toString());
    }


    //============================
    //=== PartialSubnet
    //============================

    @Test
    public void serializedPartialSubnet()
            throws IOException, ClassNotFoundException {
        print (EOL + "serializedPartialSubnet()");
        final String s = "192.168.3.0/24,192.168.3.30-39";
        PartialSubnet partial = PartialSubnet.valueOf(s);
        print(partial.toDebugString());

        String tmp = save(partial);
        PartialSubnet copy = (PartialSubnet) load(tmp);
        compareSizes(copy);
        print(copy.toDebugString());

        verifyEqual(partial, copy);
        assertSame(AM_NSR, partial, copy);
        assertEquals(AM_NEQ, s, copy.toString());
    }


    //============================
    //=== PortNumber
    //============================

    @Test
    public void serializedPortNumber()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedPortNumber()");
        final int kMinusOne = 1023;
        PortNumber pn = PortNumber.valueOf(kMinusOne);
        print(pn);

        String tmp = save(pn);
        PortNumber copy = (PortNumber) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(pn, copy);
        assertSame(AM_NSR, pn, copy);
        assertEquals(AM_NEQ, kMinusOne, copy.toInt());
    }


    //============================
    //=== BigPortNumber
    //============================

    @Test
    public void serializedBigPortNumber()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedBigPortNumber()");
        final long big = 0xffffffffL;
        BigPortNumber bpn = BigPortNumber.valueOf(big);
        print(bpn);

        String tmp = save(bpn);
        BigPortNumber copy = (BigPortNumber) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(bpn, copy);
        assertEquals(AM_NSR, bpn, copy);
        assertEquals(AM_NEQ, big, copy.toLong());
    }


    //============================
    //=== ICMPv6Type
    //============================

    @Test
    public void serializedICMPv6Type()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedICMPv6Type()");
        final int code = 136;
        ICMPv6Type type = ICMPv6Type.valueOf(code);
        print(type);

        String tmp = save(type);
        ICMPv6Type copy = (ICMPv6Type) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(type, copy);
        assertSame(AM_NSR, type, copy);
        assertEquals(AM_NEQ, code, copy.getCode());
    }


    //============================
    //=== ICMPv4Type
    //============================

    @Test
    public void serializedICMPv4Type()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedICMPv4Type()");
        final int code = 136;
        ICMPv4Type type = ICMPv4Type.valueOf(code);
        print(type);

        String tmp = save(type);
        ICMPv4Type copy = (ICMPv4Type) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(type, copy);
        assertSame(AM_NSR, type, copy);
        assertEquals(AM_NEQ, code, copy.getCode());
    }

}
