package org.opendaylight.scale.impl;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by evijayd on 9/8/2016.
 */
public class ConnectionManager {

    private static Cluster cluster;
    private static Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);
    private static boolean isMainNode = true;
    private static String host = "localhost";
    private static int replication_factor = 1;


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
            cluster = Cluster.builder().addContactPoint(serverIP).build();

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
