/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * forwardingrules-manager
 * org.opendaylight.openflowplugin.applications.frm
 *
 * ForwardingRulesCommiter
 * It represent a contract between DataStore DataChangeEvent and relevant
 * SalRpcService for device. Every implementation has to be registered for
 * Configurational/DS tree path.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Aug 25, 2014
 */
public interface ForwardingRulesCommiter<D extends DataObject, A extends DataObject, R extends DataObject, U extends DataObject>
        extends AutoCloseable, DataTreeChangeListener<D>,
        ForwardingRulesAddCommiter<D, A>, ForwardingRulesRemoveCommiter<D, R>, ForwardingRulesUpdateCommiter<D, U> {
}

