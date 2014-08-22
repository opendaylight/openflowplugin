/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.commands.Command;
import org.opendaylight.net.driver.DeviceDriverService;
import org.opendaylight.net.host.HostService;
import org.opendaylight.net.model.Host;
import org.opendaylight.util.driver.DeviceDriverBroker;

import java.util.Iterator;

@Command(scope = "net", name = "drivers", description="Lists all device drivers")
public class DriverListCommand extends AbstractShellCommand {

    private static final String FMT = "typeName={} provider={}";

    @Override
    protected Object doExecute() throws Exception {
        DeviceDriverBroker broker = get(DeviceDriverBroker.class);
        for (String typeName : broker.getDeviceTypeNames())
            print(FMT, typeName, broker.getProvider(typeName));

        return null;
    }

}