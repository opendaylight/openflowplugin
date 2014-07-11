/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
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
     * @param path
     * @param sessionContext TODO: fix type when moved to API
     * @return message converted to MD-SAL
     */
    TO convert(FROM input, InstanceIdentifier<DataObject> path, Object sessionContext);

}
