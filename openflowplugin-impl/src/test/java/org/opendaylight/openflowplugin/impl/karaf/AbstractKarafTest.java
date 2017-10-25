/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import com.google.common.base.Function;
import java.io.PrintStream;
import java.util.List;
import org.apache.felix.service.command.CommandSession;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by mirehak on 7/29/15.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractKarafTest {
    @Mock
    protected CommandSession cmdSession;
    @Mock
    protected PrintStream console;

    @Before
    public void setUp() {
        Mockito.when(cmdSession.getConsole()).thenReturn(console);
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
