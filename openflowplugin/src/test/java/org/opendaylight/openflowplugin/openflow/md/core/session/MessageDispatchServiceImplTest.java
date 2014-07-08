/**
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Future;

/**
 * test for {@link MessageDispatchServiceImpl}
 */
public class MessageDispatchServiceImplTest {

    MockSessionContext session;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        session = new MockSessionContext(0);

    }

    /**
     * Test barrier message for null cookie
     *
     * @throws Exception
     */
    @Test
    public void testBarrierMessageForPrimary() throws Exception {
        MockConnectionConductor conductor = new MockConnectionConductor(1);
        SwitchConnectionDistinguisher cookie = conductor.getAuxiliaryKey();
        BarrierInputBuilder barrierMsg = new BarrierInputBuilder();
        session.getMessageDispatchService().barrier(barrierMsg.build(), cookie);
        Assert.assertEquals(MessageType.BARRIER, session.getPrimaryConductor().getMessageType());
    }

    /**
     * Test packet out message for primary connection
     *
     * @throws Exception
     */
    @Test
    public void testPacketOutMessageForPrimary() throws Exception {
        session.getMessageDispatchService().packetOut(null, null);
        Assert.assertEquals(MessageType.PACKETOUT, session.getPrimaryConductor().getMessageType());
    }

    /**
     * Test packet out message for auxiliary connection
     *
     * @throws Exception
     */
    @Test
    public void testPacketOutMessageForAuxiliary() throws Exception {
        MockConnectionConductor conductor = new MockConnectionConductor(1);
        SwitchConnectionDistinguisher cookie = conductor.getAuxiliaryKey();
        session.addAuxiliaryConductor(cookie, conductor);
        session.getMessageDispatchService().packetOut(null, cookie);
        Assert.assertEquals(MessageType.NONE, session.getPrimaryConductor().getMessageType());
        conductor = (MockConnectionConductor) session.getAuxiliaryConductor(cookie);
        Assert.assertEquals(MessageType.PACKETOUT, conductor.getMessageType());
    }

    /**
     * Test packet out message when multiple auxiliary connection exist
     *
     * @throws Exception
     */
    @Test
    public void testPacketOutMessageForMultipleAuxiliary() throws Exception {
        MockConnectionConductor conductor1 = new MockConnectionConductor(1);
        SwitchConnectionDistinguisher cookie1 = conductor1.getAuxiliaryKey();
        session.addAuxiliaryConductor(cookie1, conductor1);
        MockConnectionConductor conductor2 = new MockConnectionConductor(2);
        SwitchConnectionDistinguisher cookie2 = conductor2.getAuxiliaryKey();
        session.addAuxiliaryConductor(cookie2, conductor2);
        MockConnectionConductor conductor3 = new MockConnectionConductor(3);
        SwitchConnectionDistinguisher cookie3 = conductor3.getAuxiliaryKey();
        session.addAuxiliaryConductor(cookie3, conductor3);
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        // send message
        session.getMessageDispatchService().packetOut(builder.build(), cookie2);

        Assert.assertEquals(MessageType.NONE, session.getPrimaryConductor().getMessageType());

        conductor3 = (MockConnectionConductor) session.getAuxiliaryConductor(cookie3);
        Assert.assertEquals(MessageType.NONE, conductor3.getMessageType());

        conductor2 = (MockConnectionConductor) session.getAuxiliaryConductor(cookie2);
        Assert.assertEquals(MessageType.PACKETOUT, conductor2.getMessageType());

        conductor1 = (MockConnectionConductor) session.getAuxiliaryConductor(cookie1);
        Assert.assertEquals(MessageType.NONE, conductor1.getMessageType());

    }

    /**
     * Test for invalid session
     *
     * @throws Exception
     */
    @Test
    public void testInvalidSession() throws Exception {
        session.setValid(false);
        Future<RpcResult<Void>> resultFuture = session.getMessageDispatchService().packetOut(null, null);
        if (resultFuture.isDone()) {
            RpcResult<Void> rpcResult = resultFuture.get();
            Assert.assertTrue(!rpcResult.getErrors().isEmpty());

            Iterator<RpcError> it = rpcResult.getErrors().iterator();
            RpcError rpcError = it.next();

            Assert.assertTrue(rpcError.getApplicationTag().equals(OFConstants.APPLICATION_TAG));
            Assert.assertTrue(rpcError.getTag().equals(OFConstants.ERROR_TAG_TIMEOUT));
            Assert.assertTrue(rpcError.getErrorType().equals(RpcError.ErrorType.TRANSPORT));
        }
    }

}

class MockSessionContext implements SessionContext {
    private MockConnectionConductor conductor;
    private Map<SwitchConnectionDistinguisher, ConnectionConductor> map;
    private IMessageDispatchService messageService;
    private boolean isValid = true;
    private CompositeObjectRegistration<ModelDrivenSwitch> registration;
    private int seed;

    MockSessionContext(int conductorNum) {
        conductor = new MockConnectionConductor(conductorNum);
        map = new HashMap<SwitchConnectionDistinguisher, ConnectionConductor>();
        messageService = new MessageDispatchServiceImpl(this);
    }

