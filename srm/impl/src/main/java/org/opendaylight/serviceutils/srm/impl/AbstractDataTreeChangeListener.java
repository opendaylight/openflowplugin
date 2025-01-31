/*
 * Copyright (c) 2018 Ericsson S.A. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
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
public abstract class AbstractDataTreeChangeListener<T extends DataObject>
        implements ClusteredDataTreeChangeListener<T>, DataTreeChangeListenerActions<T>,
                   ChainableDataTreeChangeListener<T>, AutoCloseable {
    private final ChainableDataTreeChangeListenerImpl<T> chainingDelegate = new ChainableDataTreeChangeListenerImpl<>();
    private final DataBroker dataBroker;
    private final DataTreeIdentifier<T> dataTreeIdentifier;

    private Registration dataChangeListenerRegistration;

    private AbstractDataTreeChangeListener(DataBroker dataBroker, DataTreeIdentifier<T> dataTreeIdentifier) {
        this.dataBroker = dataBroker;
        this.dataTreeIdentifier = dataTreeIdentifier;
    }

    protected AbstractDataTreeChangeListener(DataBroker dataBroker, LogicalDatastoreType datastoreType,
                                   InstanceIdentifier<T> instanceIdentifier) {
        this(dataBroker, DataTreeIdentifier.of(datastoreType, instanceIdentifier));
    }

    @Override
    public void addBeforeListener(DataTreeChangeListener<T> listener) {
        chainingDelegate.addBeforeListener(listener);
    }

    @Override
    public void addAfterListener(DataTreeChangeListener<T> listener) {
        chainingDelegate.addAfterListener(listener);
    }

    @PostConstruct
    public void register() {
        this.dataChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    protected DataBroker getDataBroker() {
        return dataBroker;
    }

    @Override
    @PreDestroy
    public void close() {
        if (dataChangeListenerRegistration != null) {
            dataChangeListenerRegistration.close();
        }
    }

    @Override
    @Deprecated
    public void add(T newDataObject) {
        // TODO: to be removed after all listeners migrated to use the new methods
    }

    @Override
    @Deprecated
    public void remove(T removedDataObject) {
        // TODO: to be removed after all listeners migrated to use the new methods
    }

    @Override
    @Deprecated
    public void update(T originalDataObject, T updatedDataObject) {
        // TODO: to be removed after all listeners migrated to use the new methods
    }
}
