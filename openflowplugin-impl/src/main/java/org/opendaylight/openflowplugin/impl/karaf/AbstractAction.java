/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import java.io.PrintStream;
import org.apache.karaf.shell.api.action.Action;

/**
 * An abstract {@link Action}.
 */
abstract class AbstractAction implements Action {
    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public final Object execute() {
        execute(System.out);
        return null;
    }

    abstract void execute(PrintStream out);
}
