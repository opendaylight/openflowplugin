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
import java.util.concurrent.atomic.AtomicLong;
import javax.management.AttributeChangeNotification;
import javax.management.NotificationBroadcasterSupport;

public class NodeReconciliationAlarm extends NotificationBroadcasterSupport implements NodeReconciliationAlarmMBean {

    private AtomicLong sequenceNumber = new AtomicLong(1);

    private volatile java.util.List<String> raiseAlarmObject = new ArrayList<>();
    private volatile List<String> clearAlarmObject = new ArrayList<>();

    @Override
    public void setRaiseAlarmObject(final List<String> raiseAlarmObject) {
        this.raiseAlarmObject = raiseAlarmObject;

        sendRaiseAlarmNotification(this.raiseAlarmObject);
    }

    private void sendRaiseAlarmNotification(final List<String> alarmObject) {
        sendNotification(new AttributeChangeNotification(this, sequenceNumber.getAndIncrement(),
                System.currentTimeMillis(), "raise alarm object notified ", "raiseAlarmObject", "ArrayList", "",
                alarmObject));
    }

    @Override
    public List<String> getRaiseAlarmObject() {
        return raiseAlarmObject;
    }

    @Override
    public void setClearAlarmObject(final List<String> clearAlarmObject) {
        this.clearAlarmObject = clearAlarmObject;
        sendClearAlarmNotification(this.clearAlarmObject);
    }

    private void sendClearAlarmNotification(final List<String> alarmObject) {
        sendNotification(new AttributeChangeNotification(this, sequenceNumber.getAndIncrement(),
                System.currentTimeMillis(), "clear alarm object notified ", "clearAlarmObject", "ArrayList", "",
                alarmObject));
    }

    @Override
    public List<String> getClearAlarmObject() {
        return clearAlarmObject;
    }

    @Override
    public void raiseAlarm(final String alarmName, final String additionalText, final String source) {
        sendRaiseAlarmNotification(ImmutableList.of(alarmName, additionalText, source));
    }

    @Override
    public void clearAlarm(final String alarmName, final String additionalText, final String source) {
        sendClearAlarmNotification(ImmutableList.of(alarmName, additionalText, source));
    }
}
