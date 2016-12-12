/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

@RunWith(MockitoJUnitRunner.class)
public class MessageDeserializerInjectorTest {
    @Mock
    private SwitchConnectionProvider switchConnectionProvider;

    @Mock
    private OFDeserializer<OfHeader> ofDeserializer;

    private Function<Integer, Function<Class<? extends OfHeader>, Consumer<OFDeserializer<? extends OfHeader>>>> injector;

    @Before
    public void setUp() throws Exception {
        injector = MessageDeserializerInjector.createInjector(switchConnectionProvider, EncodeConstants.OF13_VERSION_ID);
    }

    @Test
    public void injectDeserializers() throws Exception {
        injector.apply(10).apply(OfHeader.class).accept(ofDeserializer);
        verify(switchConnectionProvider).unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 10));
        verify(switchConnectionProvider).registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 10), OfHeader.class);
        verify(switchConnectionProvider).registerDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 10, OfHeader.class), ofDeserializer);
    }

}