/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import java.lang.management.ManagementFactory;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class ReconciliationJMXAgent implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationJMXAgent.class);
    private static final ObjectName OF_RECONF_OBJNAME;

    static {
        try {
            OF_RECONF_OBJNAME = new ObjectName("org.opendaylight.openflowplugin.frm:type=ReconciliationState");
        } catch (MalformedObjectNameException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private ObjectInstance objInstance;

    @Inject
    @Activate
    public ReconciliationJMXAgent(final ReconciliationJMXServiceMBean reconciliationJMXService) {
        // Uniquely identify the MBeans and register them with the platform MBeanServer
        ObjectInstance inst = null;
        if (!mbs.isRegistered(OF_RECONF_OBJNAME)) {
            try {
                inst = mbs.registerMBean(reconciliationJMXService, OF_RECONF_OBJNAME);
                LOG.debug("Registered Mbean {} successfully", OF_RECONF_OBJNAME);
            } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                LOG.warn("Registeration failed for Mbean {} : ", OF_RECONF_OBJNAME , e);
            }
        }
        objInstance = inst;
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        if (objInstance != null) {
            objInstance = null;
            try {
                mbs.unregisterMBean(OF_RECONF_OBJNAME);
            } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                LOG.warn("Unregistration failed for Mbean {} : ", OF_RECONF_OBJNAME , e);
            }
        }
    }
}
