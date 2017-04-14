/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.session;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Message Dispatch Service to send the message to switch.
 */
public interface IMessageDispatchService {

    String CONNECTION_ERROR_MESSAGE = "Session for the cookie is invalid. Reason: "
            + "the switch has been recently disconnected OR inventory provides outdated information.";

    /**
     * send barrier message to switch.
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
     * send experimenter message to switch.
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
     * send flow modification message to switch.
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<UpdateFlowOutput>> flowMod(FlowModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send get async message to switch.
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
     * send get config message to switch.
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
     * send get features message to switch.
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
     * send get queue config message to switch.
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
     * send group modification message to switch.
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<UpdateGroupOutput>> groupMod(GroupModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send meter modification message to switch.
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<UpdateMeterOutput>> meterMod(MeterModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send multipart request message to switch.
     *
     * @param input
     *            - multipart request message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<java.lang.Void>> multipartRequest(
            MultipartRequestInput input,
            SwitchConnectionDistinguisher cookie);

    /**
     * send packet out message to switch.
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
     * send port modification message to switch.
     *
     * @param input
     *            - message
     * @param cookie
     *            - to identify connection if null then feel free to send via
     *            any connection
     * @return - the future
     */
    Future<RpcResult<UpdatePortOutput>> portMod(PortModInput input, SwitchConnectionDistinguisher cookie);

    /**
     * send role request message to switch.
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
     * send set async message to switch.
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
     * send set config message to switch.
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
     * send table modification message to switch.
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
