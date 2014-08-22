/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.opendaylight.util.Task;

/**
 * Test suite for the abstract task base.
 * 
 * @author Thomas Vachuska
 */
public class TaskTest {

    private class TestTask extends Task {

        public TestTask() {
            super("TestTask");
        }

        @Override
        public void run() {
            while (!stopped())
                delay(10);
        }
    }

    @Test
    public void testBasics() {
        TestTask tt = new TestTask();
        tt.start();
        Task.delay(20);
        tt.cease();
        Task.delay(100);
        assertFalse("task should not be alive", tt.isAlive());
    }
}
