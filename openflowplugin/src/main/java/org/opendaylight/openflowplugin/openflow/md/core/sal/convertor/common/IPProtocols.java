/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Enumerates IP protocols.
 * @author tkubas
 */
// TODO make a model in YANG for protocols
public enum IPProtocols {
    ICMP(1),
    TCP(6),
    UDP(17),
    ICMPV6(58);

    private Uint8 protocol;

    private static final ImmutableMap<Uint8, IPProtocols> VALUE_MAP = Maps.uniqueIndex(Arrays.asList(values()),
        proto -> proto.protocol);

    IPProtocols(final int value) {
        this.protocol = Uint8.valueOf(value);
    }

    public static IPProtocols fromProtocolNum(final Uint8 protocolNum) {
        return VALUE_MAP.get(protocolNum);
    }
}
