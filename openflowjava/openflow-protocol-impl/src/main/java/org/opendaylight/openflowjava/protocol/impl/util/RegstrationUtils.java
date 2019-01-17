/*
 * Copyright (c) 2019 Ericsson Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import java.util.List;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegstrationUtils {
    protected DiagStatusService diagStatusService;
    private static final Logger LOG = LoggerFactory.getLogger(RegstrationUtils.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";
    private static final String OPENFLOW_SERVICE_6633 = "OPENFLOW6633";
    private static final String OPENFLOW_SERVICE_66 = "OPENFLOW66";
    private static final String SIZE = "The number of services are not 2";

    public RegstrationUtils(DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public String operational(List<Boolean> result) {
        String check = null;
        try {
            if ((result.size() == 2)  && (result.get(0).booleanValue() == true)
                    && (result.get(1).booleanValue() == true)) {
                check = OPENFLOW_SERVICE_NAME;
            }
            else if ((result.size() == 2) && (result.get(0).booleanValue()  == false)
                    && (result.get(1).booleanValue() == true)) {
                LOG.error("Error:The service OPENFLOW6633 is not up");
                check = OPENFLOW_SERVICE_6633;
            }
            else if ((result.size() == 2) && (result.get(0).booleanValue() == true)
                    && (result.get(1).booleanValue() == false)) {
                LOG.error("Error:The service OPENFLOW6633 is not up");
                check = OPENFLOW_SERVICE_66;
            }
            else if (result.size() != 2) {
                LOG.error("The portsize is not 2");
                check = SIZE;
            }
        }
        catch (Throwable throwable) {
            LOG.error("The error is ");
            diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, throwable));
            check = throwable.toString();
        } return check;
    }
}