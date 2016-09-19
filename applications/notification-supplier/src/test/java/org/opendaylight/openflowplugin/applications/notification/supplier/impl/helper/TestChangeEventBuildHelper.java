/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper;

import java.util.Collection;
import java.util.Collections;

public class TestChangeEventBuildHelper {

    private TestChangeEventBuildHelper() {
        throw new UnsupportedOperationException("Test utility class");
    }

    public static Collection createEmptyTestDataTreeEvent(){
        return  Collections.EMPTY_LIST ;
    }

    public static Collection createNullTestDataTreeEvent(){
        return null;
    }

}

