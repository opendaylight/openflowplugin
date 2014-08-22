/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.util.LocalMemory;

/**
 * {@link org.opendaylight.util.LocalMemory} tests.
 * 
 * @author Fabiel Zuniga
 */
public class LocalMemoryTest {

    @Test
    public void testConstruction() {
        LocalMemory<String> sharedMemory = new LocalMemory<String>();
        Assert.assertNull(sharedMemory.read());

        sharedMemory = new LocalMemory<String>("Hello World");
        Assert.assertEquals("Hello World", sharedMemory.read());
    }

    @Test
    public void testWriteRead() {
        LocalMemory<String> sharedMemory = new LocalMemory<String>();
        sharedMemory.write("Hello World");
        Assert.assertEquals("Hello World", sharedMemory.read());
    }
}
