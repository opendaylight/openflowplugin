/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.batch;

import com.google.common.util.concurrent.AsyncFunction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Wrapper for batch step jobs ({@link BatchPlanStep} and corresponding transform function).
 */
public class BatchStepJob {

    private final BatchPlanStep planStep;
    private final AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>> stepFunction;

    public BatchStepJob(final BatchPlanStep planStep,
                        final AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>> stepFunction) {
        this.planStep = planStep;
        this.stepFunction = stepFunction;
    }

    public BatchPlanStep getPlanStep() {
        return planStep;
    }

    public AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>> getStepFunction() {
        return stepFunction;
    }

}
