/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;

/**
 * Provider to register mastership change listener.
 */
public interface MastershipChangeServiceManager extends MastershipChangeListener {

    @Nonnull
    MastershipChangeRegistration register(@Nonnull final MastershipChangeService service);

    void unregister(@Nonnull final MastershipChangeService service);

}
