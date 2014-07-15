/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import java.util.Map;

import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * convert message from OFJava-API model into MD-SAL model
 * 
 * @param <FROM> input message model - OFJava-API
 * @param <PATH> represents possible paths in yang schema for augmentations
 */
public interface ConvertorFromOFJava<FROM extends DataContainer, PATH extends AugmentationPath> {

    /**
     * @param input
     * @param path in yang schema where a converted value has to be augmented
     * @return message converted to MD-SAL and its type
     */
    <T extends Augmentation<Extension>> Map.Entry<Class<T>, T> convert(FROM input, PATH path);
}
