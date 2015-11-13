/*
 * Copyright (c) 2015 Intel Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.jbench;

/**
 * <p>This class provides methods to get and set various parameters of the SdnController.</p>
 * @author Raksha Madhava Bangera
 *
 */
public class SdnController {

    private final int defaultOpenflowPort = 6633;
    private String hostName;
    private Integer openFlowPort;

    /**
     * Empty constructor of SdnController
     */
    public SdnController() {

    }
    /**
     * Constructor of SdnController
     * @param host host-name of the Controller
     * @param port port-number of the Controller to which the fake-switch should connect to
     */
    public SdnController(String host, Integer port) {
        this.hostName = host;
        this.openFlowPort = port;
    }

    /**
     * This methods sets the host-name of the controller
     *
     * @param host host-name of the Controller
     */
    public void setHost(String host) {
        hostName = host;
    }

    /**
     * This method sets the port number of the controller to which the fake-switch connects to
     *
     * @param portNum port-number of the Controller
     */
    public void setPort(Integer portNum) {
        openFlowPort = portNum;
    }

    /**
     * This method returns the host-name of the controller
     *
     * @return host-name of the controller
     */
    public String getHost() {
        return hostName;
    }

    /**
     * This method returns the port-number of the controller to which the fake-switch connects to
     *
     * @return port-number of the controller
     */
    public Integer getPort() {
        return openFlowPort;
    }

    /**
     * This method splits the string of the format IP:port into separate IP and port values
     *
     * @param controllerIpPort String of the format IP:port
     */
    void extractIpAndPort(String controllerIpPort) {
        String[] controllerTuples = controllerIpPort.split(":");
        if (controllerTuples.length >= 1 && controllerTuples[0] != null && controllerTuples[0].length() > 1) {
            this.setHost(controllerTuples[0]);
        } else {
            this.setHost("localhost");
        }
        if (controllerTuples.length > 1 && controllerTuples[1] != null) {
            this.setPort(Integer.parseInt(controllerTuples[1]));
        } else {
            this.setPort(defaultOpenflowPort);
        }
    }
}
