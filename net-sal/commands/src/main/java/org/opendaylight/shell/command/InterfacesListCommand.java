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
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Interface;

import java.util.List;

@Command(scope = "net", name = "interfaces", description = "Lists network interfaces of all devices")
public class InterfacesListCommand extends DeviceListCommand {

    private static final String FMT = "    id={} state={} mac={} type={}";

    @Argument(index = 0, name = "deviceId", description = "Device ID",
              required = false, multiValued = false)
    String deviceId = null;

    @Override
    protected Object doExecute() throws Exception {
        if (deviceId == null)
            return super.doExecute();

        DeviceService ds = get(DeviceService.class);
        Device d = ds.getDevice(DeviceId.valueOf(deviceId));
        print(d, ds.getInterfaces(d));

        return null;
    }


    @Override
    protected void print(Device d, List<Interface> netInterfaces) {
        super.print(d, netInterfaces);
        for (Interface nif : netInterfaces)
            print(FMT, nif.id(), nif.state(), nif.mac(), nif.type());
    }

}