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
import org.opendaylight.yangtools.yang.binding.DataObject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Provides augmentation resolving upon given {@link Augmentable}. 
 * Used {@link Augmentation}s do not share {@link Augmentable}.
 * <br/>
 * <b>Usage:</b> in case there are multiple {@link Augmentable} classes which might contain
 * corresponding {@link Augmentation}s (1:1..n binding). And those {@link Augmentation}s 
 * are sharing the same grouping so that they could be processed in the same way.
 * 
 * @param <GROUPING>
 */
public class GroupingLooseResolver<GROUPING> {

    Class<GROUPING> commonInterface;
    Set<Class<? extends Augmentation<?>>> classes;

    /**
     * @param commonInterface
     * @param common grouping Interface
     */
    public GroupingLooseResolver(Class<GROUPING> commonInterface) {
        this.commonInterface = commonInterface;
        classes = new HashSet<>();
    }

    /**
     * @param cls equivalent augmentation class
     * @return this for chaining
     */
    public GroupingLooseResolver<GROUPING> add(Class<? extends Augmentation<?>> cls) {
        Preconditions.checkArgument(commonInterface.isAssignableFrom(cls),
                "oh man! I got " + cls);
        classes.add(cls);
        return this;
    }

    /**
     * @param data expected to match <tt>&lt;T extends Augmentable&lt;T&gt;&gt;</tt>
     * @return shared grouping
     */
    @SuppressWarnings("unchecked")
    public <T extends Augmentable<T>> Optional<GROUPING> getExtension(DataObject data) {
        T guessData = (T) data;

        for (Class<? extends Augmentation<?>> cls : classes) {
            Augmentation<T> potential = guessData
                    .getAugmentation((Class<Augmentation<T>>) cls);
            if (potential != null) {
                return Optional.of((GROUPING) potential);
            }
        }

        return Optional.absent();
    }
}
