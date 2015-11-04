package org.opendaylight.openflowplugin.applications.frm.impl;


import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.openflowplugin.applications.frm.FlowCapableNodeConnectorCommitter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Collection;

public abstract class AbstractNodeConnectorCommitter <T extends DataObject> implements FlowCapableNodeConnectorCommitter<T> {
    protected ForwardingRulesManager provider;

    protected final Class<T> clazz;

    public AbstractNodeConnectorCommitter (ForwardingRulesManager provider, Class<T> clazz) {
        this.provider = Preconditions.checkNotNull(provider, "ForwardingRulesManager can not be null!");
        this.clazz = Preconditions.checkNotNull(clazz, "Class can not be null!");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<T>> changes) {
        Preconditions.checkNotNull(changes, "Changes may not be null!");

        for (DataTreeModification<T> change : changes) {
            final InstanceIdentifier<T> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<T> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent =
                    key.firstIdentifierOf(FlowCapableNodeConnector.class);

            if (preConfigurationCheck(nodeConnIdent)) {
                switch (mod.getModificationType()) {
                    case DELETE:
                        remove(key, mod.getDataBefore(), nodeConnIdent);
                        break;
                    case SUBTREE_MODIFIED:
                        update(key, mod.getDataBefore(), mod.getDataAfter(), nodeConnIdent);
                        break;
                    case WRITE:
                        if (mod.getDataBefore() == null) {
                            add(key, mod.getDataAfter(), nodeConnIdent);
                        } else {
                            update(key, mod.getDataBefore(), mod.getDataAfter(), nodeConnIdent);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                }
            }
        }
    }

    /**
     * Method return wildCardPath for Listener registration
     * and for identify the correct KeyInstanceIdentifier from data;
     */
    protected abstract InstanceIdentifier<T> getWildCardPath();

    private boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        Preconditions.checkNotNull(nodeConnIdent, "FlowCapableNodeConnector ident can not be null!");
        return true;
        //return provider.isNodeActive(nodeConnIdent);
    }
}
