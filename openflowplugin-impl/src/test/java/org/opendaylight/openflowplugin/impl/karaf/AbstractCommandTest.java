/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import org.apache.karaf.shell.api.console.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Created by mirehak on 7/29/15.
 */
@ExtendWith(MockitoExtension.class)
abstract class AbstractCommandTest {
    @Mock
    protected Session session;
    @Mock
    protected PrintStream console;

    @BeforeEach
    void beforeEach() {
        doReturn(console).when(session).getConsole();
        doBeforeEach();
    }

    protected abstract void doBeforeEach();

    protected static void assertNoActivity(final List<String> allStatLines,
            final Function<String, Boolean> checkFunction) {
        assertTrue(checkNoActivity(allStatLines, checkFunction));
    }

    protected static void assertHasActivity(final List<String> allStatLines,
            final Function<String, Boolean> checkFunction) {
        assertFalse(checkNoActivity(allStatLines, checkFunction));
    }

    private static boolean checkNoActivity(final List<String> allStatLines,
            final Function<String, Boolean> checkFunction) {
        for (var statLine : allStatLines) {
            if (!checkFunction.apply(statLine)) {
                return false;
            }
        }
        return true;
    }
}
