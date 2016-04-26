/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync;

import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Represents configuration item contract for device.
 * Combining add/update/remove commiters into single one.
 */
public interface ForwardingRulesCommitter<D extends DataObject, A extends DataObject, R extends DataObject, U extends DataObject>
        extends ForwardingRulesAddCommitter<D, A>, ForwardingRulesRemoveCommitter<D, R>, ForwardingRulesUpdateCommitter<D, U> {
}

