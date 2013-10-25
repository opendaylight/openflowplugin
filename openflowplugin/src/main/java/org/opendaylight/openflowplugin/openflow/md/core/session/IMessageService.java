package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.concurrent.Future;

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

/**
 * Message Service can be use to send the message to switch.
 *
 * @author AnilGujele
 *
 */
public interface IMessageService {

    /**
     * send barrier message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<BarrierOutput>> barrier(BarrierInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send experimenter message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> experimenter(ExperimenterInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send flow modification message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> flowMod(FlowModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send get async message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send get config message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send get features message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<GetFeaturesOutput>> getFeatures(GetFeaturesInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send get queue config message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(GetQueueConfigInput input,
            SwitchConnectionDistinguisher cookie);

    /**
     * send group modification message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> groupMod(GroupModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send meter modification message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> meterMod(MeterModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send packet out message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> packetOut(PacketOutInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send port modification message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> portMod(PortModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send role request message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<RoleRequestOutput>> roleRequest(RoleRequestInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send set async message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> setAsync(SetAsyncInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send set config message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> setConfig(SetConfigInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send table modification message to switch
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> tableMod(TableModInput input, SwitchConnectionDistinguisher cookie);

}
