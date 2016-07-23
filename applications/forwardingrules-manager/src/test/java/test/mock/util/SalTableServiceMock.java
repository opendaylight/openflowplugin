/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;

import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class SalTableServiceMock implements SalTableService {
    private List<UpdateTableInput> updateTableInput = new ArrayList<>();


    public List<UpdateTableInput> getUpdateTableInput() {
        return updateTableInput;
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(UpdateTableInput input) {
        updateTableInput.add(input);
        return null;
    }
}
