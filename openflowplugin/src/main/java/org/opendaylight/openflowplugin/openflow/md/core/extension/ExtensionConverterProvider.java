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
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
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
    <FROM extends DataContainer, TO extends DataContainer, PATH extends AugmentationPath> ConvertorFromOFJava<FROM, TO, PATH> getConverter(MessageTypeKey<?> key);
    
    /**
     * lookup converter
     * @param key
     * @return found converter
     */
    <FROM extends DataContainer, TO extends DataContainer, PATH extends AugmentationPath> ConvertorToOFJava<FROM, TO, PATH> getConverter(ConverterExtensionKey<?> key);
}
