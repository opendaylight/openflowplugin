/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * lookup and register key for extension converters, basic case expects this to correlate with input model type
 * 
 * @param <TYPE> type of key
 */
public interface ConverterExtensionKey<TYPE extends DataContainer> {

    /**
     * @return key type
     */
    Class<TYPE> getType();
}
