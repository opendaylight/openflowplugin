/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.queue;

import java.util.List;

import com.google.common.util.concurrent.SettableFuture;

/**
 * @author mirehak
 * @param <T> result typer 
 */
public interface TicketResult<T> {

    /**
     * @return processed result
     */
    SettableFuture<List<T>> getResult();

    /**
     * @return direct access to result
     */
    List<T> getDirectResult();

    /**
     * @param directResult setter for direct result
     */
    void setDirectResult(List<T> directResult);

}
