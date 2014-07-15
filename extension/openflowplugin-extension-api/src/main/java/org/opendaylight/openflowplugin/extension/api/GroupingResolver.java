/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * @param <GROUPING>
 * @param <T>
 */
public class GroupingResolver<GROUPING, T extends Augmentable<T>> {

    Class<GROUPING> commonInterface;
    Set<Class<? extends Augmentation<T>>> classes;

    /**
     * @param commonInterface
     */
    public GroupingResolver(Class<GROUPING> commonInterface) {
        this.commonInterface = commonInterface;
        classes = new HashSet<>();
    }

    /**
     * @param cls
     */
    public <X extends Augmentation<T>> void add(Class<X> cls) {
        Preconditions.checkArgument(commonInterface.isAssignableFrom(cls));
        classes.add(cls);
    }

    /**
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<GROUPING> getExtension(T data) {
        for (Class<? extends Augmentation<T>> cls : classes) {
            Augmentation<T> potential = data.getAugmentation(cls);
            if (potential != null) {
                return Optional.of((GROUPING) potential);
            }
        }
        return Optional.absent();
    }
}