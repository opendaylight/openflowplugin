/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.Uint64;

public interface ReconciliationService {

    ListenableFuture<Set<Uint64>> reconcile(Set<Uint64> nodes);

    ListenableFuture<Set<Uint64>> reconcileAll();
}
