package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.concurrent.Future;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * message dispatch service to send the message to switch.
 *
 * @author AnilGujele
 *
 */
public class MessageDispatchServiceImpl implements IMessageDispatchService {

    private static final Logger LOG = LoggerFactory.getLogger(OFSessionUtil.class);

    private SessionContext session;

    /**
     * constructor
     *
     * @param session
     *            - MessageDispatchService for this session
     */
    public MessageDispatchServiceImpl(SessionContext session) {
        this.session = session;
    }

    /**
     * get proper connection adapter to send the message to switch.
     *
     * @param - cookie to identify the right connection, it can be null also.
     * @return connectionAdapter associated with cookie, otherwise return best
     *         suitable connection.
     *
     */

    private ConnectionAdapter getConnectionAdapter(SwitchConnectionDistinguisher cookie) {

        if (!session.isValid()) {
            LOG.warn("Session for the cookie {} is invalid." + cookie);
            throw new IllegalArgumentException("Session for the cookie is invalid.");
        }
        LOG.debug("finding connecton for cookie value {}. " + cookie);
        // set main connection as default
        ConnectionAdapter connectionAdapter = session.getPrimaryConductor().getConnectionAdapter();
        if (null != cookie) {
            ConnectionConductor conductor = session.getAuxiliaryConductor(cookie);
            // check if auxiliary connection exist
            if (null != conductor) {
                LOG.debug("found auxiliary connection for the cookie.");
                connectionAdapter = conductor.getConnectionAdapter();
            }
        } else {
            // TODO: pick connection to utilize all the available connection.
        }
        return connectionAdapter;
    }

    @Override
    public Future<RpcResult<BarrierOutput>> barrier(BarrierInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).barrier(input);
    }

    @Override
    public Future<RpcResult<Void>> experimenter(ExperimenterInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).experimenter(input);
    }

    @Override
    public Future<RpcResult<Void>> flowMod(FlowModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).flowMod(input);
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getAsync(input);
    }

    @Override
    public Future<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getConfig(input);
    }

    @Override
    public Future<RpcResult<GetFeaturesOutput>> getFeatures(GetFeaturesInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getFeatures(input);
    }

    @Override
    public Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(GetQueueConfigInput input,
            SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).getQueueConfig(input);
    }

    @Override
    public Future<RpcResult<Void>> groupMod(GroupModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).groupMod(input);
    }

    @Override
    public Future<RpcResult<Void>> meterMod(MeterModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).meterMod(input);
    }

    @Override
    public Future<RpcResult<Void>> packetOut(PacketOutInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).packetOut(input);
    }

    @Override
    public Future<RpcResult<Void>> portMod(PortModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).portMod(input);
    }

    @Override
    public Future<RpcResult<RoleRequestOutput>> roleRequest(RoleRequestInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).roleRequest(input);
    }

    @Override
    public Future<RpcResult<Void>> setAsync(SetAsyncInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).setAsync(input);
    }

    @Override
    public Future<RpcResult<Void>> setConfig(SetConfigInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).setConfig(input);
    }

    @Override
    public Future<RpcResult<Void>> tableMod(TableModInput input, SwitchConnectionDistinguisher cookie) {
        return getConnectionAdapter(cookie).tableMod(input);
    }

}
