/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.version.rev160511.GetOfVersionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.version.rev160511.GetOfVersionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.version.rev160511.GetOfVersionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.version.rev160511.OfVersionService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;

public class OfVersionServiceImpl implements OfVersionService {
	
	final RequestContextStack requestContextStack;
    final DeviceContext deviceContext;
    
	public OfVersionServiceImpl(RequestContextStack requestContextStack, DeviceContext deviceContext) {
		this.deviceContext = deviceContext;
		this.requestContextStack = requestContextStack;
	}

	@Override
	public Future<RpcResult<GetOfVersionOutput>> getOfVersion(GetOfVersionInput input) {
		RpcResultBuilder<GetOfVersionOutput> rpcResultBuilder = RpcResultBuilder.success();
		GetOfVersionOutputBuilder builder = new GetOfVersionOutputBuilder();
		builder.setNode(input.getNode());
		builder.setOfVersion(deviceContext.getDeviceState().getVersion());
		rpcResultBuilder.withResult(builder);
		return Futures.immediateFuture(rpcResultBuilder.build());
	}

}