    @Override
    public MockConnectionConductor getPrimaryConductor() {
        // TODO Auto-generated method stub
        return conductor;
    }

    @Override
    public GetFeaturesOutput getFeatures() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionConductor getAuxiliaryConductor(SwitchConnectionDistinguisher auxiliaryKey) {

        return map.get(auxiliaryKey);
    }

    @Override
    public Set<Entry<SwitchConnectionDistinguisher, ConnectionConductor>> getAuxiliaryConductors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addAuxiliaryConductor(SwitchConnectionDistinguisher auxiliaryKey, ConnectionConductor conductorArg) {
        map.put(auxiliaryKey, conductorArg);
    }

    @Override
    public ConnectionConductor removeAuxiliaryConductor(SwitchConnectionDistinguisher connectionCookie) {
        return map.remove(connectionCookie);
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return isValid;
    }

    @Override
    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Override
    public SwitchSessionKeyOF getSessionKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMessageDispatchService getMessageDispatchService() {
        // TODO Auto-generated method stub
        return messageService;
    }

    @Override
    public Long getNextXid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Long, PortGrouping> getPhysicalPorts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Long> getPorts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PortGrouping getPhysicalPort(Long portNumber) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getPortBandwidth(Long portNumber) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPortEnabled(long portNumber) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPortEnabled(PortGrouping port) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<PortGrouping> getEnabledPorts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Long, Boolean> getPortsBandwidth() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompositeObjectRegistration<ModelDrivenSwitch> getProviderRegistration() {
        return registration;
    }

    @Override
    public void setProviderRegistration(
            CompositeObjectRegistration<ModelDrivenSwitch> registration) {
        this.registration = registration;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }
}

class MockConnectionConductor implements ConnectionConductor {

    private int conductorNum;
    private MockConnectionAdapter adapter;

    public MockConnectionConductor(int conductorNumber) {
        conductorNum = conductorNumber;
        adapter = new MockConnectionAdapter();
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public Short getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CONDUCTOR_STATE getConductorState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConductorState(CONDUCTOR_STATE conductorState) {
        // TODO Auto-generated method stub

    }

    @Override
    public Future<Boolean> disconnect() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSessionContext(SessionContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setConnectionCookie(SwitchConnectionDistinguisher auxiliaryKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public SessionContext getSessionContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SwitchConnectionDistinguisher getAuxiliaryKey() {
        if (0 != conductorNum) {
            SwitchConnectionCookieOFImpl key = new SwitchConnectionCookieOFImpl();
            key.setAuxiliaryId((short) conductorNum);
            key.init(42);
            return key;
        }
        return null;
    }

    @Override
    public ConnectionAdapter getConnectionAdapter() {
        // TODO Auto-generated method stub
        return adapter;
    }

    public MessageType getMessageType() {
        return adapter.getMessageType();
    }

    @Override
    public void setQueueProcessor(QueueProcessor<OfHeader, DataObject> queueKeeper) {
        // NOOP
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        // NOOP
    }

    @Override
    public void setId(int conductorId) {
        // NOOP
    }
}

enum MessageType {
    NONE, BARRIER, FLOWMOD, TABLEMOD, PACKETOUT;
}

class MockConnectionAdapter implements ConnectionAdapter {

    private MessageType messageType;
    private ConnectionReadyListener connectionReadyListener;

    public MockConnectionAdapter() {
        setMessageType(MessageType.NONE);
    }

    @Override
    public Future<RpcResult<BarrierOutput>> barrier(BarrierInput input) {
        setMessageType(MessageType.BARRIER);
        return null;
    }

    @Override
    public Future<RpcResult<EchoOutput>> echo(EchoInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> echoReply(EchoReplyInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> experimenter(ExperimenterInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> flowMod(FlowModInput input) {
        setMessageType(MessageType.FLOWMOD);
        return null;
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetFeaturesOutput>> getFeatures(GetFeaturesInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(GetQueueConfigInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> groupMod(GroupModInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> hello(HelloInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> meterMod(MeterModInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> packetOut(PacketOutInput input) {
        setMessageType(MessageType.PACKETOUT);
        return null;
    }

    @Override
    public Future<RpcResult<Void>> portMod(PortModInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<RoleRequestOutput>> roleRequest(RoleRequestInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> setAsync(SetAsyncInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> setConfig(SetConfigInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<Void>> tableMod(TableModInput input) {
        setMessageType(MessageType.TABLEMOD);
        return null;
    }

    @Override
    public Future<Boolean> disconnect() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAlive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setMessageListener(OpenflowProtocolListener messageListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSystemListener(SystemNotificationsListener systemListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkListeners() {
        // TODO Auto-generated method stub

    }

    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public void fireConnectionReadyNotification() {
        connectionReadyListener.onConnectionReady();
    }

    @Override
    public void setConnectionReadyListener(
            ConnectionReadyListener connectionReadyListener) {
        this.connectionReadyListener = connectionReadyListener;
    }

    @Override
    public Future<RpcResult<Void>> multipartRequest(
            MultipartRequestInput input) {
        // TODO Auto-generated method stub
        return null;
    }
}
