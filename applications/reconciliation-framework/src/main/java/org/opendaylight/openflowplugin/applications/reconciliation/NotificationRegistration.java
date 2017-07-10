/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation;

/*
 * API to return to the service after the registration of the service is completed
 * which could be used by the service to unregister itself
 */
public interface NotificationRegistration extends  AutoCloseable{
}
