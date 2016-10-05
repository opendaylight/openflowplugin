/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.subscriber;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.reference.app.ReferenceAppUtil;
import org.opendaylight.reference.app.openflow.FlowProgrammer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.SubscriberListEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.subscriber.list.entries.SubscriberListEntry;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clustered DataTree Event Listener for subscriber entry which updates entry in local map.
 * It gets notification whenever add/delete subscriber is triggered on config data store and
 * uses flowprogrammer to update flow in switchs.
 */
public class SubscriberHandler implements ClusteredDataTreeChangeListener<SubscriberListEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriberHandler.class);
    private static final Integer BUCKET_SIZE_RECON = Integer.getInteger("priority.task.coordinator.level.max", 5);
    private static AtomicInteger subscriberCount = new AtomicInteger(0);

    private int hashKey;
    private final DataBroker dataBroker;
    private final FlowProgrammer flowProgrammer;
    private static List<Map<Integer, SubscriberListEntry>> listOfHash = new ArrayList<>();


    public SubscriberHandler(final DataBroker dataBroker, FlowProgrammer flowProgrammer) {
        this.dataBroker = dataBroker;
        Preconditions.checkNotNull(dataBroker, "DataBroker Cannot be null!");
        this.flowProgrammer = flowProgrammer;
        Preconditions.checkNotNull(flowProgrammer, "FlowProgrammer Cannot be null!");
        for (int i = 0; i < BUCKET_SIZE_RECON; i++) {
            listOfHash.add(new ConcurrentHashMap<Integer, SubscriberListEntry>());
        }
    }

    /**
     * register for subscriber event in config data store.
     */
    public void register() {
        final DataTreeIdentifier<SubscriberListEntry> treeId = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, getWildCardPath());
        dataBroker.registerDataTreeChangeListener(treeId, SubscriberHandler.this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<SubscriberListEntry>> changes) {
        Preconditions.checkNotNull(changes, "Data Tree Changes cannot be null");
        for (DataTreeModification<SubscriberListEntry> change : changes) {
            DataObjectModification<SubscriberListEntry> mod = change.getRootNode();
            switch (mod.getModificationType()) {
                case DELETE:
                    removeSubscriber(mod.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        addSubscriber(mod.getDataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled subscriber entry modification type " + mod.getModificationType());
            }
        }
    }

    private InstanceIdentifier<SubscriberListEntry> getWildCardPath() {
        return InstanceIdentifier
                .builder(SubscriberListEntries.class)
                .child(SubscriberListEntry.class).build();
    }

    /**
     * add subscriber in local map and update flow in switchs
     *
     * @param entry - subscriber entry to be added in all the switchs
     */
    private void addSubscriber(SubscriberListEntry entry) {
        LOG.debug("add subscriber {}", entry.getIndex());
        subscriberCount.incrementAndGet();
        hashKey = (subscriberCount.get()) % BUCKET_SIZE_RECON;
        listOfHash.get(hashKey).put(entry.getIndex().intValue(), entry);
        Flow flow = ReferenceAppUtil.convertSubsToFlow(entry);
        flowProgrammer.updateFlowInSwitchs(flow, true);
    }

    /**
     * remove subscriber from map and update flow in switchs
     *
     * @param entry - subscriber entry to be removed from all the switchs
     */
    private void removeSubscriber(SubscriberListEntry entry) {
        LOG.debug("remove subscriber {}", entry.getIndex());
        SubscriberListEntry deletedEntry = null;
        for (int i = 0; i < BUCKET_SIZE_RECON; i++) {
            deletedEntry = listOfHash.get(i).remove(entry.getIndex().intValue());
            if (deletedEntry != null) {
                break;
            }
        }
        Flow flow = ReferenceAppUtil.convertSubsToFlow(entry);
        flowProgrammer.updateFlowInSwitchs(flow, false);
    }

    /**
     * get subscriber map.
     *
     * @return - subscriber map
     */
    public static List<Map<Integer, SubscriberListEntry>> getListOfHash() {
        return listOfHash;
    }

}