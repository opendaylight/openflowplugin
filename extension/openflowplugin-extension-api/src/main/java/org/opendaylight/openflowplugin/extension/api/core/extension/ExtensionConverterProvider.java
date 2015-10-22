/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api.core.extension;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 *
 */
public interface ExtensionConverterProvider {

    /**
     * lookup converter
     * @param key
     * @return found converter
     */
    <F extends DataContainer, P extends AugmentationPath> ConvertorFromOFJava<F, P> getConverter(MessageTypeKey<?> key);

    /**
     * lookup converter
     * @param key
     * @return found converter
     */
    <T extends DataContainer> ConvertorToOFJava<T> getConverter(ConverterExtensionKey<?> key);

    /**
     * @param key
     * @return found converter
     */
    <F extends Action, T extends DataContainer> ConvertorActionToOFJava<F, T> getConverter(TypeVersionKey<F> key);

    /**
     * lookup converter<br>
     * TODO: this method should be compatible with {@link #getConverter(MessageTypeKey)} after matches are migrated to similar structure
     * @param key
     * @return found converter
     */
    <F extends DataContainer, P extends AugmentationPath> ConvertorActionFromOFJava<F, P> getActionConverter(MessageTypeKey<?> key);

    /**
     * lookup converter for experimenter message
     *
     * @param key
     * @return found converter
     */
    <F extends ExperimenterMessageOfChoice, T extends DataContainer> ConvertorMessageToOFJava<F, T> getMessageConverter(TypeVersionKey<F> key);

    /**
     * lookup converter for experimenter message
     *
     * @param key
     * @return found converter
     */
    <F extends DataContainer, P extends AugmentationPath> ConvertorMessageFromOFJava<F, P> getMessageConverter(MessageTypeKey<?> key);
}
