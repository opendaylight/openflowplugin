/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.controller.sal.binding.api.data.DataChangeListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * 
 */
public interface DataChangeListenerRegistrationPublisher {

    /**
     * @return the dataChangeListenerRegistration
     */
    public abstract ListenerRegistration<DataChangeListener> getDataChangeListenerRegistration();

}
