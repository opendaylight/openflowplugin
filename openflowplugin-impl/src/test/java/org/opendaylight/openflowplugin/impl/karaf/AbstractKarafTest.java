/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by mirehak on 7/29/15.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractKarafTest {
    protected static final Function<String, Boolean> CHECK_NO_ACTIVITY_FUNCTION = String::isEmpty;

    @Mock
    protected PrintStream console;

    @Before
    public void setUp() {
        doSetUp();
    }

    public abstract void doSetUp();

    public static boolean checkNoActivity(List<String> allStatLines, Function<String, Boolean> checkFunction) {
        boolean noActivity = true;
        for (String statLine : allStatLines) {
            noActivity &= checkFunction.apply(statLine);
        }
        return noActivity;
    }
}
