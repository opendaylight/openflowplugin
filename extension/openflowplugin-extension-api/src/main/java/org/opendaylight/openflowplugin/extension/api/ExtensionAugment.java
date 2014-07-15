/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * @author msunal
 *
 */
public class ExtensionAugment<T extends Augmentation<Extension>> {

    private final T augmentationObject;
    private final Class<T> augmentationClass;

    public ExtensionAugment(Class<T> augmentationClass, T augmentationObject) {
        this.augmentationClass = augmentationClass;
        this.augmentationObject = augmentationObject;
    }

    public T getAugmentationObject() {
        return augmentationObject;
    }

    public Class<T> getAugmentationClass() {
        return augmentationClass;
    }
}
