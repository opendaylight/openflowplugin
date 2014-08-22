/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.junit.TestTools;

/**
 * Base class for unit tests of model objects.
 *
 * @author Simon Hunt
 */
public class AbstractModelTest {

    /**
     * Prints the given string as a method name, followed by "()".
     *
     * @param methodName the test method name
     */
    protected void title(String methodName) {
        TestTools.print(TestTools.EOL + methodName + "()");
    }
}
