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
import org.opendaylight.net.host.HostService;
import org.opendaylight.net.model.Host;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Host ID completer.
 *
 * @author Thomas Vachuska
 */
public class HostIdCompleter implements Completer {

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        StringsCompleter delegate = new StringsCompleter();
        HostService hs = AbstractShellCommand.get(HostService.class);
        Iterator<Host> it = hs.getHosts();
        SortedSet<String> strings = delegate.getStrings();
        while (it.hasNext())
            strings.add(it.next().ip().toString());
        return delegate.complete(buffer, cursor, candidates);
    }
}
