/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 3.4.2015.
 */
public class TranslatorLibraryBuilder {

    private final Map<TranslatorKey, MessageTranslator<?, ?>> translators = new HashMap<>();

    public TranslatorLibraryBuilder addTranslator(TranslatorKey key, MessageTranslator<?, ?> translator) {
        translators.put(key, translator);
        return this;
    }

    public TranslatorLibrary build() {
        return new TranslatorLibraryImpl(translators);
    }

    private final class TranslatorLibraryImpl implements TranslatorLibrary {
        private Map<TranslatorKey, MessageTranslator<?, ?>> translators = new ConcurrentHashMap<>();

        TranslatorLibraryImpl(Map<TranslatorKey, MessageTranslator<?, ?>> translators) {
            this.translators.putAll(translators);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I, O> MessageTranslator<I, O> lookupTranslator(TranslatorKey key) {
            return (MessageTranslator<I, O>) translators.get(key);
        }

    }
}
