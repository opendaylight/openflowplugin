/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Holder for {@link DataTreeChangeListener} registration.
 */
public interface DataTreeChangeListenerRegistrationHolder {

    /**
     * Returns the DataTreeChangeListenerRegistration.
     */
    ListenerRegistration<DataTreeChangeListener> getDataTreeChangeListenerRegistration();

}
