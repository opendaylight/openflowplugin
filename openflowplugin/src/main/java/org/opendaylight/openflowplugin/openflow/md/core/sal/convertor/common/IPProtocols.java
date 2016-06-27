/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;

/**
 * @author tkubas
 *
 */
//TODO make a model in YANG for protocols 
public enum IPProtocols {
    ICMP((short) 1), 
    TCP((short) 6), 
    UDP((short) 17), 
    ICMPV6((short) 58);

    private short protocol;
    
    private static final Map<Short, IPProtocols> VALUE_MAP;
    static {
        Builder<Short, IPProtocols> builder = ImmutableMap.builder();
        for(IPProtocols protocols : IPProtocols.values()) {
            builder.put(protocols.protocol, protocols);
        }
        VALUE_MAP = builder.build();
    }
    
    private IPProtocols(short value) {
        this.protocol = value;
    }

    public static IPProtocols fromProtocolNum(Short protocolNum) {
        return VALUE_MAP.get(protocolNum);
    }
}
