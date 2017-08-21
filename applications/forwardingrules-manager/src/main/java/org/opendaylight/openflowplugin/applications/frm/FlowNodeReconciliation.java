/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;

/**
 * Implementation provider of this interface will implement tasks functionality for a newly connected node.
 * Implementation is not enforced to do tasks in any specific way, but the higher level intention is to
 * provide best effort tasks of  all the configuration (flow/meter/group) present in configuration data store
 * for the given node.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 */
public interface FlowNodeReconciliation extends ReconciliationNotificationListener, AutoCloseable {
}