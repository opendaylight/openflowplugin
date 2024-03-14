/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import java.util.List;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;

/**
 * General dropAllPackets on/off argument completer.
 */
@Service
public class DropAllPacketsCompleter implements Completer {

    @Override
    public int complete(final Session session, final CommandLine commandLine, final List<String> candidates) {
        final var delegate = new StringsCompleter();
        delegate.getStrings().add("on");
        delegate.getStrings().add("off");
        return delegate.complete(session, commandLine, candidates);
    }
}
