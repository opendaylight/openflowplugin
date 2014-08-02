/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
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
    <FROM extends DataContainer, PATH extends AugmentationPath> ConvertorFromOFJava<FROM, PATH> getConverter(MessageTypeKey<?> key);
    
    /**
     * lookup converter
     * @param key
     * @return found converter
     */
    <TO extends DataContainer> ConvertorToOFJava<TO> getConverter(ConverterExtensionKey<?> key);

    /**
     * @param key
     * @return found converter
     */
    <FROM extends Action, TO extends DataContainer> ConvertorActionToOFJava<FROM, TO> getConverter(TypeVersionKey<FROM> key);
    
    /**
     * lookup converter<br/>
     * TODO: this method should be compatible with {@link #getConverter(MessageTypeKey)} after matches are migrated to similar structure
     * @param key
     * @return found converter
     */
    <FROM extends DataContainer, PATH extends AugmentationPath> ConvertorActionFromOFJava<FROM, PATH> getActionConverter(MessageTypeKey<?> key);
}
