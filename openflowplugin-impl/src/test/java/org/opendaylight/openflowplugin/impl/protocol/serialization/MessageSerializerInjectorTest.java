/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

@RunWith(MockitoJUnitRunner.class)
public class MessageSerializerInjectorTest {
    @Mock
    private SwitchConnectionProvider switchConnectionProvider;

    @Mock
    private OFSerializer<OfHeader> ofSerializer;

    private Function<Class<?>, Consumer<OFSerializer<? extends OfHeader>>> injector;

    @Before
    public void setUp() {
        injector = MessageSerializerInjector.createInjector(switchConnectionProvider, EncodeConstants.OF13_VERSION_ID);
    }

    @Test
    public void injectSerializers() throws Exception {
        injector.apply(OfHeader.class).accept(ofSerializer);
        verify(switchConnectionProvider).registerSerializer(new MessageTypeKey<>(OFConstants.OFP_VERSION_1_3, OfHeader.class), ofSerializer);
    }
}