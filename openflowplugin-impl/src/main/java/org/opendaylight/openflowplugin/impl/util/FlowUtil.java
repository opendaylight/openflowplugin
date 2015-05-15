/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public final class FlowUtil {

    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger unaccountedFlowsCounter = new AtomicInteger(0);
    private static final Logger LOG = LoggerFactory.getLogger(FlowUtil.class);


    private FlowUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static FlowId createAlienFlowId(final short tableId) {
        final StringBuilder sBuilder = new StringBuilder(ALIEN_SYSTEM_FLOW_ID)
                .append(tableId).append('-').append(unaccountedFlowsCounter.incrementAndGet());
        String alienId =  sBuilder.toString();
        return new FlowId(alienId);

    }
}
