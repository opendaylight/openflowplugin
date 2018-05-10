/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.ChangeOperationalStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.ChangeOperationalStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetLldpFloodIntervalInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetLldpFloodIntervalOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetLldpFloodIntervalOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetOperationalStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetOperationalStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetOperationalStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.LldpSpeakerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.SetLldpFloodIntervalInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.SetLldpFloodIntervalOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class OperationalStatusChangeService implements LldpSpeakerService {

    private final LLDPSpeaker speakerInstance;

    public OperationalStatusChangeService(final LLDPSpeaker speakerInstance) {
        this.speakerInstance = speakerInstance;
    }

    @Override
    public ListenableFuture<RpcResult<ChangeOperationalStatusOutput>> changeOperationalStatus(final
                                                                                          ChangeOperationalStatusInput
                                                                                           input) {
        speakerInstance.setOperationalStatus(input.getOperationalStatus());
        RpcResultBuilder<ChangeOperationalStatusOutput> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public ListenableFuture<RpcResult<GetOperationalStatusOutput>> getOperationalStatus(
            GetOperationalStatusInput input) {
        RpcResultBuilder<GetOperationalStatusOutput> rpcResultBuilder = RpcResultBuilder.success();
        GetOperationalStatusOutputBuilder getOperationalStatusOutputBuilder = new GetOperationalStatusOutputBuilder();
        getOperationalStatusOutputBuilder.setOperationalStatus(speakerInstance.getOperationalStatus());
        rpcResultBuilder.withResult(getOperationalStatusOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public ListenableFuture<RpcResult<SetLldpFloodIntervalOutput>> setLldpFloodInterval(final SetLldpFloodIntervalInput
                                                                                               input) {
        speakerInstance.setLldpFloodInterval(input.getInterval());
        RpcResultBuilder<SetLldpFloodIntervalOutput> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public ListenableFuture<RpcResult<GetLldpFloodIntervalOutput>> getLldpFloodInterval(
            GetLldpFloodIntervalInput intput) {
        RpcResultBuilder<GetLldpFloodIntervalOutput> rpcResultBuilder = RpcResultBuilder.success();
        GetLldpFloodIntervalOutputBuilder getLldpFloodIntervalOutputBuilder = new GetLldpFloodIntervalOutputBuilder();
        getLldpFloodIntervalOutputBuilder.setInterval(speakerInstance.getLldpFloodInterval());
        rpcResultBuilder.withResult(getLldpFloodIntervalOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
