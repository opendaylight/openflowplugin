/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.commands.Command;
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.Interface;
import org.opendaylight.util.driver.DeviceIdentity;

import java.util.Iterator;
import java.util.List;

@Command(scope = "net", name = "devices", description="Lists all network devices")
public class DeviceListCommand extends AbstractShellCommand {

    private static final String FMT = "id={} mfr={} hw={} sw={} serial={} online={} ports={}";

    @Override
    protected Object doExecute() throws Exception {
        DeviceService ds = get(DeviceService.class);
        Iterator<Device> it = ds.getDevices();
        while (it.hasNext()) {
            Device d = it.next();
            print(d, ds.getInterfaces(d));
        }

        return null;
    }

    protected void print(Device d, List<Interface> netInterfaces) {
        DeviceIdentity di = d.info().getFacet(DeviceIdentity.class);
        print(FMT, d.id(), di.getVendor(), di.getProductNumber(),
              di.getFirmwareVersion(), di.getSerialNumber(),
              d.isOnline(), netInterfaces.size());
    }

}