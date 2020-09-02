/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;

/**
 * Unit tests for MessageListenerWrapper.
 *
 * @author michal.polkorab
 */
public class MessageListenerWrapperTest {

    /**
     * Test MessageListenerWrapper creation.
     */
    @Test
    public void test() {
        HelloInputBuilder builder = new HelloInputBuilder();
        HelloInput hello = builder.build();
        SimpleRpcListener<?> listener = new SimpleRpcListener<>(hello, "Error");
        MessageListenerWrapper wrapper = new MessageListenerWrapper(hello, listener);
        Assert.assertEquals("Wrong message", hello, wrapper.getMsg());
        Assert.assertEquals("Wrong listener", listener, wrapper.getListener());
    }
}
