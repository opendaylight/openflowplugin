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
import org.opendaylight.yangtools.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides augmentation resolving upon given {@link Augmentable}.
 * Used {@link Augmentation}s do not share {@link Augmentable}.
 * <br>
 * <b>Usage:</b> in case there are multiple {@link Augmentable} classes which might contain
 * corresponding {@link Augmentation}s (1:1..n binding). And those {@link Augmentation}s
 * are sharing the same grouping so that they could be processed in the same way.
 *
 * @param <G> grouping
 */
public class GroupingLooseResolver<G> {
    private static final Logger LOG = LoggerFactory.getLogger(GroupingLooseResolver.class);

    private final Class<G> commonInterface;
    private final Set<Class<? extends Augmentation<?>>> classes;

    /**
     * Constructor.
     *
     * @param commonInterface common interface
     */
    public GroupingLooseResolver(Class<G> commonInterface) {
        this.commonInterface = commonInterface;
        classes = new HashSet<>();
    }

    /**
     * Get augmentation classes.
     *
     * @return list of augmentation classes
     */
    public Set<Class<? extends Augmentation<?>>> getClasses() {
        return classes;
    }

    /**
     * Adds an augmentation class.
     *
     * @param cls equivalent augmentation class
     * @return this for chaining
     */
    public GroupingLooseResolver<G> add(Class<? extends Augmentation<?>> cls) {
        Preconditions.checkArgument(commonInterface.isAssignableFrom(cls),
                "oh man! I got " + cls);
        classes.add(cls);
        return this;
    }

    /**
     * Gets the extension for the give data.
     *
     * @param data parameter(data) for getExtension
     * @param <T> type of data
     * @return shared grouping
     */
    @SuppressWarnings("unchecked")
    public <T extends Augmentable<T>> Optional<G> getExtension(DataObject data) {
        // The type of 'data' should really be T for compile-time checking. Several call sites do not pass an
        // Augmentable DataObject type which would result in a ClassCastException at runtime. This is clearly
        // broken - those call sites need to be analyzed to determine the correct behavior in order for this method
        // signature to be changed but for now catch ClassCastException.
        T guessData;
        try {
            guessData = (T) data;
        } catch (ClassCastException e) {
            LOG.warn("Cannot cast to Augmentable", e);
            return Optional.empty();
        }

        for (Class<? extends Augmentation<?>> cls : classes) {
            Augmentation<T> potential = guessData
                    .augmentation((Class<Augmentation<T>>) cls);
            if (potential != null) {
                return Optional.of((G) potential);
            }
        }

        return Optional.empty();
    }
}
