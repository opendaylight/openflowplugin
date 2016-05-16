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
import org.opendaylight.openflowplugin.impl.translator.OfMessageReceivedTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 3.4.2015.
 */
public final class TranslatorLibraryUtil {


    private TranslatorLibraryUtil() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    private static final TranslatorKeyFactory of13TranslatorKeyFactory = new TranslatorKeyFactory(OFConstants.OFP_VERSION_1_3);
    private static final TranslatorKeyFactory of10TranslatorKeyFactory = new TranslatorKeyFactory(OFConstants.OFP_VERSION_1_0);
    private static final TranslatorLibrary basicTranslatorLibrary;

    static {
        basicTranslatorLibrary = new TranslatorLibraryBuilder().
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(PacketIn.class), new PacketReceivedTranslator()).
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(PortGrouping.class), new PortUpdateTranslator()).
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(MultipartReplyAggregateCase.class), new AggregatedFlowStatisticsTranslator()).
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(FlowRemoved.class), new FlowRemovedTranslator()).
                addTranslator(of10TranslatorKeyFactory.createTranslatorKey(PacketIn.class), new PacketReceivedTranslator()).
                addTranslator(of10TranslatorKeyFactory.createTranslatorKey(PortGrouping.class), new PortUpdateTranslator()).
                addTranslator(of10TranslatorKeyFactory.createTranslatorKey(MultipartReplyAggregateCase.class), new AggregatedFlowStatisticsTranslator()).
                addTranslator(of10TranslatorKeyFactory.createTranslatorKey(FlowRemoved.class), new FlowRemovedV10Translator()).

                build();
    }

    public static void setBasicTranslatorLibrary(final TranslatorLibrarian librarian) {
        librarian.setTranslatorLibrary(basicTranslatorLibrary);
    }
}
