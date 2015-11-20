/**
 * Copyright (c) 2013, 2015 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ipv4SubnetUtils {
    private long netAddr;
    private long netMask;
    private long addr;
    private int prefix;
    private boolean isValidIP = false;

    public boolean checkIfValidIP(String cidrNotation) {
    boolean valid = false;
    calculateAndValidateIP(cidrNotation);

    if (addr == netAddr) {
        valid = true;
    }
    return valid;
    }

    public void calculateAndValidateIP(String cidrNotation) {

    String[] parts = cidrNotation.split("/");
    String ip = parts[0];

    try {
        if (parts.length < 2) {
        prefix = 0;
        } else {
        prefix = Integer.parseInt(parts[1]);
        }
        long mask = 0xffffffff << (32 - prefix);
        //System.out.println("Address=" + ip + "\nPrefix=" + prefix);

        byte[] netMaskBytes = longToByteArray(mask);

        netMask = addrToLong(InetAddress.getByAddress(netMaskBytes));
        setAddr(addrToLong((Inet4Address) InetAddress.getByName(ip)));

        // Calculate network address of ip
        setNetAddr(getAddr() & netMask);

        Inet4Address netAddrInet = (Inet4Address) toInetAddress(getNetAddr());
        //System.out.println("netAddrInet=" + netAddrInet.getHostAddress());

    } catch (UnknownHostException e) {
        System.out.println(e.getMessage());

    }
    }

    public boolean isValidIP() {
    return isValidIP;
    }

    public void setValidIP(boolean isValidIP) {
    this.isValidIP = isValidIP;
    }

    private byte[] longToByteArray(long value) {
    byte[] netMaskBytes = new byte[] { (byte) (value >>> 24), (byte) (value >> 16 & 0xff),
        (byte) (value >> 8 & 0xff), (byte) (value & 0xff) };
    return netMaskBytes;
    }

    // Inet IP to int
    private long addrToLong(InetAddress i4addr) {
    byte[] ba = i4addr.getAddress();
    return (ba[0] << 24) | ((ba[1] & 0xFF) << 16) | ((ba[2] & 0xFF) << 8) | (ba[3] & 0xFF);
    }

    public static InetAddress toInetAddress(long ipAddress) throws UnknownHostException {
    byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
    InetAddress address = InetAddress.getByAddress(bytes);
    return address;
    }

    public long getNetAddr() {
    return netAddr;
    }

    public void setNetAddr(long netAddr) {
    this.netAddr = netAddr;
    }

    public long getAddr() {
    return addr;
    }

    public void setAddr(long addr) {
    this.addr = addr;
    }
    /*
     * public static void main(String[] args) { String addr = "10.10.16.0/20";
     * // calculate(addr); }
     */
}
