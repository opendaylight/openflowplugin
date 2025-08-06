/*
 * Copyright (c) 2018 Ericsson S.A. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Abstract class providing some common functionality to abstract listeners. This class is not designed to be
 * extended by the specific listeners, that's why it is package-private. It provides subclasses with access to the
 * {@link DataBroker} passed as constructor argument, listener registration/de-registration and a shutdown method to
 * be implemented if needed by the subclasses (e.g. shutting down services, closing resources, etc.)
 *
 * @param <T> type of the data object the listener is registered to.
 * @author David Suárez (david.suarez.fuentes@gmail.com)
 */
abstract class AbstractDataTreeChangeListener<T extends DataObject>
        implements ClusteredDataTreeChangeListener<T>, ChainableDataTreeChangeListener<T>, AutoCloseable {
    private final ChainableDataTreeChangeListenerImpl<T> chainingDelegate = new ChainableDataTreeChangeListenerImpl<>();
    private final DataBroker dataBroker;
    private final DataTreeIdentifier<T> dataTreeIdentifier;

    private Registration dataChangeListenerRegistration;

    private AbstractDataTreeChangeListener(final DataBroker dataBroker,
            final DataTreeIdentifier<T> dataTreeIdentifier) {
        this.dataBroker = requireNonNull(dataBroker);
        this.dataTreeIdentifier = requireNonNull(dataTreeIdentifier);
    }

    AbstractDataTreeChangeListener(final DataBroker dataBroker, final LogicalDatastoreType datastoreType,
            final InstanceIdentifier<T> instanceIdentifier) {
        this(dataBroker, DataTreeIdentifier.of(datastoreType, instanceIdentifier));
    }

    @Override
    public void addBeforeListener(final DataTreeChangeListener<T> listener) {
        chainingDelegate.addBeforeListener(listener);
    }

    @Override
    public void addAfterListener(final DataTreeChangeListener<T> listener) {
        chainingDelegate.addAfterListener(listener);
    }

    @PostConstruct
    public void register() {
        dataChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    @PreDestroy
    public void close() {
        if (dataChangeListenerRegistration != null) {
            dataChangeListenerRegistration.close();
        }
    }

    /**
     * Default method invoked upon data tree change, in turn it calls the
     * appropriate method (add, update, remove) depending on the type of change.
     *
     * @param changes          collection of changes
     */
    @Override
    public final void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
        for (var dataTreeModification : changes) {
            var instanceIdentifier = dataTreeModification.getRootPath().path();
            var dataObjectModification = dataTreeModification.getRootNode();
            var dataBefore = dataObjectModification.dataBefore();
            var dataAfter = dataObjectModification.dataAfter();

            switch (dataObjectModification.modificationType()) {
                case null -> throw new NullPointerException();
                case SUBTREE_MODIFIED -> update(instanceIdentifier, dataBefore, dataAfter);
                case DELETE -> remove(instanceIdentifier, dataBefore);
                case WRITE -> {
                    if (dataBefore == null) {
                        add(instanceIdentifier, dataAfter);
                    } else {
                        update(instanceIdentifier, dataBefore, dataAfter);
                    }
                }
            }
        }
    }

    /**
     * Invoked when a new data object is added.
     *
     * @param instanceIdentifier instance id for this data object
     * @param newDataObject      newly added object
     */
    abstract void add(@NonNull InstanceIdentifier<T> instanceIdentifier, @NonNull T newDataObject);

    /**
     * Invoked when the data object has been removed.
     *
     * @param instanceIdentifier instance id for this data object
     * @param removedDataObject  existing object being removed
     */
    abstract void remove(@NonNull InstanceIdentifier<T> instanceIdentifier, @NonNull T removedDataObject);

    /**
     * Invoked when there is a change in the data object.
     *
     * @param instanceIdentifier instance id for this data object
     * @param originalDataObject existing object being modified
     * @param updatedDataObject  modified data object
     */
    abstract void update(@NonNull InstanceIdentifier<T> instanceIdentifier, @NonNull T originalDataObject,
            @NonNull T updatedDataObject);
}
