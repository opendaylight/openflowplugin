/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.util;

import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class OfEventLogUtil {
    private static String loggerName;

    public OfEventLogUtil(ConfigurationService config) {
        setLoggerName(config);
    }

    private static void setLoggerName(ConfigurationService config) {
        loggerName = config.getProperty(ConfigurationProperty.OF_EVENT_LOGGER_NAME.toString(),
                String::valueOf);
    }

    public static String getLoggerName() {
        return loggerName;
    }
}