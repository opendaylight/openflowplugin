/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tkubas
 *
 */

//TODO make a model in YANG for protocols 
public enum IPProtocols {
    ICMP(1), 
    TCP(6), 
    UDP(17), 
    ICMPV6(58);

    private int protocol;
    
    private static Map<Integer, IPProtocols> valueMap;
    static {
        valueMap = new HashMap<>();
        for(IPProtocols protocols : IPProtocols.values()) {
            valueMap.put(protocols.protocol, protocols);
        }
    }
    
    private IPProtocols(int value) {
        this.protocol = value;
    }

    private byte getValue() {
        return (byte) this.protocol;
    }
    
    private Short getShortValue() {
        return new Short((short) protocol);
    }
    
    public static IPProtocols fromProtocolNum(Short protocolNum) {
        return valueMap.get(protocolNum);
    }
}
