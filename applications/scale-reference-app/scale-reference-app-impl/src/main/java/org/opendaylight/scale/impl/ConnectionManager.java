package org.opendaylight.scale.impl;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by evijayd on 9/8/2016.
 */
public class ConnectionManager {

    private static Cluster cluster;
    private static Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);
    private static boolean isMainNode = true;
    private static String host = "localhost";
    private static int replication_factor = 3;

    private static final String MAX_REQUESTS_PER_CONN = "datastax.max.reqs";
    private static final String MIN_CONNECTIONS_PER_HOST = "datastax.min.conn";
    private static final String MAX_CONNECTIONS_PER_HOST = "datastax.max.conn";
    private static final String CONTACT_POINTS = "datastax.contact.points";
    private static final int DEFAULT_MAX_REQUESTS_PER_CONN = 1024;
    private static final int DEFAULT_MIN_CONNECTIONS_PER_HOST = 2;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 4;
    private static final String DEFAULT_CONTACT_POINTS = "localhost";

    private static List<InetAddress> getAddresses(Properties properties) {
        List<String> list = Splitter.on(',')
                .omitEmptyStrings()
                .trimResults()
                .splitToList(properties.getProperty(CONTACT_POINTS, DEFAULT_CONTACT_POINTS));
        List<InetAddress> addresses = new ArrayList<>();
        for (String str: list) {
            try {
                InetAddress address = InetAddress.getByName(str);
                addresses.add(address);
            } catch (Exception e) {
                LOG.error("{} is not a valid address.", str);
            }
        }
        return addresses;
    }

    public static Session getSession(String keySpace) {

        Session session = null;
        Properties properties =
                new Properties(System.getProperties());
        String serverIP = properties.getProperty("CassandraDBServerIP",host);
        LOG.info("Trying to work with {}, Which main node is set to={}",serverIP,isMainNode);
        cluster = Cluster.builder().addContactPoint(serverIP).build();

        for (int index = 0; index < 5; index++) {
            try {
                session = cluster.connect(keySpace);
                return session;
            } catch (InvalidQueryException err) {
                try {
                    LOG.info("Failed to get subscribercontrol keyspace...");
                    if (isMainNode) {
                        LOG.info("This is the main node, trying to create keyspace and tables...");
                        session = cluster.connect();
                        session.execute("CREATE KEYSPACE "+keySpace+" WITH replication "
                                + "= {'class':'SimpleStrategy', 'replication_factor':"+replication_factor+"};");
                        session = cluster.connect(keySpace);
                        return session;
                    }
                } catch (Exception err2) {
                    LOG.error("Failed to create keyspace & tables, will retry in 5 seconds...",err2);
                }
            }
            LOG.info("Sleeping for 5 seconds...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.error("Interrupted",e);
            }
        }
        return session;
    }

    public static Session getAsyncSession(String keySpace) {
        Session session = null;
        try {
            Properties properties =
                    new Properties(System.getProperties());
            String serverIP = properties.getProperty("CassandraDBServerIP",host);
            LOG.info("Trying to work with {}, Which main node is set to={}",serverIP,isMainNode);
            cluster = Cluster.builder()
                    .addContactPoints(getAddresses(properties))
                    .withPoolingOptions(new PoolingOptions()
                            .setConnectionsPerHost(HostDistance.LOCAL,
                                    Integer.getInteger(MIN_CONNECTIONS_PER_HOST, DEFAULT_MIN_CONNECTIONS_PER_HOST),
                                    Integer.getInteger(MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST))
                            .setConnectionsPerHost(HostDistance.REMOTE,
                                    Integer.getInteger(MIN_CONNECTIONS_PER_HOST, DEFAULT_MIN_CONNECTIONS_PER_HOST),
                                    Integer.getInteger(MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST))
                            .setMaxRequestsPerConnection(HostDistance.LOCAL,
                                    Integer.getInteger(MAX_REQUESTS_PER_CONN, DEFAULT_MAX_REQUESTS_PER_CONN))
                            .setMaxRequestsPerConnection(HostDistance.REMOTE,
                                    Integer.getInteger(MAX_REQUESTS_PER_CONN, DEFAULT_MAX_REQUESTS_PER_CONN)))
                    .withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy()))
                    .build();
            LOG.info("Using {} as contact points", getAddresses(properties));
            LOG.info("Using {} min connections, {} max connections and {} max requests per connection",
                    Integer.getInteger(MIN_CONNECTIONS_PER_HOST, DEFAULT_MIN_CONNECTIONS_PER_HOST),
                    cluster.getConfiguration().getPoolingOptions().getMaxConnectionsPerHost(HostDistance.LOCAL),
                    cluster.getConfiguration().getPoolingOptions().getMaxRequestsPerConnection(HostDistance.LOCAL));
            cluster.getConfiguration().getSocketOptions().setReadTimeoutMillis(300000);

            for (int index = 0; index < 5; index++) {
                try {
                    session = cluster.connectAsync(keySpace).get();
                    return session;
                } catch (InvalidQueryException err) {
                    try {
                        LOG.info("Failed to get subscribercontrol keyspace...");
                        if (isMainNode) {
                            LOG.info("This is the main node, trying to create keyspace and tables...");
                            session = cluster.connectAsync().get();
                            session.execute("CREATE KEYSPACE "+keySpace+" WITH replication "
                                    + "= {'class':'SimpleStrategy', 'replication_factor':"+replication_factor+"};");
                            session = cluster.connectAsync(keySpace).get();
                            return session;
                        }
                    } catch (Exception err2) {
                        LOG.error("Failed to create keyspace & tables, will retry in 5 seconds...",err2);
                    }
                }
                LOG.info("Sleeping for 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted",e);
                }
            }
            return session;
        }catch (Exception e){
            LOG.error("Exception in getting session to the keyspace: {}", e);
            return null;
        }
    }
}
