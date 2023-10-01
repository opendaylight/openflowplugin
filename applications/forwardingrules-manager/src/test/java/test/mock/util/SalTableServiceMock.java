/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalTableServiceMock {
    private final List<UpdateTableInput> updateTableInput = new ArrayList<>();

    public List<UpdateTableInput> getUpdateTableInput() {
        return updateTableInput;
    }

    public ListenableFuture<RpcResult<UpdateTableOutput>> updateTable(UpdateTableInput input) {
        updateTableInput.add(input);
        return RpcResultBuilder.success(new UpdateTableOutputBuilder().build()).buildFuture();
    }
}
