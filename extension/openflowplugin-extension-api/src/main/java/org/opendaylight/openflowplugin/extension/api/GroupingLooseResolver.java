/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.Grouping;
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
public class GroupingLooseResolver<G extends Grouping> {
    private static final Logger LOG = LoggerFactory.getLogger(GroupingLooseResolver.class);

    private final Set<Class<? extends Augmentation<?>>> classes;

    /**
     * Constructor.
     *
     * @param commonInterface common interface
     */
    @SafeVarargs
    public GroupingLooseResolver(final Class<G> commonInterface, final Class<? extends Augmentation<?>>... classes) {
        this.classes = ImmutableSet.copyOf(classes);
        this.classes.forEach(
            cls -> Preconditions.checkArgument(commonInterface.isAssignableFrom(cls), "oh man! I got " + cls));
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
     * Gets the extension for the give data.
     *
     * @param data parameter(data) for getExtension
     * @return shared grouping
     */
    @SuppressWarnings("unchecked")
    public Optional<G> getExtension(final DataContainer data) {
        // This way look weird, but the caller may only have a Grouping view of the DataObject, hence we need to see
        // if the object even supports augmentations.
        if (data instanceof Augmentable augmentable) {
            for (var cls : classes) {
                final var aug = augmentable.augmentation(cls);
                if (aug != null) {
                    return Optional.of((G) aug);
                }
            }
        } else {
            LOG.warn("Cannot cast {} to Augmentable", data.getClass());
        }
        return Optional.empty();
    }
}
