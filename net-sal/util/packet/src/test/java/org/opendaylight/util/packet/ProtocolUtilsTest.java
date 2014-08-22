/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.packet.ProtocolUtils.getEnum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.zip.Adler32;

import org.junit.Test;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.packet.ProtocolUtils.Crc32c;


/**
 * Protocol utilities unit tests.
 *
 * @author Frank Wood
 */
public class ProtocolUtilsTest extends PacketTest {

    @Test
    public void checkSum() {
        PacketWriter w = new PacketWriter(10 * 2);
        w.writeU16(0x4500);
        w.writeU16(0x003c);
        
        int cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0x453c & 0xffff, cs);
        
        w.writeU16(0x1c46);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0x6182 & 0xffff, cs);
        
        w.writeU16(0x4000);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0xa182 & 0xffff, cs);
        
        w.writeU16(0x4006);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0xe188 & 0xffff, cs);
        
        w.writeU16(0x0000);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0xe188 & 0xffff, cs);
        
        w.writeU16(0xac10);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0x8d99 & 0xffff, cs); // one odd dbit (carry)
        
        w.writeU16(0x0a63);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0x97fc & 0xffff, cs);
        
        w.writeU16(0xac10);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(~0x440d & 0xffff, cs); // one odd bit (carry)
        
        w.writeU16(0x0a0c);
        
        cs = ProtocolUtils.checkSumU16(w);
        assertEquals(0xb1e6 & 0xffff, cs);
    }
    
    @Test
    public void checkSumIcmp() {
        String s = "abcdefghijklmnopqrstuvwabcdefghi";
        
        PacketWriter w = new PacketWriter(10 + s.length());
        w.writeU16(0x0800);
        w.writeU16(0x0000);
        w.writeU16(0x0000);
        w.writeU16(0x0001);
        w.writeU16(0x0004);
        w.writeString(s);
        
        int cs = ProtocolUtils.checkSumU16(w);
        assertEquals(0x4d57, cs);
    }
    
    private enum TestEnum implements ProtocolEnum {
        BIT1(1),
        BIT2(2),
        BIT3(4),
        ;
        private int code;
        private TestEnum(int code) {this.code = code;}
        @Override
        public int code() {return code;}
    }
    
    @Test
    public void getEnumFromCode() {
        assertEquals(TestEnum.BIT3, getEnum(TestEnum.class, 4, TestEnum.BIT1));
        assertEquals(TestEnum.BIT1, getEnum(TestEnum.class, 33, TestEnum.BIT1));
    }
    
    @Test
    public void getEnums() {
        List<TestEnum> l = ProtocolUtils.getEnums(TestEnum.class, 0x03);
        assertEquals(2, l.size());
        assertEquals(TestEnum.BIT1, l.get(0));
        assertEquals(TestEnum.BIT2, l.get(1));
    }
    
    @Test
    public void hasEnums() {
        assertTrue(ProtocolUtils.has(0x05, TestEnum.BIT1, TestEnum.BIT3));
        assertFalse(ProtocolUtils.has(0x05, TestEnum.BIT2));
    }
    
    @Test
    public void getMask() {
        assertEquals(0x05, ProtocolUtils.getMask(TestEnum.class,
                                                 TestEnum.BIT1,
                                                 TestEnum.BIT3));
    }
    
    @Test
    public void contains() {
        TestEnum[] enums = new TestEnum[] {TestEnum.BIT1, TestEnum.BIT3};
        assertTrue(ProtocolUtils.contains(enums, TestEnum.BIT1, TestEnum.BIT3));
        assertFalse(ProtocolUtils.contains(enums, TestEnum.BIT2));
    }
    
    @Test
    public void sctpAdler32checkSum() {
        byte[] bytes = new byte[] {
            0x0b, (byte)0x80, 0x40, 0x00, 0x21, 0x44, 0x15, 0x23,
            0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x10,
            0x28, 0x02, 0x43, 0x45, 0x00, 0x00, 0x20, 0x00,
            0x00, 0x00, 0x00, 0x00
        };

        Adler32 a32 = new Adler32();
        a32.update(bytes);
        
        assertEquals(0x2bf2024e, a32.getValue());
    }
    
    @Test
    public void sctpCrc32cCheckSum() {
        byte[] bytes = new byte[] {
            (byte)0x80, 0x44, 0x00, 0x50, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x3c,
            0x3b, (byte)0xb9, (byte)0x9c, 0x46, 0x00, 0x01, (byte)0xa0, 0x00,
            0x00, 0x0a, (byte)0xff, (byte)0xff, 0x2b, 0x2d, 0x7e, (byte)0xb2,
            0x00, 0x05, 0x00, 0x08, (byte)0x9b, (byte)0xe6, 0x18, (byte)0x9b,
            0x00, 0x05, 0x00, 0x08, (byte)0x9b, (byte)0xe6, 0x18, (byte)0x9c,
            0x00, 0x0c, 0x00, 0x06, 0x00, 0x05, 0x00, 0x00,
            (byte)0x80, 0x00, 0x00, 0x04, (byte)0xc0, 0x00, 0x00, 0x04,
            (byte)0xc0, 0x06, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00                
        };

        assertEquals(0x30baef54, Crc32c.checkSumU32(bytes));
    }    
    
    @Test
    public void icmpCheckSumV6() {
        byte[] bytes = new byte[] {
            (byte)0x87, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte)0xfe, (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x02, 0x60, (byte)0x97, (byte)0xff, (byte)0xfe, (byte)0x07,
            (byte)0x69, (byte)0xea, (byte)0x01, (byte)0x01, (byte)0x00,
            0x00, (byte)0x86, 0x05, (byte)0x80, (byte)0xda
        };
        PacketWriter pw = new PacketWriter(bytes.length);
        pw.writeBytes(bytes);
        
        IpPseudoHdr phdr = new IpPseudoHdr(
            IpAddress.valueOf("fe80::200:86ff:fe05:80da"),
            IpAddress.valueOf("fe80::260:97ff:fe07:69ea"),
            IpType.IPV6_ICMP
        ).len(bytes.length);
        
        assertEquals(0x068bd, ProtocolUtils.checkSumU16(phdr, pw));        
    }
}
