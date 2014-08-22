/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.util.Arrays;

import org.junit.Test;

import org.opendaylight.util.api.PartialSuccessException.Failure;

public class PartialSuccessExceptionTest {

    @Test(expected = PartialSuccessException.class)
    public void test() {
        throw new PartialSuccessException(Arrays.asList("good", "better"),
                                          Arrays.asList(new Failure("bad", "mojo")));
    }

}
