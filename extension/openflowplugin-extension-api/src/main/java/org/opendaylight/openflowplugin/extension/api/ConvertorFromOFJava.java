/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * convert message from OFJava-API model into MD-SAL model 
 * @param <FROM> input message model - OFJava-API
 * @param <TO> output message model - MD-SAL
 * 
 * TODO: consider creating of specialized subinterfaces
 */
public interface ConvertorFromOFJava<FROM extends DataContainer, TO extends DataContainer> {
    
    /**
     * @param input
     * @param path in yang schema where a converted value has to be augmented
     * @return message converted to MD-SAL
     */
    TO convert(FROM input, InstanceIdentifier<Extension> path);

}
