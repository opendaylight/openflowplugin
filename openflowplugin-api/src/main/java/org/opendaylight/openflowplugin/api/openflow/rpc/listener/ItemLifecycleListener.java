/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.rpc.listener;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Flow/group/meter lifecycle listener - aimed on rpc result approved by barrier message.
 */
public interface ItemLifecycleListener {

    /**
     * react upon item added event
     *
     * @param itemPath keyed path in DS
     * @param itemBody item body
     */

    <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onAdded(KeyedInstanceIdentifier<I, K> itemPath, I itemBody);

    /**
     * react upon item removed event
     *
     * @param itemPath keyed path in DS
     */
    <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onRemoved(KeyedInstanceIdentifier<I, K> itemPath);

    /**
     * react upon item updated event
     *
     * @param itemPath keyed path in DS
     * @param itemBody item body
     */

    <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onUpdated(KeyedInstanceIdentifier<I, K> itemPath, I itemBody);
}
