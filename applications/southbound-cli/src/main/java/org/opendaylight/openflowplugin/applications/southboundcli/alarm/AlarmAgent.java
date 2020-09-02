/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.alarm;

import java.lang.management.ManagementFactory;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlarmAgent {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmAgent.class);
    private static final String BEAN_NAME = "SDNC.FM:name=NodeReconciliationOperationOngoingBean";
    private final MBeanServer mbs;
    private final NodeReconciliationAlarm alarmBean = new NodeReconciliationAlarm();
    private ObjectName alarmName;

    /**
     * constructor get the instance of platform MBeanServer.
     */
    public AlarmAgent() {
        mbs = ManagementFactory.getPlatformMBeanServer();
    }

    @PostConstruct
    public void start() {
        try {
            alarmName = new ObjectName(BEAN_NAME);
            if (!mbs.isRegistered(alarmName)) {
                mbs.registerMBean(alarmBean, alarmName);
                LOG.info("Registered Mbean {} successfully", alarmName);
            }
        } catch (MalformedObjectNameException e) {
            LOG.error("ObjectName instance creation failed for bean {}", BEAN_NAME, e);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            LOG.error("Registeration failed for Mbean {}", alarmName, e);
        }
    }

    /**
     * Method invoke raise alarm JMX API in platform MbeanServer with alarm
     * details.
     *
     * @param alarmId alarm to be raised
     * @param text Additional details describing about the alarm on which dpnId
     * @param src Source of the alarm ex: dpnId=openflow:1 the source node that
     *            caused this alarm
     */
    public void invokeFMRaiseMethod(final String alarmId, final String text, final String src) {
        try {
            mbs.invoke(alarmName, "raiseAlarm", new Object[] { alarmId, text, src },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName() });
            LOG.debug("Invoked raiseAlarm function for Mbean {} with source {}", BEAN_NAME, src);
        } catch (InstanceNotFoundException | ReflectionException | MBeanException e) {
            LOG.error("Invoking raiseAlarm function failed for Mbean {}", alarmName, e);
        }
    }

    /**
     * Method invoke clear alarm JMX API in platform MbeanServer with alarm
     * details.
     *
     * @param alarmId alarm to be cleared
     * @param text Additional details describing about the alarm on which dpnId
     * @param src Source of the alarm ex: dpn=openflow:1 the source node that
     *            caused this alarm
     */
    public void invokeFMClearMethod(final String alarmId, final String text, final String src) {
        try {
            mbs.invoke(alarmName, "clearAlarm", new Object[] { alarmId, text, src },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName() });
            LOG.debug("Invoked clearAlarm function for Mbean {} with source {}", BEAN_NAME, src);
        } catch (InstanceNotFoundException | ReflectionException | MBeanException e) {
            LOG.error("Invoking clearAlarm method failed for Mbean {}", alarmName, e);
        }
    }

    /**
     * Method gets the alarm details to be raised and construct the alarm
     * objects.
     *
     * @param nodeId Source of the alarm dpnId
     */
    public void raiseNodeReconciliationAlarm(final Long nodeId) {
        String alarmText = getAlarmText(nodeId,  " started reconciliation");
        String source = getSourceText(nodeId);

        LOG.debug("Raising NodeReconciliationOperationOngoing alarm, alarmText {} source {}", alarmText, source);
        // Invokes JMX raiseAlarm method
        invokeFMRaiseMethod("NodeReconciliationOperationOngoing", alarmText, source);
    }

    /**
     * Method gets the alarm details to be cleared and construct the alarm
     * objects.
     *
     * @param nodeId Source of the alarm dpnId
     */
    public void clearNodeReconciliationAlarm(final Long nodeId) {
        String alarmText = getAlarmText(nodeId, " finished reconciliation");
        String source = getSourceText(nodeId);

        LOG.debug("Clearing NodeReconciliationOperationOngoing alarm of source {}", source);
        // Invokes JMX clearAlarm method
        invokeFMClearMethod("NodeReconciliationOperationOngoing", alarmText, source);
    }

    /**
     * Method gets the alarm text for the nodeId.
     *
     * @param nodeId Source of the alarm nodeId
     * @param event reason for alarm invoke/clear
     */
    private String getAlarmText(final Long nodeId, final String event) {
        return new StringBuilder("OF Switch ").append(nodeId).append(event).toString();
    }

    /**
     * Method gets the source text for the nodeId.
     *
     * @param nodeId Source of the alarm nodeId
     */
    private String getSourceText(final Long nodeId) {
        return "Dpn=" + nodeId;
    }
}
