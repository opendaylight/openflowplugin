/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
/**
 * Utility class for comparing flows.
 */
public final class FlowComparator {

    private FlowComparator() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    private static final Collection<SimpleComparator<Flow>> FLOW_COMPARATORS = new ArrayList<>();
    static {
        FLOW_COMPARATORS.add(FlowComparatorFactory.createPriority());
        FLOW_COMPARATORS.add(FlowComparatorFactory.createTableId());
        FLOW_COMPARATORS.add(FlowComparatorFactory.createContainerName());
        FLOW_COMPARATORS.add(FlowComparatorFactory.createCookie());
        FLOW_COMPARATORS.add(FlowComparatorFactory.createMatch());
    }

    public static boolean flowEquals(final Flow statsFlow, final Flow storedFlow) {
        if (statsFlow == null || storedFlow == null) {
            return false;
        }

        for (SimpleComparator<Flow> flowComp : FLOW_COMPARATORS) {
            if (!flowComp.areObjectsEqual(statsFlow, storedFlow)) {
                return false;
            }
        }

        return true;
    }
}
