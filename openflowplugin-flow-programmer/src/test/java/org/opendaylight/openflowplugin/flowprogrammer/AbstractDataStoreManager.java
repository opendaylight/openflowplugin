/*
 * Copyright (c) 2015 Intel, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer;

import java.util.concurrent.ExecutorService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.openflowplugin.flowprogrammer.FlowProgrammerImpl;

/**
 * This class contains auxiliary methods to manage abstract data store
 *
 * @author Yi Yang <yi.y.yang@intel.com>
 * @since 2015-09-08
 */

/*
 * The purpose of this class is to get DataBroker used in tests
 */
public abstract class AbstractDataStoreManager extends AbstractDataBrokerTest {
    private static boolean executorSet = false;
    protected DataBroker dataBroker;
    protected static ExecutorService executor;
    protected final FlowProgrammerImpl flowProgrammerImpl = new FlowProgrammerImpl();

    /* Initialize Data store broker, executor is set only once, new data
     * broker is created before every set, it ensures empty data store.
     */
    protected void setFlowProgrammerImpl() {
        if (!executorSet) {
            executor = flowProgrammerImpl.getExecutor();
            executorSet = true;
        }
        dataBroker = getDataBroker();
        flowProgrammerImpl.setDataProvider(dataBroker);
    }
}
