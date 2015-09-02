/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import java.lang.Long;
import java.math.BigInteger;
import java.lang.Integer;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpShaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxArpThaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNshc4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNsiKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxNspKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg0Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg1Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg2Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg3Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg4Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg5Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg6Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxReg7Key;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIdKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4SrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpOpKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpSpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthTypeKey;

/**
 * This class converts Nicira Extension Match to simple data type. 
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class NxmData {
    Long nxmNxArpSha;
    Long nxmNxArpTha;
    Long nxmNxNshc1;
    Long nxmNxNshc2;
    Long nxmNxNshc3;
    Long nxmNxNshc4;
    Short nxmNxNsi;
    Long nxmNxNsp;
    Long nxmNxReg0;
    Long nxmNxReg1;
    Long nxmNxReg2;
    Long nxmNxReg3;
    Long nxmNxReg4;
    Long nxmNxReg5;
    Long nxmNxReg6;
    Long nxmNxReg7;
    BigInteger nxmNxTunId;
    Long nxmNxTunIpv4Dst;
    Long nxmNxTunIpv4Src;
    Integer nxmOfArpOp;
    Long nxmOfArpSpa;
    Long nxmOfArpTpa;
    Long nxmOfEthDst;
    Long nxmOfEthSrc;
    Integer nxmOfEthType;

    public NxmData() {
        this.nxmNxArpSha = Long.valueOf(-1);
        this.nxmNxArpTha = Long.valueOf(-1);
        this.nxmNxNshc1 = Long.valueOf(-1);
        this.nxmNxNshc2 = Long.valueOf(-1);
        this.nxmNxNshc3 = Long.valueOf(-1);
        this.nxmNxNshc4 = Long.valueOf(-1);
        this.nxmNxNsi = Short.valueOf((short)-1);
        this.nxmNxNsp = Long.valueOf(-1);
        this.nxmNxReg0 = Long.valueOf(-1);
        this.nxmNxReg1 = Long.valueOf(-1);
        this.nxmNxReg2 = Long.valueOf(-1);
        this.nxmNxReg3 = Long.valueOf(-1);
        this.nxmNxReg4 = Long.valueOf(-1);
        this.nxmNxReg5 = Long.valueOf(-1);
        this.nxmNxReg6 = Long.valueOf(-1);
        this.nxmNxReg7 = Long.valueOf(-1);
        this.nxmNxTunId = BigInteger.valueOf(-1);
        this.nxmNxTunIpv4Dst = Long.valueOf(-1);
        this.nxmNxTunIpv4Src = Long.valueOf(-1);
        this.nxmOfArpOp = Integer.valueOf(-1);
        this.nxmOfArpSpa = Long.valueOf(-1);
        this.nxmOfArpTpa = Long.valueOf(-1);
        this.nxmOfEthDst = Long.valueOf(-1);
        this.nxmOfEthSrc = Long.valueOf(-1);
        this.nxmOfEthType = Integer.valueOf(-1);
    }

    public void setNxmNxArpSha(Long nxmNxArpSha) { this.nxmNxArpSha = nxmNxArpSha; }
    public void setNxmNxArpTha(Long nxmNxArpTha) { this.nxmNxArpTha = nxmNxArpTha; }
    public void setNxmNxNshc1(Long nxmNxNshc1) { this.nxmNxNshc1 = nxmNxNshc1; }
    public void setNxmNxNshc2(Long nxmNxNshc2) { this.nxmNxNshc2 = nxmNxNshc2; }
    public void setNxmNxNshc3(Long nxmNxNshc3) { this.nxmNxNshc3 = nxmNxNshc3; }
    public void setNxmNxNshc4(Long nxmNxNshc4) { this.nxmNxNshc4 = nxmNxNshc4; }
    public void setNxmNxNsi(Short nxmNxNsi) { this.nxmNxNsi = nxmNxNsi; }
    public void setNxmNxNsp(Long nxmNxNsp) { this.nxmNxNsp = nxmNxNsp; }
    public void setNxmNxReg0(Long nxmNxReg0) { this.nxmNxReg0 = nxmNxReg0; }
    public void setNxmNxReg1(Long nxmNxReg1) { this.nxmNxReg1 = nxmNxReg1; }
    public void setNxmNxReg2(Long nxmNxReg2) { this.nxmNxReg2 = nxmNxReg2; }
    public void setNxmNxReg3(Long nxmNxReg3) { this.nxmNxReg3 = nxmNxReg3; }
    public void setNxmNxReg4(Long nxmNxReg4) { this.nxmNxReg4 = nxmNxReg4; }
    public void setNxmNxReg5(Long nxmNxReg5) { this.nxmNxReg5 = nxmNxReg5; }
    public void setNxmNxReg6(Long nxmNxReg6) { this.nxmNxReg6 = nxmNxReg6; }
    public void setNxmNxReg7(Long nxmNxReg7) { this.nxmNxReg7 = nxmNxReg7; }
    public void setNxmNxTunId(BigInteger nxmNxTunId) { this.nxmNxTunId = nxmNxTunId; }
    public void setNxmNxTunIpv4Dst(Long nxmNxTunIpv4Dst) { this.nxmNxTunIpv4Dst = nxmNxTunIpv4Dst; }
    public void setNxmNxTunIpv4Src(Long nxmNxTunIpv4Src) { this.nxmNxTunIpv4Src = nxmNxTunIpv4Src; }
    public void setNxmOfArpOp(Integer nxmOfArpOp) { this.nxmOfArpOp = nxmOfArpOp; }
    public void setNxmOfArpSpa(Long nxmOfArpSpa) { this.nxmOfArpSpa = nxmOfArpSpa; }
    public void setNxmOfArpTpa(Long nxmOfArpTpa) { this.nxmOfArpTpa = nxmOfArpTpa; }
    public void setNxmOfEthDst(Long nxmOfEthDst) { this.nxmOfEthDst = nxmOfEthDst; }
    public void setNxmOfEthSrc(Long nxmOfEthSrc) { this.nxmOfEthSrc = nxmOfEthSrc; }
    public void setNxmOfEthType(Integer nxmOfEthType) { this.nxmOfEthType = nxmOfEthType; }

    public static Long macAddressToLong(MacAddress mac) {
        String HEX = "0x";
        String[] bytes = mac.getValue().split(":");
        Long address =
            (Long.decode(HEX + bytes[0]) << 40) |
            (Long.decode(HEX + bytes[1]) << 32) |
            (Long.decode(HEX + bytes[2]) << 24) |
            (Long.decode(HEX + bytes[3]) << 16) |
            (Long.decode(HEX + bytes[4]) << 8 ) |
            (Long.decode(HEX + bytes[5]));
        return address;
    }

    public static Long ipv4AddressToLong(Ipv4Address ip) {
        String[] bytes = ip.getValue().split(".");
        Long address =
            (Long.decode(bytes[0]) << 24) |
            (Long.decode(bytes[1]) << 16) |
            (Long.decode(bytes[2]) << 8) |
            (Long.decode(bytes[3]));
        return address;
    }

    public static NxmData toNxmData(GeneralAugMatchNodesNodeTableFlow m) {
        Long longNxmNxArpSha = Long.valueOf(-1);
        Long longNxmNxArpTha = Long.valueOf(-1);
        Long longNxmNxNshc1 = Long.valueOf(-1);
        Long longNxmNxNshc2 = Long.valueOf(-1);
        Long longNxmNxNshc3 = Long.valueOf(-1);
        Long longNxmNxNshc4 = Long.valueOf(-1);
        Short shortNxmNxNsi = Short.valueOf((short)-1);
        Long longNxmNxNsp = Long.valueOf(-1);
        Long longNxmNxReg0 = Long.valueOf(-1);
        Long longNxmNxReg1 = Long.valueOf(-1);
        Long longNxmNxReg2 = Long.valueOf(-1);
        Long longNxmNxReg3 = Long.valueOf(-1);
        Long longNxmNxReg4 = Long.valueOf(-1);
        Long longNxmNxReg5 = Long.valueOf(-1);
        Long longNxmNxReg6 = Long.valueOf(-1);
        Long longNxmNxReg7 = Long.valueOf(-1);
        BigInteger bigIntegerNxmNxTunId = BigInteger.valueOf(-1);
        Long longNxmNxTunIpv4Dst = Long.valueOf(-1);
        Long longNxmNxTunIpv4Src = Long.valueOf(-1);
        Integer integerNxmOfArpOp = Integer.valueOf(-1);
        Long longNxmOfArpSpa = Long.valueOf(-1);
        Long longNxmOfArpTpa = Long.valueOf(-1);
        Long longNxmOfEthDst = Long.valueOf(-1);
        Long longNxmOfEthSrc = Long.valueOf(-1);
        Integer integerNxmOfEthType = Integer.valueOf(-1);

        if (m != null) {
            List<ExtensionList> extensionsList = m.getExtensionList();
            if (extensionsList != null && !extensionsList.isEmpty()) {
                for (ExtensionList extensions : extensionsList) {
                    Extension extension = extensions.getExtension();
                    Class<? extends ExtensionKey> extensionKey = extensions.getExtensionKey();
                    NxAugMatchNodesNodeTableFlow am = extension.getAugmentation(NxAugMatchNodesNodeTableFlow.class);
                    if (extensionKey.equals(NxmNxArpShaKey.class)) {
                        longNxmNxArpSha = macAddressToLong(am.getNxmNxArpSha().getMacAddress());
                    }
                    else if (extensionKey.equals(NxmNxArpThaKey.class)) {
                        longNxmNxArpTha = macAddressToLong(am.getNxmNxArpTha().getMacAddress());
                    }
                    else if (extensionKey.equals(NxmNxNshc1Key.class)) {
                        longNxmNxNshc1 = am.getNxmNxNshc1().getValue();
                    }
                    else if (extensionKey.equals(NxmNxNshc2Key.class)) {
                        longNxmNxNshc2 = am.getNxmNxNshc2().getValue();
                    }
                    else if (extensionKey.equals(NxmNxNshc3Key.class)) {
                        longNxmNxNshc3 = am.getNxmNxNshc3().getValue();
                    }
                    else if (extensionKey.equals(NxmNxNshc4Key.class)) {
                        longNxmNxNshc4 = am.getNxmNxNshc4().getValue();
                    }
                    else if (extensionKey.equals(NxmNxNsiKey.class)) {
                        shortNxmNxNsi = am.getNxmNxNsi().getNsi();
                    }
                    else if (extensionKey.equals(NxmNxNspKey.class)) {
                        longNxmNxNsp = am.getNxmNxNsp().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg0Key.class)) {
                        longNxmNxReg0 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg1Key.class)) {
                        longNxmNxReg1 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg2Key.class)) {
                        longNxmNxReg2 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg3Key.class)) {
                        longNxmNxReg3 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg4Key.class)) {
                        longNxmNxReg4 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg5Key.class)) {
                        longNxmNxReg5 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg6Key.class)) {
                        longNxmNxReg5 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxReg7Key.class)) {
                        longNxmNxReg7 = am.getNxmNxReg().getValue();
                    }
                    else if (extensionKey.equals(NxmNxTunIdKey.class)) {
                        bigIntegerNxmNxTunId = am.getNxmNxTunId().getValue();
                    }
                    else if (extensionKey.equals(NxmNxTunIpv4DstKey.class)) {
                        longNxmNxTunIpv4Dst = ipv4AddressToLong(am.getNxmNxTunIpv4Dst().getIpv4Address());
                    }
                    else if (extensionKey.equals(NxmNxTunIpv4SrcKey.class)) {
                        longNxmNxTunIpv4Src = ipv4AddressToLong(am.getNxmNxTunIpv4Src().getIpv4Address());
                    }
                    else if (extensionKey.equals(NxmOfArpOpKey.class)) {
                        integerNxmOfArpOp = am.getNxmOfArpOp().getValue();
                    }
                    else if (extensionKey.equals(NxmOfArpSpaKey.class)) {
                        longNxmOfArpSpa = ipv4AddressToLong(am.getNxmOfArpSpa().getIpv4Address());
                    }
                    else if (extensionKey.equals(NxmOfArpTpaKey.class)) {
                        longNxmOfArpSpa = ipv4AddressToLong(am.getNxmOfArpTpa().getIpv4Address());
                    }
                    else if (extensionKey.equals(NxmOfEthDstKey.class)) {
                        longNxmOfEthDst = macAddressToLong(am.getNxmOfEthDst().getMacAddress());
                    }
                    else if (extensionKey.equals(NxmOfEthSrcKey.class)) {
                        longNxmOfEthSrc = macAddressToLong(am.getNxmOfEthSrc().getMacAddress());
                    }
                    else if (extensionKey.equals(NxmOfEthTypeKey.class)) {
                        integerNxmOfEthType = am.getNxmOfEthType().getValue();
                    }
                }
            }
        }
        NxmData nxmData = new NxmData();
        nxmData.setNxmNxArpSha(longNxmNxArpSha);
        nxmData.setNxmNxArpTha(longNxmNxArpTha);
        nxmData.setNxmNxNshc1(longNxmNxNshc1);
        nxmData.setNxmNxNshc2(longNxmNxNshc2);
        nxmData.setNxmNxNshc3(longNxmNxNshc3);
        nxmData.setNxmNxNshc4(longNxmNxNshc4);
        nxmData.setNxmNxNsi(shortNxmNxNsi);
        nxmData.setNxmNxNsp(longNxmNxNsp);
        nxmData.setNxmNxReg0(longNxmNxReg0);
        nxmData.setNxmNxReg1(longNxmNxReg1);
        nxmData.setNxmNxReg2(longNxmNxReg2);
        nxmData.setNxmNxReg3(longNxmNxReg3);
        nxmData.setNxmNxReg4(longNxmNxReg4);
        nxmData.setNxmNxReg5(longNxmNxReg5);
        nxmData.setNxmNxReg6(longNxmNxReg6);
        nxmData.setNxmNxReg7(longNxmNxReg7);
        nxmData.setNxmNxTunId(bigIntegerNxmNxTunId);
        nxmData.setNxmNxTunIpv4Dst(longNxmNxTunIpv4Dst);
        nxmData.setNxmNxTunIpv4Src(longNxmNxTunIpv4Src);
        nxmData.setNxmOfArpOp(integerNxmOfArpOp);
        nxmData.setNxmOfArpSpa(longNxmOfArpSpa);
        nxmData.setNxmOfArpTpa(longNxmOfArpTpa);
        nxmData.setNxmOfEthDst(longNxmOfEthDst);
        nxmData.setNxmOfEthSrc(longNxmOfEthSrc);
        nxmData.setNxmOfEthType(integerNxmOfEthType);
        return nxmData;
    }

    public boolean isSame(GeneralAugMatchNodesNodeTableFlow m) {
        NxmData nxmData = toNxmData(m);
        if ((nxmData.nxmNxArpSha != this.nxmNxArpSha)
           && (nxmData.nxmNxArpSha != -1) && (this.nxmNxArpSha != -1)) {
            return false;
        }
        if ((nxmData.nxmNxArpTha != this.nxmNxArpTha)
           && (nxmData.nxmNxArpTha != -1) && (this.nxmNxArpTha != -1)) {
            return false;
        }
        if ((nxmData.nxmNxNshc1 != this.nxmNxNshc1)
           && (nxmData.nxmNxNshc1 != -1) && (this.nxmNxNshc1 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxNshc2 != this.nxmNxNshc2)
           && (nxmData.nxmNxNshc2 != -1) && (this.nxmNxNshc2 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxNshc3 != this.nxmNxNshc3)
           && (nxmData.nxmNxNshc3 != -1) && (this.nxmNxNshc3 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxNshc4 != this.nxmNxNshc4)
           && (nxmData.nxmNxNshc4 != -1) && (this.nxmNxNshc4 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxNsi != this.nxmNxNsi)
           && (nxmData.nxmNxNsi != -1) && (this.nxmNxNsi != -1)) {
            return false;
        }
        if ((nxmData.nxmNxNsp != this.nxmNxNsp)
           && (nxmData.nxmNxNsp != -1) && (this.nxmNxNsp != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg0 != this.nxmNxReg0)
           && (nxmData.nxmNxReg0 != -1) && (this.nxmNxReg0 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg1 != this.nxmNxReg1)
           && (nxmData.nxmNxReg1 != -1) && (this.nxmNxReg1 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg2 != this.nxmNxReg2)
           && (nxmData.nxmNxReg2 != -1) && (this.nxmNxReg2 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg3 != this.nxmNxReg3)
           && (nxmData.nxmNxReg3 != -1) && (this.nxmNxReg3 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg4 != this.nxmNxReg4)
           && (nxmData.nxmNxReg4 != -1) && (this.nxmNxReg4 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg5 != this.nxmNxReg5)
           && (nxmData.nxmNxReg5 != -1) && (this.nxmNxReg5 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg6 != this.nxmNxReg6)
           && (nxmData.nxmNxReg6 != -1) && (this.nxmNxReg6 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxReg7 != this.nxmNxReg7)
           && (nxmData.nxmNxReg7 != -1) && (this.nxmNxReg7 != -1)) {
            return false;
        }
        if ((nxmData.nxmNxTunId != this.nxmNxTunId)
           && (nxmData.nxmNxTunId != BigInteger.valueOf(-1)) && (this.nxmNxTunId != BigInteger.valueOf(-1))) {
            return false;
        }
        if ((nxmData.nxmNxTunIpv4Dst != this.nxmNxTunIpv4Dst)
           && (nxmData.nxmNxTunIpv4Dst != -1) && (this.nxmNxTunIpv4Dst != -1)) {
            return false;
        }
        if ((nxmData.nxmNxTunIpv4Src != this.nxmNxTunIpv4Src)
           && (nxmData.nxmNxTunIpv4Src != -1) && (this.nxmNxTunIpv4Src != -1)) {
            return false;
        }
        if ((nxmData.nxmOfArpOp != this.nxmOfArpOp)
           && (nxmData.nxmOfArpOp != -1) && (this.nxmOfArpOp != -1)) {
            return false;
        }
        if ((nxmData.nxmOfArpSpa != this.nxmOfArpSpa)
           && (nxmData.nxmOfArpSpa != -1) && (this.nxmOfArpSpa != -1)) {
            return false;
        }
        if ((nxmData.nxmOfArpTpa != this.nxmOfArpTpa)
           && (nxmData.nxmOfArpTpa != -1) && (this.nxmOfArpTpa != -1)) {
            return false;
        }
        if ((nxmData.nxmOfEthDst != this.nxmOfEthDst)
           && (nxmData.nxmOfEthDst != -1) && (this.nxmOfEthDst != -1)) {
            return false;
        }
        if ((nxmData.nxmOfEthSrc != this.nxmOfEthSrc)
           && (nxmData.nxmOfEthSrc != -1) && (this.nxmOfEthSrc != -1)) {
            return false;
        }
        if ((nxmData.nxmOfEthType != this.nxmOfEthType)
           && (nxmData.nxmOfEthType != -1) && (this.nxmOfEthType != -1)) {
            return false;
        }
        return true;
    }
}
