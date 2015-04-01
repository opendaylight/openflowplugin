/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;

/**
 * @author tkubas
 *
 */
public class TranslatorLibraryImpl implements TranslatorLibrary {
    private Map<TranslatorKey, MessageTranslator<?, ?>> translators = new HashMap<>();

    @Override
    public MessageTranslator<?, ?> addTranslator(TranslatorKey key, MessageTranslator<?, ?> translator) {
        return translators.put(key, translator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I, O> MessageTranslator<I, O> lookupTranslator(TranslatorKey key) {
        return (MessageTranslator<I, O>) translators.get(key);
    }

}
