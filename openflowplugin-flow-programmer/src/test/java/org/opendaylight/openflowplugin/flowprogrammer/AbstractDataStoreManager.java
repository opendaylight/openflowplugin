/*
 * Copyright (c) 2015 Intel, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.openflowplugin.flowprogrammer.FlowProgrammerImpl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;

import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;

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

    @Override
    protected Iterable<YangModuleInfo> getModuleInfos() throws Exception {
        Builder<YangModuleInfo> moduleInfoSet = ImmutableSet.<YangModuleInfo>builder();
        loadModuleInfos(Nodes.class, moduleInfoSet);
        loadModuleInfos(FlowCapableNode.class, moduleInfoSet);
        return moduleInfoSet.build();
    }

    public static void loadModuleInfos(Class<?> clazzFromModule, Builder<YangModuleInfo> moduleInfoSet) throws Exception {
        YangModuleInfo moduleInfo = BindingReflections.getModuleInfo(clazzFromModule);
        checkState(moduleInfo != null, "Module Info for %s is not available.", clazzFromModule);
        collectYangModuleInfo(moduleInfo, moduleInfoSet);
    }

    private static void collectYangModuleInfo(final YangModuleInfo moduleInfo,
            final Builder<YangModuleInfo> moduleInfoSet) throws IOException {
        moduleInfoSet.add(moduleInfo);
        for (YangModuleInfo dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
    }

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
