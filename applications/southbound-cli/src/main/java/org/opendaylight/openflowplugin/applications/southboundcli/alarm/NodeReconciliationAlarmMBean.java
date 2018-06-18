/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.alarm;

import java.util.List;

/**
 * NodeReconciliationAlarmMBean is the generic interface for
 * providing alarm services by maintaning the alarm object.
 */
public interface NodeReconciliationAlarmMBean {

    /**
     * This method is called to set the list of raiseAlarmObjets.
     * This includes the alarm objects which are to be raised
     *
     * @param raiseAlarmObject holds the list of alarm objects
     */
    void setRaiseAlarmObject(List<String> raiseAlarmObject);

    /**
     * This method is called to retrieve the list of alarm objects which are to be raised.
     *
     * @return List of string containing the alarm objects
     */
    List<String> getRaiseAlarmObject();

    /**
     * This method is called to set the list of alarm objects to be cleared.
     *
     * @param clearAlarmObject maintains the list of clearable alarm objects
     */
    void setClearAlarmObject(List<String> clearAlarmObject);

    /**
     * This method is called to retrieve the list of the alarm objects to be cleared.
     *
     * @return List of clearable alarm objects
     */
    List<String> getClearAlarmObject();

    /**
     * This method is called to raise the alarm with the specified alarm name.
     *
     * @param alarmName name of the alarm to be raised
     * @param additionalText description of alarm event
     * @param source source of alarm
     */
    void raiseAlarm(String alarmName, String additionalText, String source);

    /**
     * This method is called to clear the raised alarm with the specified alarm name.
     *
     * @param alarmName name of the alarm to be cleared
     * @param additionalText description of alarm event
     * @param source source of alarm
     */
    void clearAlarm(String alarmName, String additionalText, String source);
}