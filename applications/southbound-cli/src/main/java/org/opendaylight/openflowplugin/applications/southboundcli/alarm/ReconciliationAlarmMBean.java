/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
