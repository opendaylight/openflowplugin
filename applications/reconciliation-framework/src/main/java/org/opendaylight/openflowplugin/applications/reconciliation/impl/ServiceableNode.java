package org.opendaylight.openflowplugin.applications.reconciliation.impl;

/**
 * Created by eknnosd on 5/24/2017.
 */
public class ServiceableNode {
    Object service;
    String serviceName;

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public ServiceableNode(String serviceName, Object service) {

        this.service = service;
        this.serviceName = serviceName;
    }
}
