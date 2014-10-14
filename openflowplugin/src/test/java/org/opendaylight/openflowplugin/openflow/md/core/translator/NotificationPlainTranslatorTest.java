/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
public class NotificationPlainTranslatorTest {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock GetFeaturesOutput features;

    NotificationPlainTranslator translator = new NotificationPlainTranslator();

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests {@link NotificationPlainTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     */
    @Test
    public void testIncorrectInput() {
        HelloMessageBuilder helloBuilder = new HelloMessageBuilder();
        HelloMessage message = helloBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 0, list.size());
    }

    /**
     * Tests {@link NotificationPlainTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     */
    @Test
    public void test() {
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("64"));
        HelloMessageBuilder helloBuilder = new HelloMessageBuilder();
        helloBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        helloBuilder.setXid(42L);
        HelloMessage message = helloBuilder.build();
        NotificationQueueWrapper wrapper = new NotificationQueueWrapper(message, message.getVersion());

        List<DataObject> list = translator.translate(cookie, sc, wrapper);

        Assert.assertEquals("Wrong list size", 1, list.size());
        HelloMessage hello = (HelloMessage) list.get(0);
        Assert.assertEquals("Wrong output", 4, hello.getVersion().intValue());
        Assert.assertEquals("Wrong output", 42, hello.getXid().intValue());
    }
}
