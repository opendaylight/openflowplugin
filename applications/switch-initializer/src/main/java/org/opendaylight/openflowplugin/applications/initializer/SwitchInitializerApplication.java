/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.initializer;

import org.opendaylight.openflowplugin.api.openflow.device.initialization.SwitchInitializer;

/**
 * Interface to application.
 */
public interface SwitchInitializerApplication extends SwitchInitializer {

    @Override
    void close();
}
