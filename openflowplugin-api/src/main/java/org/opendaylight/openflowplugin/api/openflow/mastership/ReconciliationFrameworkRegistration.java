/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

/**
 * API provided for reconciliation framework registration.
 * Service provides three events.
 * <ul>
 *     <li><i>onDevicePrepared</i>
 *     <li><i>onDeviceDisconnected</i>
 * </ul>
 * @see ReconciliationFrameworkEvent
 * @since 0.5.0 Nitrogen
 */
public interface ReconciliationFrameworkRegistration extends AutoCloseable {
}
