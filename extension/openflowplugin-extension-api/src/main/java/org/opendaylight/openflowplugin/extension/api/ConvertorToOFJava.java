/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * convert message from MD-SAL model into OFJava-API model
 * 
 * @param <T> output message model - OFJava-API
 */
public interface ConvertorToOFJava<T extends DataContainer> {

    /**
     * @param extension where is vendor's augmentation
     * @return message converted to OFJava-API
     */
    T convert(Extension extension);
}
