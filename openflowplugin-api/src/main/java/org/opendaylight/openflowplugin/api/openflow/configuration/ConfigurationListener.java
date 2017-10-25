/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.configuration;

import javax.annotation.Nonnull;

/**
 * Listens for changes in OpenFlowPlugin configuration.
 */
public interface ConfigurationListener {

    /**
     * Method invoked on configuration property change.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     */
    void onPropertyChanged(@Nonnull String propertyName, @Nonnull String propertyValue);

}
