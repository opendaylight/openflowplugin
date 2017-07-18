/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;


import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class SalBundleServiceMock implements SalBundleService {

    private final List<ControlBundleInput> controlBundleInput = new ArrayList<>();
    private final List<AddBundleMessagesInput>  addBundleMessagesInput = new ArrayList<>();


    @Override
    public Future<RpcResult<java.lang.Void>> controlBundle(ControlBundleInput input) {
        getControlBundleInput().add(input);
        return null;
    }

    @Override
    public Future<RpcResult<java.lang.Void>> addBundleMessages(AddBundleMessagesInput input) {
        getAddBundleMessagesInput().add(input);
        return null;
    }

    public List<ControlBundleInput> getControlBundleInput() {
        return controlBundleInput;
    }

    public List<AddBundleMessagesInput> getAddBundleMessagesInput() {
        return addBundleMessagesInput;
    }


}
