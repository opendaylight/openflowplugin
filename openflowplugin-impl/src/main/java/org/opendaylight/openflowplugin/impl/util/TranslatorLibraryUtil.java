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
import org.opendaylight.openflowplugin.impl.translator.PacketReceivedTranslator;
import org.opendaylight.openflowplugin.impl.translator.PortUpdateTranslator;
import org.opendaylight.openflowplugin.impl.translator.TranslatorKeyFactory;
import org.opendaylight.openflowplugin.impl.translator.TranslatorLibraryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.PortUpdate;

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
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(PacketReceived.class), new PacketReceivedTranslator()).
                addTranslator(of13TranslatorKeyFactory.createTranslatorKey(PortUpdate.class), new PortUpdateTranslator()).
                addTranslator(of10TranslatorKeyFactory.createTranslatorKey(PacketReceived.class), new PacketReceivedTranslator()).
                addTranslator(of10TranslatorKeyFactory.createTranslatorKey(PortUpdate.class), new PortUpdateTranslator()).
                build();

    }

    public static void setBasicTranslatorLibrary(TranslatorLibrarian librarian) {
        librarian.setTranslatorLibrary(basicTranslatorLibrary);
    }
}
