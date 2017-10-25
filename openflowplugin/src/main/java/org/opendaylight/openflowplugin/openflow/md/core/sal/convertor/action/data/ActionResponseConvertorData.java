/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data;

import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;

/**
 * Convertor data used in {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor}
 * containing Openflow version and {@link org.opendaylight.openflowplugin.extension.api.path.ActionPath}
 */
public class ActionResponseConvertorData extends ConvertorData {
    private ActionPath actionPath;
    private Short ipProtocol;

    /**
     * Instantiates a new Action response convertor data.
     *
     * @param version the version
     */
    public ActionResponseConvertorData(short version) {
        super(version);
    }

    /**
     * Gets action path.
     *
     * @return the action path
     */
    public ActionPath getActionPath() {
        return actionPath;
    }

    /**
     * Sets action path.
     *
     * @param actionPath the action path
     */
    public void setActionPath(ActionPath actionPath) {
        this.actionPath = actionPath;
    }

    /**
     * Gets ip protocol.
     *
     * @return the ip protocol
     */
    public Short getIpProtocol() {
        return ipProtocol;
    }

    /**
     * Sets ip protocol.
     *
     * @param ipProtocol the ip protocol
     */
    public void setIpProtocol(final Short ipProtocol) {
        this.ipProtocol = ipProtocol;
    }
}
