package org.opendaylight.scale.impl;

import com.datastax.driver.core.*;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by evijayd on 11/8/2016.
 */
public class SubscriberRead {

    private static Logger LOG = LoggerFactory.getLogger(SubscriberRead.class);
    private long startTime = 0;

    public void readAllSubscribers(long fetchSize,String selectQuery,Session session){
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
                    if (--remainingInPage == 0)
                        break;
                }
                LOG.info("Done page {}", page);

                boolean wasLastPage = rs.getExecutionInfo().getPagingState() == null;
                if (wasLastPage) {
                    LOG.info("Subscriber Read, Time taken to complete in millisec:"+(System.currentTimeMillis() - startTime));
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
