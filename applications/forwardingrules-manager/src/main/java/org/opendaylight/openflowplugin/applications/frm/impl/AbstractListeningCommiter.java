/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractChangeListner implemented basic {@link org.opendaylight.mdsal.binding.api.DataTreeModification}
 * processing for flow node subDataObject (flows, groups and meters).
 */
public abstract class AbstractListeningCommiter<T extends DataObject>
        implements ForwardingRulesCommiter<T>, RecoverableListener {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractListeningCommiter.class);

    final ForwardingRulesManager provider;
    NodeConfigurator nodeConfigurator;
    protected final DataBroker dataBroker;

    private Registration listenerRegistration;

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "See FIXME below")
    protected AbstractListeningCommiter(final ForwardingRulesManager provider, final DataBroker dataBroker) {
        this.provider = requireNonNull(provider, "ForwardingRulesManager can not be null!");
        nodeConfigurator = requireNonNull(provider.getNodeConfigurator(), "NodeConfigurator can not be null!");
        this.dataBroker = requireNonNull(dataBroker, "DataBroker can not be null!");

        // FIXME: this may start listening on an uninitialized object: clean up the lifecycle here
        registerListener();
        provider.addRecoverableListener(this);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
        LOG.trace("Received data changes :{}", requireNonNull(changes, "Changes may not be null!"));

        for (DataTreeModification<T> change : changes) {
            final InstanceIdentifier<T> key = change.getRootPath().path();
            final DataObjectModification<T> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent =
                    key.firstIdentifierOf(FlowCapableNode.class);
            try {
                if (preConfigurationCheck(nodeIdent)) {
                    switch (mod.modificationType()) {
                        case DELETE:
                            remove(key, mod.dataBefore(), nodeIdent);
                            break;
                        case SUBTREE_MODIFIED:
                            update(key, mod.dataBefore(), mod.dataAfter(), nodeIdent);
                            break;
                        case WRITE:
                            final var dataBefore = mod.dataBefore();
                            if (dataBefore == null) {
                                add(key, mod.dataAfter(), nodeIdent);
                            } else {
                                update(key, dataBefore, mod.dataAfter(), nodeIdent);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unhandled modification type " + mod.modificationType());
                    }
                } else if (provider.isStaleMarkingEnabled()) {
                    LOG.info("Stale-Marking ENABLED and switch {} is NOT connected, storing stale entities", nodeIdent);
                    // Switch is NOT connected
                    switch (mod.modificationType()) {
                        case DELETE:
                            createStaleMarkEntity(key, mod.dataBefore(), nodeIdent);
                            break;
                        case SUBTREE_MODIFIED:
                        case WRITE:
                            break;
                        default:
                            throw new IllegalArgumentException("Unhandled modification type " + mod.modificationType());
                    }
                }
            } catch (RuntimeException e) {
                LOG.error("Failed to handle event {} key {} due to error ", mod.modificationType(), key, e);
            }
        }
    }

    @Override
    public final void registerListener() {
        listenerRegistration = dataBroker.registerTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, getWildCardPath()), this);
    }

    @Override
    public void deregisterListener() {
        close();
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    /**
     * Method return wildCardPath for Listener registration
     * and for identify the correct KeyInstanceIdentifier from data.
     */
    protected abstract InstanceIdentifier<T> getWildCardPath();

    private boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        requireNonNull(nodeIdent, "FlowCapableNode identifier can not be null!");
        // In single node cluster, node should be in local cache before we get any flow/group/meter
        // data change event from data store. So first check should pass.
        // In case of 3-node cluster, when shard leader changes, clustering will send blob of data
        // present in operational data store and config data store. So ideally local node cache
        // should get populated. But to handle a scenario where flow request comes before the blob
        // of config/operational data gets processes, it won't find node in local cache and it will
        // skip the flow/group/meter operational. This requires an addition check, where it reads
        // node from operational data store and if it's present it calls flowNodeConnected to explicitly
        // trigger the event of new node connected.
        return provider.isNodeOwner(nodeIdent);
    }
}
