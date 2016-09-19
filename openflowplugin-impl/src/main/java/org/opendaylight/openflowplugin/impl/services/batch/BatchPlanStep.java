/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.batch;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.service.batch.common.rev160322.BatchOrderGrouping;

/**
 * Container of CRUD actions for one type of object (flow, group, meter, ..) of same type (add, remove, update).
 */
public class BatchPlanStep {
    private final List<? extends BatchOrderGrouping> taskBag;
    private final BatchStepType stepType;
    private boolean barrierAfter = false;

    public BatchPlanStep(final BatchStepType stepType) {
        this.stepType = stepType;
        taskBag = new ArrayList<>();
    }

    public <T extends BatchOrderGrouping> List<T> getTaskBag() {
        return (List<T>) taskBag;
    }

    public BatchStepType getStepType() {
        return stepType;
    }

    public boolean isEmpty() {
        return taskBag.isEmpty();
    }

    public void setBarrierAfter(final boolean barrier) {
        this.barrierAfter = barrier;
    }

    public boolean isBarrierAfter() {
        return barrierAfter;
    }
}
