/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.mdsal.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of ChainableDataTreeChangeListener.
 *
 * <p>Suitable as a delegate for listeners implementing ChainableDataTreeChangeListener.
 *
 * @author Michael Vorburger
 * @deprecated Use {@code listener-api} instead.
 */
@Deprecated
public final class ChainableDataTreeChangeListenerImpl<T extends DataObject>
        implements ChainableDataTreeChangeListener<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ChainableDataTreeChangeListenerImpl.class);

    private final List<DataTreeChangeListener<T>> beforeListeners = new CopyOnWriteArrayList<>();
    private final List<DataTreeChangeListener<T>> afterListeners = new CopyOnWriteArrayList<>();

    @Override
    public void addBeforeListener(DataTreeChangeListener<T> listener) {
        beforeListeners.add(listener);
    }

    @Override
    public void addAfterListener(DataTreeChangeListener<T> listener) {
        afterListeners.add(listener);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public void notifyBeforeOnDataTreeChanged(List<DataTreeModification<T>> changes) {
        for (DataTreeChangeListener<T> listener : beforeListeners) {
            try {
                listener.onDataTreeChanged(changes);
            } catch (Exception e) {
                LOG.error("Caught Exception from a before listener's onDataChanged(); "
                        + "nevertheless proceeding with others, if any", e);
            }
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public void notifyAfterOnDataTreeChanged(List<DataTreeModification<T>> changes) {
        for (DataTreeChangeListener<T> listener : afterListeners) {
            try {
                listener.onDataTreeChanged(changes);
            } catch (Exception e) {
                LOG.error("Caught Exception from an after listener's onDataChanged(); "
                        + "nevertheless proceeding with others, if any", e);
            }
        }
    }

}
