/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.api.security.SecurityContext;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * A basis for understanding how to set up a secure connection with TLS.
 *
 * @author Simon Hunt
 */
public class ExploringTlsTest {

    SecurityContext ctx;

    @Test
    public void basic() {
        print(EOL + "basic()");

        ctx = new SecurityContext();
        print(ctx);

    }
}
