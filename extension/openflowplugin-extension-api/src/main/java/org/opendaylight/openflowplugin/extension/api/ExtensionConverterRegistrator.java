/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * registration place for message converters provided by vendor extensions
 */
public interface ExtensionConverterRegistrator {

    /**
     * register converter for direction from MD-SAL to OFJava
     * @param key
     * @param extConvertor
     * @return closable registration
     */
    public AutoCloseable registerConvertor(
            ConverterExtensionKey<? extends DataContainer> key,
            ConvertorToOFJava<? extends DataContainer, ? extends DataContainer> extConvertor);

    /**
     * register converter for direction from OFJava to MD-SAL
     * @param key
     * @param extConvertor
     * @return closable registration
     */
    public AutoCloseable registerConvertor(
            MessageTypeKey<? extends DataContainer> key,
            ConvertorFromOFJava<? extends DataContainer, ? extends DataContainer> extConvertor);

}
