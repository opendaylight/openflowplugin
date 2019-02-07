/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import java.lang.management.ManagementFactory;
import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReconciliationJMXAgent {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationJMXAgent.class);
    private static final String OF_RECONC_BEANNAME
            = "com.ericsson.sdncp.services.openflowplugin.frm:type=ReconciliationState";
    private MBeanServer mbs = null;
    private ObjectName objectName = null;

    @Inject
    public ReconciliationJMXAgent(final ReconciliationJMXService reconciliationJMXService) {
        mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            objectName = new ObjectName(OF_RECONC_BEANNAME);
            registerReconciliationMbean(reconciliationJMXService);
        } catch (MalformedObjectNameException e) {
            LOG.error("ObjectName instance creation failed for BEANAME {} : {}", OF_RECONC_BEANNAME, e);
        }
    }

    public void registerReconciliationMbean(ReconciliationJMXService reconciliationJMXService) {
        try {
            // Uniquely identify the MBeans and register them with the platform MBeanServer
            if (!mbs.isRegistered(objectName)) {
                mbs.registerMBean(reconciliationJMXService, objectName);
                LOG.debug("Registered Mbean {} successfully", OF_RECONC_BEANNAME);
            }
        } catch (MBeanRegistrationException | InstanceAlreadyExistsException | NotCompliantMBeanException e) {
            LOG.error("Registeration failed for Mbean {} :{}", OF_RECONC_BEANNAME , e);
        }
    }
}
