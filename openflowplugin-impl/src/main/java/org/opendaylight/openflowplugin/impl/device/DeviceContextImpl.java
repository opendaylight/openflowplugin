/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * 
 */
public class DeviceContextImpl implements DeviceContext {

    @Override
    public <M extends ChildOf<DataObject>> void onMessage(M message, RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAuxiliaryConenctionContext(ConnectionContext connectionContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAuxiliaryConenctionContext(ConnectionContext connectionContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public DeviceState getDeviceState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTransactionChain(TransactionChain transactionChain) {
        // TODO Auto-generated method stub

    }

    @Override
    public TransactionChain getTransactionChain() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TableFeatures getCapabilities() {
        // TODO Auto-generated method stub
        return null;
    }

}
