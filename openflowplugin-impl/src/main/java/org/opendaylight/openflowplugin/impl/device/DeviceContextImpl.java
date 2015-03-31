/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestFutureContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class DeviceContextImpl implements DeviceContext {
    private Map<Xid, RequestFutureContext> requests =
            new HashMap<Xid, RequestFutureContext>();

    private final Map<SwitchConnectionDistinguisher, ConnectionContext> auxiliaryConnectionContexts = new HashMap<>();

    private XidGenerator xidGenerator = new XidGenerator();

    @Override
    public <M extends ChildOf<DataObject>> void onMessage(M message, RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAuxiliaryConenctionContext(ConnectionContext connectionContext) {
        SwitchConnectionDistinguisher connectionDistinguisher = new SwitchConnectionCookieOFImpl(connectionContext.getFeatures().getAuxiliaryId());
        auxiliaryConnectionContexts.put(connectionDistinguisher, connectionContext);
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
    public WriteTransaction getWriteTransaction() {
        return null;
    }

    @Override
    public ReadTransaction getReadTransaction() {
        return null;
    }

    @Override
    public TableFeatures getCapabilities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionContext getPrimaryConnectionContext() {
        return null;
    }

    @Override
    public ConnectionContext getAuxiliaryConnectiobContexts(final BigInteger cookie) {
        return auxiliaryConnectionContexts.get(new SwitchConnectionCookieOFImpl(cookie.longValue()));
    }

    @Override
    public Xid getNextXid() {
        return xidGenerator.generate();
    }

    @Override
    public <T extends DataObject> Future<RpcResult<T>> sendRequest(Xid xid) {
        return null;
    }

    @Override
    public Map<Xid, RequestFutureContext> getRequests() {
        // TODO Auto-generated method stub
        return requests;
    }

    @Override
    public void hookRequestCtx(Xid xid, RequestFutureContext requestFutureContext) {
        // TODO Auto-generated method stub
        requests.put(xid, requestFutureContext);
    }

    @Override
    public void processReply(Xid xid, OfHeader ofHeader) {
        // TODO Auto-generated method stub
        SettableFuture replyFuture = getRequests().get(xid).getFuture();
        replyFuture.set(ofHeader);
    }

    private final class XidGenerator {

        private AtomicLong xid = new AtomicLong(0);

        public Xid generate() {
            return new Xid(xid.incrementAndGet());
        }
    }
}
