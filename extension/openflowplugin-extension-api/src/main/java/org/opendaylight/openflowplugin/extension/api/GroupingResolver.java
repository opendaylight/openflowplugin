/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;

/**
 * Provides augmentation resolving upon given {@link Augmentable}.
 * Used {@link #classes} share the same {@link Augmentable}.
 * <br>
 * <b>Usage:</b> in case there is {@link Augmentable} which might contain
 * multiple {@link Augmentation}s depending on origin. And those {@link Augmentation}s
 * are sharing the same grouping so that they could be processed in the same way.
 *
 * @param <G> grouping
 * @param <T> Augmentable
 * @deprecated Use {@link AugmentationGroupingResolver} instead.
 */
@Deprecated
public class GroupingResolver<G, T extends Augmentable<T>> {

    Class<G> commonInterface;
    Set<Class<? extends Augmentation<T>>> classes;

    public GroupingResolver(final Class<G> commonInterface) {
        this.commonInterface = commonInterface;
        classes = new HashSet<>();
    }

    /**
     * Adds an augmentation class.
     *
     * @param cls equivalent augmentation class
     * @param <X> type of parameter
     * @return this for chaining
     */
    public <X extends Augmentation<T>> GroupingResolver<G, T> add(final Class<X> cls) {
        Preconditions.checkArgument(commonInterface.isAssignableFrom(cls));
        classes.add(cls);
        return this;
    }

    /**
     * Sets the augmentation classes.
     *
     * @param clses set of equivalent augmentation classes
     */
    public void setAugmentations(final Set<Class<? extends Augmentation<T>>> clses) {
        for (Class<? extends Augmentation<T>> cls : clses) {
            Preconditions.checkArgument(commonInterface.isAssignableFrom(cls));
        }
        classes = clses;
    }

    /**
     * Gets the extension for the given data.
     *
     * @param data data
     * @return shared grouping
     */
    @SuppressWarnings("unchecked")
    public Optional<G> getExtension(final T data) {
        for (Class<? extends Augmentation<T>> cls : classes) {
            Augmentation<T> potential = data.augmentation(cls);
            if (potential != null) {
                return Optional.of((G) potential);
            }
        }
        return Optional.empty();
    }
}
