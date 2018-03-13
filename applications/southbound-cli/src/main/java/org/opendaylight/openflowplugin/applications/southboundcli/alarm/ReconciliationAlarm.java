/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.alarm;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.management.AttributeChangeNotification;
import javax.management.NotificationBroadcasterSupport;

public class ReconciliationAlarm extends NotificationBroadcasterSupport implements ReconciliationAlarmMBean {

    private long sequenceNumber = 1;

    private volatile java.util.List<String> raiseAlarmObject = new ArrayList<>();
    private volatile List<String> clearAlarmObject = new ArrayList<>();

    @Override
    public void setRaiseAlarmObject(List<String> raiseAlarmObject) {
        this.raiseAlarmObject = raiseAlarmObject;

        sendRaiseAlarmNotification(this.raiseAlarmObject);
    }

    private void sendRaiseAlarmNotification(List<String> alarmObject) {
        sendNotification(new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
                "raise alarm object notified ", "raiseAlarmObject", "ArrayList", "", alarmObject));
    }

    @Override
    public List<String> getRaiseAlarmObject() {
        return raiseAlarmObject;
    }

    @Override
    public void setClearAlarmObject(List<String> clearAlarmObject) {
        this.clearAlarmObject = clearAlarmObject;
        sendClearAlarmNotification(this.clearAlarmObject);
    }

    private void sendClearAlarmNotification(List<String> alarmObject) {
        sendNotification(new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
                "clear alarm object notified ", "clearAlarmObject", "ArrayList", "", alarmObject));
    }

    @Override
    public List<String> getClearAlarmObject() {
        return clearAlarmObject;
    }

    @Override
    public void raiseAlarm(String alarmName, String additionalText, String source) {
        sendRaiseAlarmNotification(ImmutableList.of(alarmName, additionalText, source));
    }

    @Override
    public void clearAlarm(String alarmName, String additionalText, String source) {
        sendClearAlarmNotification(ImmutableList.of(alarmName, additionalText, source));
    }
}
