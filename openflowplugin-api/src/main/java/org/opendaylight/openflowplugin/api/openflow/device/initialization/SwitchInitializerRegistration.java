/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device.initialization;

/**
 * API provided for additional switch initialization.
 * Service provides one event.
 * <ul>
 *     <li><i>onDevicePrepared</i>
 * </ul>
 * @see SwitchInitializer
 * @since 0.5.0 Nitrogen
 */
public interface SwitchInitializerRegistration extends AutoCloseable {
}
