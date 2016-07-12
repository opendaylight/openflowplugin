/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.openflowplugin.api.OFConstants.OFP_VERSION_1_0;
import static org.opendaylight.openflowplugin.api.OFConstants.OFP_VERSION_1_3;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.openflowplugin.impl.translator.TranslatorKeyFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;

public class TranslatorLibratyUtilTest {

    private static class TranslatorLibrarianTestImpl implements TranslatorLibrarian {

        private TranslatorLibrary translatorLibrary;

        @Override
        public TranslatorLibrary oook() {
            return translatorLibrary;
        }

        @Override
        public void setTranslatorLibrary(TranslatorLibrary translatorLibrary) {
            this.translatorLibrary = translatorLibrary;
        }
    }

    private TranslatorLibrarianTestImpl translatorLibrarian;

    @Before
    public void setUp() {
        translatorLibrarian = new TranslatorLibrarianTestImpl();
    }

    @Test
    public void setBasicTranslatorLibraryTest() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        TranslatorLibraryUtil.injectBasicTranslatorLibrary(translatorLibrarian, convertorManager);
        TranslatorLibrary translatorLibrary = translatorLibrarian.oook();

        TranslatorKeyFactory of13TranslatorKeyFactory = new TranslatorKeyFactory(OFP_VERSION_1_3);
        TranslatorKeyFactory of10TranslatorKeyFactory = new TranslatorKeyFactory(OFP_VERSION_1_0);

        MessageTranslator<Object, Object> translator;
        translator = translatorLibrary.lookupTranslator(of13TranslatorKeyFactory
                .createTranslatorKey(PacketIn.class));
        assertNotNull(translator);

        translator = translatorLibrary.lookupTranslator(of13TranslatorKeyFactory.createTranslatorKey(PortGrouping
                .class));
        assertNotNull(translator);

        translator = translatorLibrary.lookupTranslator(of13TranslatorKeyFactory.createTranslatorKey
                (MultipartReplyAggregateCase
                .class));
        assertNotNull(translator);

        translator = translatorLibrary.lookupTranslator(of10TranslatorKeyFactory.createTranslatorKey(PacketIn.class));
        assertNotNull(translator);

        translator = translatorLibrary.lookupTranslator(of10TranslatorKeyFactory.createTranslatorKey(PortGrouping.class));
        assertNotNull(translator);

        translator = translatorLibrary.lookupTranslator(of10TranslatorKeyFactory.createTranslatorKey(MultipartReplyAggregateCase
                .class));
        assertNotNull(translator);
    }

}
