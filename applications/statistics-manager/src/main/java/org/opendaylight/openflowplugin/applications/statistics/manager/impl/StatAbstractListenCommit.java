/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatListeningCommiter;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatNodeRegistration;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * statistics-manager
 * org.opendaylight.openflowplugin.applications.statistics.manager.impl
 *
 * StatAbstractListeneningCommiter
 * Class is abstract implementation for all Configuration/DataStore DataChange
 * listenable DataObjects like flows, groups, meters. It is a holder for common
 * functionality needed by construction/destruction class and for DataChange
 * event processing.
 *
 */
public abstract class StatAbstractListenCommit<T extends DataObject, N extends NotificationListener>
                                            extends StatAbstractNotifyCommit<N> implements StatListeningCommiter<T,N> {

    private static final Logger LOG = LoggerFactory.getLogger(StatAbstractListenCommit.class);

    private ListenerRegistration<DataChangeListener> listenerRegistration;

    protected final Map<InstanceIdentifier<Node>, Map<InstanceIdentifier<T>, Integer>> mapNodesForDelete = new ConcurrentHashMap<>();
    protected final Map<InstanceIdentifier<Node>, Integer> mapNodeFeautureRepeater = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    private final DataBroker dataBroker;

    protected final StatNodeRegistration nodeRegistrationManager;

    private ReadOnlyTransaction currentReadTx;
    private volatile boolean currentReadTxStale;

    /* Constructor has to make a registration */
    public StatAbstractListenCommit(final StatisticsManager manager, final DataBroker db,
            final NotificationProviderService nps, final Class<T> clazz, final StatNodeRegistration nodeRegistrationManager) {
        super(manager,nps, nodeRegistrationManager);
        this.clazz = Preconditions.checkNotNull(clazz, "Referenced Class can not be null");
        Preconditions.checkArgument(db != null, "DataBroker can not be null!");
        listenerRegistration = db.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                getWildCardedRegistrationPath(), this, DataChangeScope.BASE);
        this.dataBroker = db;
        this.nodeRegistrationManager = nodeRegistrationManager;
    }

    /**
     * Method returns WildCarded Path which is used for registration as a listening path changes in
     * {@link org.opendaylight.controller.md.sal.binding.api.DataChangeListener}
     * @return
     */
    protected abstract InstanceIdentifier<T> getWildCardedRegistrationPath();

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent) {
        Preconditions.checkNotNull(changeEvent,"Async ChangeEvent can not be null!");

        /*
         * If we have opened read transaction for configuration data store, we need to mark it as stale.
         *
         * Latest read transaction will be allocated on another read using readLatestConfiguration
         */
        currentReadTxStale = true;
    }

    @SuppressWarnings("unchecked")
    protected void removeData(final InstanceIdentifier<?> key, final Integer value) {
        if (clazz.equals(key.getTargetType())) {
            final InstanceIdentifier<Node> nodeIdent = key.firstIdentifierOf(Node.class);
            Map<InstanceIdentifier<T>, Integer> map = null;
            if (mapNodesForDelete.containsKey(nodeIdent)) {
                map = mapNodesForDelete.get(nodeIdent);
            }
            if (map == null) {
                map = new ConcurrentHashMap<>();
                mapNodesForDelete.put(nodeIdent, map);
            }
            map.put((InstanceIdentifier<T>) key, value);
        }
    }

    @Override
    public void cleanForDisconnect(final InstanceIdentifier<Node> nodeIdent) {
        mapNodesForDelete.remove(nodeIdent);
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (final Exception e) {
                LOG.error("Error by stop {} DataChange StatListeningCommiter.", clazz.getSimpleName(), e);
            }
            listenerRegistration = null;
        }

        super.close();
    }

    /**
     * Method return actual DataObject identified by InstanceIdentifier from Config/DS
     * @param path
     * @return
     */
    protected final <K extends DataObject> Optional<K> readLatestConfiguration(final InstanceIdentifier<K> path) {
        for(int i = 0; i < 2; i++) {
            boolean localReadTxStale = currentReadTxStale;

            // This non-volatile read piggy backs the volatile currentReadTxStale read above to
            // ensure visibility in case this method is called across threads (although not concurrently).
            ReadOnlyTransaction localReadTx = currentReadTx;
            if(localReadTx == null || localReadTxStale) {
                if(localReadTx != null) {
                    localReadTx.close();
                }

                localReadTx = dataBroker.newReadOnlyTransaction();

                currentReadTx = localReadTx;

                // Note - this volatile write also publishes the non-volatile currentReadTx write above.
                currentReadTxStale = false;
            }

            try {
                return localReadTx.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            } catch (final ReadFailedException e) {
                LOG.debug("It wasn't possible to read {} from datastore. Exception: {}", path, e);

                // Loop back and try again with a new Tx.
                currentReadTxStale = true;
            }
        }

        return Optional.absent();
    }

}

