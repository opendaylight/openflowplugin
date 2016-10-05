package org.opendaylight.scale;

import org.opendaylight.scale.dataaccess.StorageWrapper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static Logger LOG = LoggerFactory.getLogger(Activator.class);

    public void start(BundleContext context) {
        LOG.info("Starting the bundle");
        try {
            StorageWrapper.getInstance().addSubscriber(Subsriber.create()
                    .setId(1)
                    .setIpv4Prefix(new Ipv4Prefix("127.0.0.1/32"))
                    .setPortStart(1)
                    .setPortEnd(2)
                    .setVni(1)
                    .setPriority(1)
                    .setProfileId(2)
                    .setDisplayName("testing"));
            StorageWrapper.getInstance().removeSubscriber(Subsriber.create()
                    .setId(1)
                    .setIpv4Prefix(new Ipv4Prefix("127.0.0.1/32"))
                    .setPortStart(1)
                    .setPortEnd(2)
                    .setVni(1)
                    .setPriority(1)
                    .setProfileId(2)
                    .setDisplayName("testing"));
        }catch (Exception e)
        {
            LOG.error("Error while activation:",e);
        }
    }

    public void stop(BundleContext context) {
        LOG.info("Stopping the bundle");
    }

}