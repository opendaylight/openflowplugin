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
 * convert message from MD-SAL model into OFJava-API model
 * @param <FROM> input message model - MD-SAL
 * @param <TO> output message model - OFJava-API
 * 
 * TODO: consider creating of specialized subinterfaces
 */
public interface ConvertorToOFJava<FROM extends DataContainer, TO extends DataContainer> {
    
    /**
     * @param input
     * @param path in yang schema where a converted value has to be augmented
     * @return message converted to OFJava-API
     */
    TO convert(FROM input, AugmentationPath path);

}
