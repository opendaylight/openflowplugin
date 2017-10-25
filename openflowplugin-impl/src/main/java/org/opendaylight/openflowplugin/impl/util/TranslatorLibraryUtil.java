/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.openflowplugin.impl.translator.AggregatedFlowStatisticsTranslator;
import org.opendaylight.openflowplugin.impl.translator.FlowRemovedTranslator;
import org.opendaylight.openflowplugin.impl.translator.FlowRemovedV10Translator;
import org.opendaylight.openflowplugin.impl.translator.PacketReceivedTranslator;
import org.opendaylight.openflowplugin.impl.translator.PortUpdateTranslator;
import org.opendaylight.openflowplugin.impl.translator.TranslatorKeyFactory;
import org.opendaylight.openflowplugin.impl.translator.TranslatorLibraryBuilder;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;

public final class TranslatorLibraryUtil {


    private TranslatorLibraryUtil() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    private static final TranslatorKeyFactory OF_13_TRANSLATOR_KEY_FACTORY =
            new TranslatorKeyFactory(OFConstants.OFP_VERSION_1_3);
    private static final TranslatorKeyFactory OF_10_TRANSLATOR_KEY_FACTORY =
            new TranslatorKeyFactory(OFConstants.OFP_VERSION_1_0);

    public static void injectBasicTranslatorLibrary(final TranslatorLibrarian librarian,
                                                    final ConvertorExecutor convertorExecutor) {
        final TranslatorLibrary basicTranslatorLibrary = new TranslatorLibraryBuilder()
                .addTranslator(OF_13_TRANSLATOR_KEY_FACTORY.createTranslatorKey(PacketIn.class),
                        new PacketReceivedTranslator(convertorExecutor))
                .addTranslator(OF_13_TRANSLATOR_KEY_FACTORY.createTranslatorKey(PortGrouping.class),
                        new PortUpdateTranslator())
                .addTranslator(OF_13_TRANSLATOR_KEY_FACTORY.createTranslatorKey(MultipartReplyAggregateCase.class),
                        new AggregatedFlowStatisticsTranslator())
                .addTranslator(OF_13_TRANSLATOR_KEY_FACTORY.createTranslatorKey(FlowRemoved.class),
                        new FlowRemovedTranslator(convertorExecutor))
                .addTranslator(OF_10_TRANSLATOR_KEY_FACTORY.createTranslatorKey(PacketIn.class),
                        new PacketReceivedTranslator(convertorExecutor))
                .addTranslator(OF_10_TRANSLATOR_KEY_FACTORY.createTranslatorKey(PortGrouping.class),
                        new PortUpdateTranslator())
                .addTranslator(OF_10_TRANSLATOR_KEY_FACTORY.createTranslatorKey(MultipartReplyAggregateCase.class),
                        new AggregatedFlowStatisticsTranslator())
                .addTranslator(OF_10_TRANSLATOR_KEY_FACTORY.createTranslatorKey(FlowRemoved.class),
                        new FlowRemovedV10Translator(convertorExecutor))
                .build();

        librarian.setTranslatorLibrary(basicTranslatorLibrary);
    }
}
