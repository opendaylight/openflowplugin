/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;

import java.io.PrintStream;
import java.util.List;

@Command(scope = "ofp", name = "list-devices", description = "Shows connected devices.")
public class ListDevicesCommandProvider extends OsgiCommandSupport {

        @Override
        protected Object doExecute() throws Exception {
            PrintStream out = session.getConsole();
            final List<String> devices = EventsTimeCounter.provideDevices();
            final StringBuilder result = new StringBuilder();
            for (String line : devices) {
                result.append(line);
                result.append("\n");
            }
            out.print(result.toString());
            return null;
        }

}
