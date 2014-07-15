/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Wrapper over augmentation and it's type in order to ease handing over and hooking of an augmentation 
 * @param <EXT_POINT> augmentable type where wrapped augmentation belongs
 */
public class AugmentTuple<EXT_POINT extends Augmentable<EXT_POINT>> {

    private final Augmentation<EXT_POINT> augmentationObject;
    private final Class<? extends Augmentation<EXT_POINT>> augmentationClass;

    /**
     * @param augmentationClass
     * @param augmentationObject
     */
    public AugmentTuple(Class<? extends Augmentation<EXT_POINT>> augmentationClass, Augmentation<EXT_POINT> augmentationObject) {
        this.augmentationClass = augmentationClass;
        this.augmentationObject = augmentationObject;
    }

    /**
     * @return instance of wrapped augmentation
     */
    public Augmentation<EXT_POINT> getAugmentationObject() {
        return augmentationObject;
    }

    /**
     * @return type of wrapped augmentation
     */
    public Class<? extends Augmentation<EXT_POINT>> getAugmentationClass() {
        return augmentationClass;
    }
}
