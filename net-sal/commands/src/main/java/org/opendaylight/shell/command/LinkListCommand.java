/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.opendaylight.net.link.LinkService;
import org.opendaylight.net.model.*;

import java.util.Iterator;

import static org.opendaylight.util.TimeUtils.rfc822Timestamp;

@Command(scope = "net", name = "links", description = "Lists all network infrastructure links")
public class LinkListCommand extends AbstractShellCommand {

    private static final String FMT = "src={}:{} dst={}:{} type={} ts={}";

    @Argument(index = 0, name = "deviceId", description = "Device ID",
              required = false, multiValued = false)
    String deviceId = null;

    @Override
    protected Object doExecute() throws Exception {
        LinkService ls = get(LinkService.class);

        Iterator<Link> it = deviceId == null ? ls.getLinks() :
                ls.getLinks(DeviceId.valueOf(deviceId)).iterator();
        while (it.hasNext())
            print(it.next());

        return null;
    }

    protected void print(Link link) {
        print(FMT, elementId(link.src()), link.src().interfaceId(),
              elementId(link.dst()), link.dst().interfaceId(), link.type(),
              rfc822Timestamp(link.timestamp()));
    }

    static String elementId(ConnectionPoint cp) {
        ElementId eid = cp.elementId();
        return eid instanceof HostId ?
                ((HostId) eid).ip().toString() : eid.toString();
    }

}