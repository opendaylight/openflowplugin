package org.opendaylight.scale.reconciliation;

import com.datastax.driver.core.*;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.scale.ScaleReferenceApp;
import org.opendaylight.scale.dataaccess.StorageWrapper;
import org.opendaylight.scale.impl.SubscriberRead;
import org.opendaylight.scale.util.FlowUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by evijayd on 11/9/2016.
 */
public class FlowReconciliation {

    private static Logger LOG = LoggerFactory.getLogger(FlowReconciliation.class);
    private long startTime = 0;
    private int fetchSize = 5000;
    private IOpenflowFacade openflowFacade;
    private Node node = null;

    public void start(Node node){
        this.node = node;
        this.openflowFacade = ScaleReferenceApp.getSouthboundmanagerDependency();
        readAllSubscribers(fetchSize,StorageWrapper.getSession());
    }

    public void readAllSubscribers(long fetchSize,Session session){
        String selectQuery = "select * from subscribercontrol.subscriberfilterswl";
        Statement statement = new SimpleStatement(selectQuery).setFetchSize((int)fetchSize);
        statement.setConsistencyLevel(ConsistencyLevel.QUORUM);
        startTime = System.currentTimeMillis();
        ListenableFuture<ResultSet> future = Futures.transform(
                session.executeAsync(statement),
                iterate(1));
    }

    private AsyncFunction<ResultSet, ResultSet> iterate(final int page) {
        return new AsyncFunction<ResultSet, ResultSet>() {
            @Override
            public ListenableFuture<ResultSet> apply(ResultSet rs) throws Exception {

                // How far we can go without triggering the blocking fetch:
                int remainingInPage = rs.getAvailableWithoutFetching();

                LOG.info("Starting page {} ({} rows)", page, remainingInPage);

                for (Row row : rs) {
                    LOG.debug("[page {} - {}] row = {}", page, remainingInPage, row);
                    try {
                        Flow flow = FlowUtils.convertSubsToFlowIPPrefix(row.getString("filterid"), new Ipv4Prefix(row.getString("srcipprefix")));
                        LOG.info(flow.getId()+" - Constructed the flows for reconciliation:" + flow);
                        openflowFacade.modifyFlow(node.getId(), flow, null, true);

                    }catch (Exception e){
                        LOG.info("Exception in sending the flows:",e);
                    }

                    if (--remainingInPage == 0)
                        break;
                }
                LOG.info("Done page {}", page);

                boolean wasLastPage = rs.getExecutionInfo().getPagingState() == null;
                if (wasLastPage) {
                    LOG.info("Subscriber Read, Time taken to complete in millisec:" + (System.currentTimeMillis() - startTime));
                    LOG.info("Done iterating all the subscribers");
                    return Futures.immediateFuture(rs);
                } else {
                    ListenableFuture<ResultSet> future = rs.fetchMoreResults();
                    return Futures.transform(future, iterate(page + 1));
                }
            }
        };
    }
}
