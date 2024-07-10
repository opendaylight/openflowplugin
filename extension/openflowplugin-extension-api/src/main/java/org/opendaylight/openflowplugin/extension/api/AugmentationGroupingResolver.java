/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Resolver providing a bridge between a grouping and its various instantiations via augment. This is useful for
 * extracting the grouping's content from a DataObject's augmentations without knowing from which instantiation it
 * comes from.
 *
 * <p>
 * Typical use case is, given a base grouping module:
 * <pre>
 *     module foo;
 *
 *     grouping foo {
 *         container augmentable {
 *
 *         }
 *     }
 * </pre>
 * and a module independent of it:
 * <pre>
 *     module bar;
 *
 *     container bar {
 *         uses foo:foo;
 *     }
 * </pre>
 * and
 * <pre>
 *     module baz;
 *
 *     container baz {
 *         uses foo:foo;
 *     }
 * </pre>
 * an external module can perform:
 * <pre>
 *     module xyzzy;
 *
 *     import bar { prefix bar; }
 *     import baz { prefix baz; }
 *
 *     grouping something {
 *         // ...
 *     }
 *
 *     augment /bar:bar/bar:augmentable {
 *         uses something;
 *     }
 *
 *     augment /baz:baz/baz:augmentable {
 *         uses something;
 *     }
 * </pre>
 * The augmentations of {@code bar} and {@code baz} instantiations of {@code grouping foo} have an equivalent
 * augmentation introduced by {@code xyzzy}. This equivalence is not expressed is generated code, in that it is
 * not apparent given {@code bar} or {@code baz} there is an augmentation which provides {@code something}.
 *
 * <p>
 * This class provides the static knowledge to ask for the contents of {@code something} given an instance of
 * {@code augmentable}, without knowing which augmentation introduces it.
 *
 * @param <G> Grouping type
 * @param <T> Augmentable type
 */
@Beta
public final class AugmentationGroupingResolver<G extends DataObject, T extends Augmentable<T>> implements Immutable {
    private final Class<? extends Augmentation<T>>[] augmentations;
    private final Class<G> grouping;

    AugmentationGroupingResolver(final Class<G> grouping, final Class<? extends Augmentation<T>>[] augmentations) {
        this.grouping = requireNonNull(grouping);
        this.augmentations = requireNonNull(augmentations);
    }

    public @NonNull Optional<G> findExtension(final T data) {
        requireNonNull(data);

        for (Class<? extends Augmentation<T>> cls : augmentations) {
            final Augmentation<T> potential = data.augmentation(cls);
            if (potential != null) {
                return Optional.of(grouping.cast(potential));
            }
        }
        return Optional.empty();
    }

    public static <G extends DataObject, T extends Augmentable<T>> @NonNull Builder<G, T> builder(
            final Class<G> groupingClass) {
        return new Builder<>(groupingClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Augmentable<T>> @NonNull Factory<T> factory(final Class<T> augmentableClass,
            final Set<Class<? extends Augmentation<T>>> augmentationClasses) {
        // Defensive copy via .clone() to guard against evil Set implementations
        final Class<?>[] array = augmentationClasses.toArray(new Class<?>[0]).clone();

        // Defensive check of all array elements
        for (Class<?> clazz : array) {
            checkArgument(Augmentation.class.isAssignableFrom(clazz), "Class %s is not an Augmentation", clazz);
        }

        return new Factory<>((Class<? extends Augmentation<T>>[]) array);
    }

    public static final class Builder<G extends DataObject, T extends Augmentable<T>> {
        private final Set<Class<? extends Augmentation<T>>> augmentations = new HashSet<>();
        private final Class<G> grouping;

        Builder(final Class<G> groupingClass) {
            grouping = requireNonNull(groupingClass);
        }

        public <X extends Augmentation<T>> @NonNull Builder<G, T> addAugmentationClass(
                final Class<X> augmentationClass) {
            checkAssignable(grouping, augmentationClass);
            augmentations.add(augmentationClass);
            return this;
        }

        @SuppressWarnings("unchecked")
        public @NonNull AugmentationGroupingResolver<G, T> build() {
            return new AugmentationGroupingResolver<>(grouping,
                    (Class<? extends Augmentation<T>>[]) augmentations.toArray(new Class<?>[0]));
        }
    }

    public static final class Factory<T extends Augmentable<T>> implements Immutable {
        private final Class<? extends Augmentation<T>>[] augmentations;

        Factory(final Class<? extends Augmentation<T>>[] augmentations) {
            this.augmentations = requireNonNull(augmentations);
        }

        public <G extends DataObject> @NonNull AugmentationGroupingResolver<G, T> createResolver(
                final Class<G> groupingClass) {
            for (Class<? extends Augmentation<T>> cls : augmentations) {
                checkAssignable(groupingClass, cls);
            }

            return new AugmentationGroupingResolver<>(groupingClass, augmentations);
        }
    }

    static void checkAssignable(final Class<?> groupingClass, final Class<?> augmentationClass) {
        checkArgument(groupingClass.isAssignableFrom(augmentationClass), "%s is not compatible with grouping %s",
            augmentationClass, groupingClass);
    }
}
