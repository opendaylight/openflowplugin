/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.model.Device;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Device ID completer.
 *
 * @author Thomas Vachuska
 */
public class DeviceIdCompleter implements Completer {

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        StringsCompleter delegate = new StringsCompleter();
        DeviceService ds = AbstractShellCommand.get(DeviceService.class);
        Iterator<Device> it = ds.getDevices();
        SortedSet<String> strings = delegate.getStrings();
        while (it.hasNext())
            strings.add(it.next().id().fingerprint());
        return delegate.complete(buffer, cursor, candidates);
    }
}
