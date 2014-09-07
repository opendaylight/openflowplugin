/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;


/**
 * @author mirehak
 * @param <T> result type
 *
 */
public interface PopListener<T> {
    
    /**
     * @param processedMessage
     */
    void onPop(T processedMessage);

}
