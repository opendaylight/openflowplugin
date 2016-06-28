/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemovedBuilder;


public class LLDPLinkAger implements AutoCloseable {
    private final long linkExpirationTime;
    private final Map<LinkDiscovered, Date> linkToDate;
    private final Timer timer;
    private final NotificationProviderService notificationService;

    /**
     * default ctor - start timer
     */
    public LLDPLinkAger(final long lldpInterval, final long linkExpirationTime,
            final NotificationProviderService notificationService) {
        this.linkExpirationTime = linkExpirationTime;
        this.notificationService = notificationService;
        linkToDate = new ConcurrentHashMap<>();
        timer = new Timer();
        timer.schedule(new LLDPAgingTask(), 0, lldpInterval);
    }

    public void put(LinkDiscovered link) {
        Date expires = new Date();
        expires.setTime(expires.getTime() + linkExpirationTime);
        linkToDate.put(link, expires);
    }

    @Override
    public void close() {
        timer.cancel();
        linkToDate.clear();
    }

    private class LLDPAgingTask extends TimerTask {

        @Override
        public void run() {
            for (Entry<LinkDiscovered,Date> entry : linkToDate.entrySet()) {
                LinkDiscovered link = entry.getKey();
                Date expires = entry.getValue();
                Date now = new Date();
                if(now.after(expires)) {
                    if (notificationService != null) {
                        LinkRemovedBuilder lrb = new LinkRemovedBuilder(link);
                        notificationService.publish(lrb.build());
                        linkToDate.remove(link);
                    }
                }
            }

        }

    }

    @VisibleForTesting
    public boolean isLinkToDateEmpty() {
        return linkToDate.isEmpty();
    }

}

