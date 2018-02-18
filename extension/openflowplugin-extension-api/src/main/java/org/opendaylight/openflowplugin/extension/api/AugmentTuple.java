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
 * @param <E> augmentable type where wrapped augmentation belongs
 */
public class AugmentTuple<E extends Augmentable<E>> {

    private final Augmentation<E> augmentationObject;
    private final Class<? extends Augmentation<E>> augmentationClass;

    /**
     * @param augmentationClass
     * @param augmentationObject
     */
    public AugmentTuple(Class<? extends Augmentation<E>> augmentationClass, Augmentation<E> augmentationObject) {
        this.augmentationClass = augmentationClass;
        this.augmentationObject = augmentationObject;
    }

    /**
     * @return instance of wrapped augmentation
     */
    public Augmentation<E> getAugmentationObject() {
        return augmentationObject;
    }

    /**
     * @return type of wrapped augmentation
     */
    public Class<? extends Augmentation<E>> getAugmentationClass() {
        return augmentationClass;
    }
}
