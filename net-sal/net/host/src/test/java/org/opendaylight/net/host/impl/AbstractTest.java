/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host.impl;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Abstract base class for unit tests, with some useful utility methods.
 *
 * @author Simon Hunt
 */
public abstract class AbstractTest {

    /**
     * Prints a title for the method (and includes the ending parens).
     *
     * @param methodName the title string
     */
    protected void title(String methodName) {
        print(EOL + methodName + "()");
    }
}
