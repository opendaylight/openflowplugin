/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.jobcoordinator.api;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class SuccessCallable implements Callable<List<ListenableFuture<? extends Object>>> {

    private List<? extends Object> results;

    public SuccessCallable() {
    }

    public List<? extends Object> getResults() {
        return results;
    }

    public void setResults(List<? extends Object> results) {
        this.results = results;
    }
}
