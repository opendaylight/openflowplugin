/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper;

import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

/**
 * Created by eshuvka on 6/7/2016.
 */
public class TestData<T extends DataObject> implements DataTreeModification<T> {

    private final DataTreeIdentifier<T> path;
    private final DataObjectModification<T> rootNode;

    public TestData(final InstanceIdentifier<T> path, final T dataBefore, final T dataAfter,
                    DataObjectModification.ModificationType modType) {
        this.path = DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, path);
        this.rootNode = new Test<>(dataBefore, dataAfter, modType);
    }

    @Override
    public DataTreeIdentifier<T> getRootPath() {
        return path;
    }

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
        public PathArgument getIdentifier() {
            return null;
        }

        @Override
        public Class<T> getDataType() {
            return null;
        }

        @Override
        public ModificationType getModificationType() {
            return modification;
        }

        @Override
        public T getDataBefore() {
            return dataObjBefore;
        }

        @Override
        public T getDataAfter() {
            return dataObjAfter;
        }

        @Override
        public Collection<DataObjectModification<? extends DataObject>> getModifiedChildren() {
            return null;
        }

        @Override
        public <C extends ChildOf<? super T>> Collection<DataObjectModification<C>> getModifiedChildren(
                Class<C> childType) {
            return null;
        }

        @Override
        public <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>>
                Collection<DataObjectModification<C>> getModifiedChildren(Class<H> caseType, Class<C> childType) {
            return null;
        }

        @Override
        public <C extends ChildOf<? super T>> DataObjectModification<C> getModifiedChildContainer(Class<C> theClass) {
            return null;
        }

        @Override
        public <H extends ChoiceIn<? super T> & DataObject, C extends ChildOf<? super H>> DataObjectModification<C>
                getModifiedChildContainer(Class<H> caseType, Class<C> child) {
            return null;
        }

        @Override
        public <C extends Augmentation<T> & DataObject> DataObjectModification<C> getModifiedAugmentation(
                Class<C> theClass) {
            return null;
        }

        @Override
        public <C extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<C>> DataObjectModification<C>
                getModifiedChildListItem(Class<C> theClass, K listKey) {
            return null;
        }

        @Override
        public <H extends ChoiceIn<? super T> & DataObject, C extends Identifiable<K> & ChildOf<? super H>,
                K extends Identifier<C>> DataObjectModification<C> getModifiedChildListItem(Class<H> caseType,
                        Class<C> listItem, K listKey) {
            return null;
        }

        @Override
        public DataObjectModification<? extends DataObject> getModifiedChild(PathArgument pathArgument) {
            return null;
        }
    }
}
