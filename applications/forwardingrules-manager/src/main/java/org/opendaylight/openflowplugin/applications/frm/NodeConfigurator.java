/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Callable;

public interface NodeConfigurator extends AutoCloseable {

    <T> ListenableFuture<T> enqueueJob(String key, Callable<ListenableFuture<T>> mainWorker);

}
