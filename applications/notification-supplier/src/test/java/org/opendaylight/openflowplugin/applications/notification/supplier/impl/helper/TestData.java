/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by eshuvka on 6/7/2016.
 */
public class TestData<T extends DataObject> implements DataTreeModification<T> {

    private final DataTreeIdentifier<T> path;
    private final DataObjectModification<T> rootNode;

    public TestData(final InstanceIdentifier<T> path, final T dataBefore, final T dataAfter,
                    DataObjectModification.ModificationType modType) {
        this.path = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, path);
        this.rootNode = new Test(dataBefore, dataAfter, modType);
    }

    @Nonnull
    @Override
    public DataTreeIdentifier<T> getRootPath() {
        return path;
    }

    @Nonnull
    @Override
    public DataObjectModification<T> getRootNode() {
        return rootNode;
    }

    class Test<T extends DataObject> implements DataObjectModification<T> {

        private final T dataObjBefore;
        private final T dataObjAfter;
        private final ModificationType modification;

        Test(final T dataBefore, final T dataAfter, ModificationType modType) {
            dataObjBefore = dataBefore;
            dataObjAfter = dataAfter;
            modification = modType;
        }

        @Override
        public InstanceIdentifier.PathArgument getIdentifier() {
            return null;
        }

        @Nonnull
        @Override
        public Class<T> getDataType() {
            return null;
        }

        @Nonnull
        @Override
        public ModificationType getModificationType() {
            return modification;
        }

        @Nullable
        @Override
        public T getDataBefore() {
            return dataObjBefore;
        }

        @Nullable
        @Override
        public T getDataAfter() {
            return dataObjAfter;
        }

        @Nonnull
        @Override
        public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
            return null;
        }

        @Nullable
        @Override
        public <C extends ChildOf<? super T>> DataObjectModification<C> getModifiedChildContainer(
                @Nonnull Class<C> theClass) {
            return null;
        }

        @Nullable
        @Override
        public <C extends Augmentation<T> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                @Nonnull Class<C> theClass) {
            return null;
        }

        @Override
        public <C extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<C>> DataObjectModification<C>
            getModifiedChildListItem(@Nonnull Class<C> theClass, @Nonnull K listKey) {
            return null;
        }

        @Nullable
        @Override
        public DataObjectModification<? extends DataObject> getModifiedChild(
                InstanceIdentifier.PathArgument pathArgument) {
            return null;
        }
    }
}