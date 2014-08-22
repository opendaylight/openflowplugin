/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.of.lib.AbstractTest;

/**
 * Base class for unsigned id unit tests.
 *
 * @author Simon Hunt
 */
public abstract class UnsignedIdTest extends AbstractTest {

    protected static final int ID_UNDER = -1;
    protected static final String ID_UNDER_STR_DEC = "-1";

    protected static final int ID_MIN = 0;
    protected static final String ID_MIN_STR_DEC = "0";
    protected static final String ID_MIN_STR_HEX = "0x0";
    protected static final String ID_MIN_STR_HEX_PLUS = "0x0(0)";

    // most significant bit not set
    protected static final int ID_LOW = 23;
    protected static final String ID_LOW_STR_DEC = "23";
    protected static final String ID_LOW_STR_HEX = "0x17";
    protected static final String ID_LOW_STR_HEX_PLUS = "0x17(23)";

    protected static final String FOOEY = "fooey";
    protected static final int B = 256;

}
