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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;

@RunWith(MockitoJUnitRunner.class)
public class ActionSerializerInjectorTest {

    @Mock
    private SwitchConnectionProvider switchConnectionProvider;

    @Mock
    private OFSerializer<Action> actionSerializer;

    private Function<Class<? extends Action>, Consumer<OFSerializer<? extends Action>>> injector;

    @Before
    public void setUp() throws Exception {
        injector = ActionSerializerInjector.createInjector(switchConnectionProvider, EncodeConstants.OF13_VERSION_ID);
    }

    @Test
    public void injectSerializers() throws Exception {
        injector.apply(CopyTtlInCase.class).accept(actionSerializer);
        verify(switchConnectionProvider).registerSerializer(
                new MessageTypeKey<Object>(EncodeConstants.OF13_VERSION_ID, CopyTtlInCase.class),
                actionSerializer);
    }

}