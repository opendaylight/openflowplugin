/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractChangeListner implemented basic {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}
 * processing for flow node subDataObject (flows, groups and meters).
 */
public abstract class AbstractListeningCommiter<T extends DataObject> implements ForwardingRulesCommiter<T>,
        RecoverableListener {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractListeningCommiter.class);

    final ForwardingRulesManager provider;
    NodeConfigurator nodeConfigurator;
    protected final DataBroker dataBroker;

    public AbstractListeningCommiter(ForwardingRulesManager provider, final DataBroker dataBroker) {
        this.provider = Preconditions.checkNotNull(provider, "ForwardingRulesManager can not be null!");
        this.nodeConfigurator = Preconditions.checkNotNull(provider.getNodeConfigurator(),
                "NodeConfigurator can not be null!");
        this.dataBroker = Preconditions.checkNotNull(dataBroker, "DataBroker can not be null!");
        registerListener();
        provider.addRecoverableListener(this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<T>> changes) {
        Preconditions.checkNotNull(changes, "Changes may not be null!");
        LOG.trace("Received data changes :{}", changes);

        for (DataTreeModification<T> change : changes) {
            final InstanceIdentifier<T> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<T> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent =
                    key.firstIdentifierOf(FlowCapableNode.class);
            if (preConfigurationCheck(nodeIdent)) {
                switch (mod.getModificationType()) {
                    case DELETE:
                        remove(key, mod.getDataBefore(), nodeIdent);
                        break;
                    case SUBTREE_MODIFIED:
                        update(key, mod.getDataBefore(), mod.getDataAfter(), nodeIdent);
                        break;
                    case WRITE:
                        if (mod.getDataBefore() == null) {
                            add(key, mod.getDataAfter(), nodeIdent);
                        } else {
                            update(key, mod.getDataBefore(), mod.getDataAfter(), nodeIdent);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                }
            } else {
                if (provider.isStaleMarkingEnabled()) {
                    LOG.info("Stale-Marking ENABLED and switch {} is NOT connected, storing stale entities",
                            nodeIdent.toString());
                    // Switch is NOT connected
                    switch (mod.getModificationType()) {
                        case DELETE:
                            createStaleMarkEntity(key, mod.getDataBefore(), nodeIdent);
                            break;
                        case SUBTREE_MODIFIED:
                            break;
                        case WRITE:
                            break;
                        default:
                            throw new
                            IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                    }
                }
            }
        }
    }

    /**
     * Method return wildCardPath for Listener registration
     * and for identify the correct KeyInstanceIdentifier from data.
     */
    protected abstract InstanceIdentifier<T> getWildCardPath();

    private boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        Preconditions.checkNotNull(nodeIdent, "FlowCapableNode identifier can not be null!");
        // In single node cluster, node should be in local cache before we get any flow/group/meter
        // data change event from data store. So first check should pass.
        // In case of 3-node cluster, when shard leader changes, clustering will send blob of data
        // present in operational data store and config data store. So ideally local node cache
        // should get populated. But to handle a scenario where flow request comes before the blob
        // of config/operational data gets processes, it won't find node in local cache and it will
        // skip the flow/group/meter operational. This requires an addition check, where it reads
        // node from operational data store and if it's present it calls flowNodeConnected to explicitly
        // trigger the event of new node connected.
        return provider.isNodeOwner(nodeIdent)
                && (provider.isNodeActive(nodeIdent) || provider.checkNodeInOperationalDataStore(nodeIdent));
    }
}

