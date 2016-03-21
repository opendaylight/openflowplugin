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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.BatchChoice;

/**
 * Container of CRUD actions for one type of object (flow, group, meter, ..) of same type (add, remove, update)
 */
public class BatchPlanStep<T extends BatchChoice> {
    private final List<T> taskBag;
    private final BatchStepType stepType;
    private boolean barrierAfter = false;

    public BatchPlanStep(final Class<T> implementedInterface, final BatchStepType stepType) {
        this.stepType = stepType;
        taskBag = new ArrayList<>();
    }

    public List<T> getTaskBag() {
        return taskBag;
    }

    public BatchStepType getStepType() {
        return stepType;
    }

    public void add(final BatchChoice task) {
        taskBag.add((T) task);
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
