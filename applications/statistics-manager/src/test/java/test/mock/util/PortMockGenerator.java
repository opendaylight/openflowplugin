/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;


import java.util.Random;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortKey;

public class PortMockGenerator {
    private static final Random rnd = new Random();
    private static final PortBuilder portBuilder = new PortBuilder();

    public static Port getRandomPort() {
        portBuilder.setKey(new PortKey(TestUtils.nextLong(0, 4294967295L)));
        portBuilder.setBarrier(rnd.nextBoolean());
        portBuilder.setPortNumber(new PortNumberUni(TestUtils.nextLong(0, 4294967295L)));
        portBuilder.setConfiguration(new PortConfig(rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextBoolean()));
        return portBuilder.build();
    }
}
