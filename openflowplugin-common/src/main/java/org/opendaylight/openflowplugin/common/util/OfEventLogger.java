/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OfEventLogger {
    private static final Logger LOG = LoggerFactory.getLogger(OfEventLogger.class);

    private OfEventLogger() {

    }

    public static void logEvent(Class loggerClass, String event, String object) {
        LOG.info("Class {}, Event {} {}", loggerClass.getSimpleName(), event, object);
    }
}