package org.opendaylight.openflowplugin.service;


import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.of.operational.status.rev141128.OperStatus;

/**
 * Created by Martin Bobak mbobak@cisco.com on 11/28/14.
 */
public class OfOperationalStatusHolder {

    private static OperStatus operationalStatus = OperStatus.RUN;

    public static OperStatus getOperationalStatus() {
        return OfOperationalStatusHolder.operationalStatus;
    }

    public static void setOperationalStatus(OperStatus operationalStatus) {
        OfOperationalStatusHolder.operationalStatus = operationalStatus;
    }
}
