/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.reference.app.impl.RefAppServiceImpl;
import org.opendaylight.reference.app.openflow.FlowProgrammer;
import org.opendaylight.reference.app.openflow.NodeEventListener;
import org.opendaylight.reference.app.reconciliation.TaskFactory;
import org.opendaylight.reference.app.subscriber.SubscriberHandler;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.RefAppModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;


/**
 * Reference app class which handles initialization of SubscriberHandler, NodeEventListener class
 * and registeration of ReferenceAppMXBean to expose counter using JMX
 */
public class ReferenceApp implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceApp.class);
    private static final String factoryType = "RESYNC";
    private final BindingAwareBroker.RpcRegistration<RefAppModelService> serviceRpcRegistration;

    public ReferenceApp(RpcProviderRegistry rpcProviderRegistry, DataBroker dataBroker,
                        IOpenflowFacade openflowFacade, IPriorityTaskManager priorityTaskManager) {
        LOG.info("starting reference app");
        FlowProgrammer flowProgrammer = new FlowProgrammer(openflowFacade);
        SubscriberHandler subscriberHandler = new SubscriberHandler(dataBroker, flowProgrammer);
        subscriberHandler.register();
        NodeEventListener nodeEventListener = new NodeEventListener(dataBroker);
        nodeEventListener.register();
        TaskFactory factory = new TaskFactory(flowProgrammer);
        priorityTaskManager.registerPriorityBasedTaskFactory(factory, factoryType);
        registerMXBean(flowProgrammer);
        RefAppModelService service = new RefAppServiceImpl(dataBroker);
        serviceRpcRegistration = rpcProviderRegistry.addRpcImplementation(RefAppModelService.class, service);
    }

    @Override
    public void close() {
        LOG.info("destroying reference-app");
        serviceRpcRegistration.close();
    }

    private void registerMXBean(FlowProgrammer referenceAppMXBean) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            String pathToMBean = String.format("%s:type=%s",
                    ReferenceAppMXBean.class.getPackage().getName(),
                    "ReferenceAppCounter");
            ObjectName name = new ObjectName(pathToMBean);
            mBeanServer.registerMBean(referenceAppMXBean, name);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException
                | MBeanRegistrationException | NotCompliantMBeanException e) {
            LOG.error("Error while registering for MBean of reference-app", e);
        }
    }

}