/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira;

import org.opendaylight.openflowplugin.extension.api.MessageConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterAction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * 
 */
public class NxResubmitActionConvertor implements MessageConvertor<Action, ExperimenterAction> {

    @Override
    public ExperimenterAction convert(Action input,
            InstanceIdentifier<DataObject> path) {
        // TODO Auto-generated method stub
        return null;
    }

}
