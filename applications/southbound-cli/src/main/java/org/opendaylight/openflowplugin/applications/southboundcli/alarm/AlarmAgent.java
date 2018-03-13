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
    private MBeanServer mbs = null;
    private ObjectName alarmName = null;
    private static final String BEANNAME = "SDNC.FM:name=FlowResyncOperationOngoing";
    private static ReconciliationAlarm alarmBean = new ReconciliationAlarm();

    /**
     * constructor get the instance of platform MBeanServer.
     */
    public AlarmAgent() {
        mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            alarmName = new ObjectName(BEANNAME);
        } catch (MalformedObjectNameException e) {
            LOG.error("ObjectName instance creation failed for BEANAME {}", BEANNAME, e);
        }
    }

    @PostConstruct
    public void start() throws Exception {
        registerAlarmMbean();
    }

    /**
     * Method registers alarm mbean in platform MbeanServer.
     */
    public void registerAlarmMbean() {
        try {
            if (!mbs.isRegistered(alarmName)) {
                mbs.registerMBean(alarmBean, alarmName);
                LOG.info("Registered Mbean {} successfully", alarmName);
            }
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            LOG.error("Registeration failed for Mbean {}", alarmName, e);
        }
    }

    /**
     * Method invoke raise alarm JMX API in platform MbeanServer with alarm
     * details.
     *
     * @param alarmId
     *            alarm to be raised
     * @param text
     *            Additional details describing about the alarm on which dpnId
     *            and hostname
     * @param src
     *            Source of the alarm ex: dpnId=openflow:1 the source node that
     *            caused this alarm
     */
    public void invokeFMraisemethod(String alarmId, String text, String src) {
        try {
            mbs.invoke(alarmName, "raiseAlarm", new Object[] { alarmId, text, src },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName() });
            LOG.debug("Invoked raiseAlarm function for Mbean {} with source {}", BEANNAME, src);
        } catch (InstanceNotFoundException | ReflectionException | MBeanException e) {
            LOG.error("Invoking raiseAlarm method failed for Mbean {}", alarmName, e);
        }
    }

    /**
     * Method invoke clear alarm JMX API in platform MbeanServer with alarm
     * details.
     *
     * @param alarmId
     *            alarm to be cleared
     * @param text
     *            Additional details describing about the alarm on which dpnId
     *            and hostname
     * @param src
     *            Source of the alarm ex: dpn=openflow:1 the source node that
     *            caused this alarm
     */
    public void invokeFMclearmethod(String alarmId, String text, String src) {
        try {
            mbs.invoke(alarmName, "clearAlarm", new Object[] { alarmId, text, src },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName() });
            LOG.debug("Invoked clearAlarm function for Mbean {} with source {}", BEANNAME, src);
        } catch (InstanceNotFoundException | ReflectionException | MBeanException e) {
            LOG.error("Invoking clearAlarm method failed for Mbean {}", alarmName, e);
        }
    }

    /**
     * Method gets the alarm details to be raised and construct the alarm
     * objects.
     *
     * @param nodeId
     *            Source of the alarm dpnId
     * @param host
     *            Controller hostname
     */
    public void raiseAdminReconciliationAlarm(String nodeId, String host) {
        String alarmText;
        StringBuilder source = new StringBuilder();

        if (host != null) {
            alarmText = getAlarmText(nodeId, host);
            source.append("Dpn=").append(nodeId);

            LOG.debug("Raising AdminReconciliation alarm... alarmText {} source {} ", alarmText, source);
            // Invokes JMX raiseAlarm method
            invokeFMraisemethod("AdminReconciliation", alarmText, source.toString());
        } else {
            LOG.error("Received hostname is null");
        }
    }

    /**
     * Method gets the alarm details to be cleared and construct the alarm
     * objects.
     *
     * @param nodeId
     *            Source of the alarm dpnId
     */
    public void clearAdminReconciliationAlarm(String nodeId, String host) {
        StringBuilder source = new StringBuilder();
        String alarmText;

        if (host != null) {
            alarmText = getAlarmText(nodeId, host);
            source.append("Dpn=").append(nodeId);
            LOG.debug("Clearing AdminReconciliation alarm of source {} ", source);
            // Invokes JMX clearAlarm method
            invokeFMclearmethod("AdminReconciliationAlarm", alarmText, source.toString());
        } else {
            LOG.error("Received hostname is null");
        }
    }

    private String getAlarmText(String nodeId, String host) {
        return "OF Switch " + nodeId + " started reconciliation with controller " + host;
    }
}
