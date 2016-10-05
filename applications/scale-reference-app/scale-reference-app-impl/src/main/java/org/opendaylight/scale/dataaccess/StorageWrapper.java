package org.opendaylight.scale.dataaccess;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.opendaylight.scale.Subsriber;
import org.opendaylight.scale.impl.ConnectionManager;
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

    public void addSubscriber(Subsriber subsriber) {
        LOG.debug("Adding subscriber filter:"+ subsriber.toString());
        BoundStatement bound = prepared.bind(String.valueOf(subsriber.getId()),subsriber.getProfileId(),String.valueOf(subsriber.getIpv4Prefix()),
                subsriber.getVni(),subsriber.getPortStart(),subsriber.getPortEnd(),subsriber.getPriority(),subsriber.getDisplayName());
        session.executeAsync(bound);
    }

    public void addSubscriberFromHint(String subscriberId, int portStart) {
        LOG.debug("Adding Subscriber with id: {} and portStart: {}", subscriberId, portStart);
        BoundStatement bound = prepared.bind(subscriberId, 1, DEFAULT_IPV4_PREFIX, 1, portStart, portStart + 1, 1, DEFAULT_SUB_NAME_FOR_BULKADD);
        session.executeAsync(bound);
    }

    public void removeSubscriber(Subsriber subsriber) {
        LOG.debug("Removing subscriber filter:" + subsriber.toString());
        String deleteQuery = "delete from subscribercontrol.subscriberfilterswl where subprofileid=" + subsriber.getProfileId() + " and srcipprefix='" + subsriber.getIpv4Prefix() +"'";
        session.executeAsync(deleteQuery);
    }
}
