/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutputBuilder;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalBundleRpcsMock {

    private final List<ControlBundleInput> controlBundleInput = new ArrayList<>();
    private final List<AddBundleMessagesInput>  addBundleMessagesInput = new ArrayList<>();

    private ListenableFuture<RpcResult<ControlBundleOutput>> controlBundle(ControlBundleInput input) {
        getControlBundleInput().add(input);
        return RpcResultBuilder.success(new ControlBundleOutputBuilder().build()).buildFuture();
    }

    private ListenableFuture<RpcResult<AddBundleMessagesOutput>> addBundleMessages(AddBundleMessagesInput input) {
        getAddBundleMessagesInput().add(input);
        return RpcResultBuilder.success(new AddBundleMessagesOutputBuilder().build()).buildFuture();
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(ControlBundle.class, this::controlBundle)
            .put(AddBundleMessages.class, this::addBundleMessages)
            .build();
    }

    public List<ControlBundleInput> getControlBundleInput() {
        return controlBundleInput;
    }

    public List<AddBundleMessagesInput> getAddBundleMessagesInput() {
        return addBundleMessagesInput;
    }
}
