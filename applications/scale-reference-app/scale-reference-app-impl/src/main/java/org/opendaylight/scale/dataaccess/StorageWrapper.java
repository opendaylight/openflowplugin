package org.opendaylight.scale.dataaccess;

import com.datastax.driver.core.*;
import org.opendaylight.scale.Subsriber;
import org.opendaylight.scale.impl.ConnectionManager;
import org.opendaylight.scale.impl.SubscriberRead;
import org.opendaylight.scale.util.FlowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created by evijayd on 9/26/2016.
 */
public class StorageWrapper {

    private static Logger LOG = LoggerFactory.getLogger(StorageWrapper.class);
    private static Session session = null;
    private final static StorageWrapper INSTANCE = new StorageWrapper();
    private final static String query = "insert into subscribercontrol.subscriberfilterswl(filterid ,subprofileid, srcipprefix ,vni ,portstart ,portend ,priority ,displayame) values (?,?,?,?,?,?,?,?)";
    private final PreparedStatement prepared;
    private final static String DEFAULT_IPV4_PREFIX = "10.0.0.1/32";
    private final static String DEFAULT_SUB_NAME_FOR_BULKADD = "BulkAddSub";

    private StorageWrapper() {
        session = ConnectionManager.getAsyncSession("subscribercontrol");
        prepared = session.prepare(query);
        prepared.setConsistencyLevel(ConsistencyLevel.QUORUM);
    }

    public static StorageWrapper getInstance() {
        return INSTANCE;
    }

    public ResultSetFuture addSubscriber(Subsriber subsriber) {
        LOG.debug("Adding subscriber filter:"+ subsriber.toString());
        BoundStatement bound = prepared.bind(String.valueOf(subsriber.getId()),subsriber.getProfileId(),String.valueOf(subsriber.getIpv4Prefix()),
                subsriber.getVni(),subsriber.getPortStart(),subsriber.getPortEnd(),subsriber.getPriority(),subsriber.getDisplayName());
        return session.executeAsync(bound);
    }

    public ResultSetFuture addSubscriberFromHint(String subscriberId, int portStart) {
        LOG.debug("Adding Subscriber with id: {} and portStart: {}", subscriberId, portStart);
        BoundStatement bound = prepared.bind(subscriberId, 1, FlowUtils.ipIntToStr(Integer.valueOf(subscriberId)), 1, portStart, portStart + 1, 1, DEFAULT_SUB_NAME_FOR_BULKADD);
        return session.executeAsync(bound);
    }

    public ResultSetFuture removeSubscriber(Subsriber subsriber) {
        LOG.debug("Removing subscriber filter:" + subsriber.toString());
        String deleteQuery = "delete from subscribercontrol.subscriberfilterswl where filterid='" + subsriber.getId() + "';";
        return session.executeAsync(deleteQuery);
    }

    public void readAllSubscribers(long fetchSize) {
        LOG.debug("Reading subscriber filters:");
        String selectQuery = "select * from subscribercontrol.subscriberfilterswl";
        SubscriberRead subscriberRead = new SubscriberRead();
        subscriberRead.readAllSubscribers(fetchSize,selectQuery,session);
    }

    public static Session getSession() {
       return session;
    }
}
