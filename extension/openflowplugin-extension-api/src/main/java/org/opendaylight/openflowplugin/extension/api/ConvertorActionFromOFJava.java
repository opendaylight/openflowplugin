/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * convert message from OFJava-API model into MD-SAL model
 * 
 * @param <F> input message model - OFJava-API
 * @param <P> represents possible paths in yang schema for augmentations
 */
public interface ConvertorActionFromOFJava<F extends DataContainer, P extends AugmentationPath> {

    /**
     * @param input
     * @param path in yang schema where a converted value has to be augmented
     * @return message converted to MD-SAL and its type
     */
    Action convert(F input, P path);
}
