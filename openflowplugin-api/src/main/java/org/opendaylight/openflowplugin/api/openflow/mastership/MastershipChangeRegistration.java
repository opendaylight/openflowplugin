/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

/**
 * API provided for all OFP application to use.
 * This is registration of service for mastership changes.
 * Service provides three events.
 * <ul>
 *     <li><i>onBecomeOwner</i>
 *     <li><i>onDevicePrepared</i>
 *     <li><i>onLoseOwnership</i>
 * </ul>
 * @see MastershipChangeService
 * @since 0.5.0 Nitrogen
 */
public interface MastershipChangeRegistration extends AutoCloseable {
}
