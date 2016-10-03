/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.converter.action.data;

import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.action.ActionResponseConverter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.common.ConverterData;

/**
 * Convertor data used in {@link ActionResponseConverter}
 * containing Openflow version and {@link org.opendaylight.openflowplugin.extension.api.path.ActionPath}
 */
public class ActionResponseConverterData extends ConverterData {
    private ActionPath actionPath;

    /**
     * Instantiates a new Action response converter data.
     *
     * @param version the version
     */
    public ActionResponseConverterData(short version) {
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
}
