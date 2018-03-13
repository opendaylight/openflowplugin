/**
 * This is the interface for the Reconciliation Alarm MBean. It basically allows for
 * raising and clearing alarms.
 *
 * @author Ericsson India Global Services Pvt Ltd. and others
 *
 */

package org.opendaylight.openflowplugin.applications.southboundcli.alarm;

import java.util.List;

public interface ReconciliationAlarmMBean {

    void setRaiseAlarmObject(java.util.List<String> raiseAlarmObject);

    List<String> getRaiseAlarmObject();

    void setClearAlarmObject(List<String> clearAlarmObject);

    List<String> getClearAlarmObject();

    void raiseAlarm(String alarmName, String additionalText, String source);

    void clearAlarm(String alarmName, String additionalText, String source);
}
