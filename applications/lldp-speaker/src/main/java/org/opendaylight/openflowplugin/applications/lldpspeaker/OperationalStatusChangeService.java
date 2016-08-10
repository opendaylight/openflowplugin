/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.ChangeOperationalStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetOperationalStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.GetOperationalStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.LldpSpeakerService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class OperationalStatusChangeService implements LldpSpeakerService {

    private final LLDPSpeaker speakerInstance;

    public OperationalStatusChangeService(final LLDPSpeaker speakerInstance) {
        this.speakerInstance = speakerInstance;
    }

    @Override
    public Future<RpcResult<Void>> changeOperationalStatus(final ChangeOperationalStatusInput input) {
        speakerInstance.setOperationalStatus(input.getOperationalStatus());
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetOperationalStatusOutput>> getOperationalStatus() {
        RpcResultBuilder<GetOperationalStatusOutput> rpcResultBuilder = RpcResultBuilder.success();
        GetOperationalStatusOutputBuilder getOperationalStatusOutputBuilder = new GetOperationalStatusOutputBuilder();
        getOperationalStatusOutputBuilder.setOperationalStatus(speakerInstance.getOperationalStatus());
        rpcResultBuilder.withResult(getOperationalStatusOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
