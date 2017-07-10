/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.util;

import java.util.HashMap;
import java.util.Map;

public class ReconciliationUtil {
    private ReconciliationUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static <E> E decideResultState(Iterable<E> iterable) {
        Map<E, Integer> freqMap = new HashMap<>();
        E mostFreq = null;
        int mostFreqCount = -1;
        for (E e : iterable) {
            Integer count = freqMap.get(e);
            freqMap.put(e, count = (count == null ? 1 : count+1));
            if (count > mostFreqCount) {
                mostFreq = e;
                mostFreqCount = count;
            }
        }
        return mostFreq;
    }
}
