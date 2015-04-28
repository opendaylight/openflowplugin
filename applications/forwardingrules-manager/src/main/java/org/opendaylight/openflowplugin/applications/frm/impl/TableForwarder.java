package org.opendaylight.openflowplugin.applications.frm.impl;

import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;

import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.OriginalTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableRef;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;

public class TableForwarder extends AbstractListeningCommiter<Table> {

    private static final Logger LOG = LoggerFactory.getLogger(TableForwarder.class);

    private ListenerRegistration<TableForwarder> listenerRegistration;

    public TableForwarder (final ForwardingRulesManager manager, final DataBroker db) {
        super(manager, Table.class);
        Preconditions.checkNotNull(db, "DataBroker can not be null!");
        final DataTreeIdentifier<Table> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, getWildCardPath());

        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<TableForwarder>>() {
                @Override
                public ListenerRegistration<TableForwarder> call() throws Exception {
                    return db.registerDataTreeChangeListener(treeId, TableForwarder.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn("FRM Table DataChange listener registration fail!");
            LOG.debug("FRM Table DataChange listener registration fail ..", e);
            throw new IllegalStateException("TableForwarder startup fail! System needs restart.", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (Exception e) {
                LOG.error("Error by stop FRM TableChangeListener.", e);
            }
            listenerRegistration = null;
        }
    }

    @Override
    protected InstanceIdentifier<Table> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).child(Table.class);
    }

    @Override
    public void remove(final InstanceIdentifier<Table> identifier, final Table removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
      // DO Nothing
    }

    @Override
    public void update(final InstanceIdentifier<Table> identifier,
                       final Table original, final Table update,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug( "Received the Table Update request [Tbl id, node Id, original, upd" +
                       " " + identifier + " " + nodeIdent + " " + original + " " + update );

        final Table originalTable = (original);
        Table updatedTable ;
        if( null == update)
          updatedTable = (original);
        else
          updatedTable = (update);

        final UpdateTableInputBuilder builder = new UpdateTableInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setTableRef(new TableRef(identifier));

        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));

        builder.setUpdatedTable((new UpdatedTableBuilder(updatedTable)).build());

        builder.setOriginalTable((new OriginalTableBuilder(originalTable)).build());
        LOG.debug( "Invoking SalTableService " ) ;

        if( this.provider.getSalTableService() != null )
        	System.out.println( " Handle to SalTableServices" + this.provider.getSalTableService()) ;
        this.provider.getSalTableService().updateTable(builder.build());

    }

    @Override
    public void add(final InstanceIdentifier<Table> identifier, final Table addDataObj,
                    final InstanceIdentifier<FlowCapableNode> nodeIdent) {
       //DO NOthing
    }


}
