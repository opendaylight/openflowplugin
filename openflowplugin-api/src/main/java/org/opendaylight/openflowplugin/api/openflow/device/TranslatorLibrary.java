package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;

public interface TranslatorLibrary {

    /**
     * @param key
     * @param translator
     * @return
     */
    MessageTranslator<?, ?> addTranslator(TranslatorKey key, MessageTranslator<?, ?> translator);

    /**
     * @param key
     * @return
     */
    <I, O> MessageTranslator<I, O> lookupTranslator(TranslatorKey key);
}
