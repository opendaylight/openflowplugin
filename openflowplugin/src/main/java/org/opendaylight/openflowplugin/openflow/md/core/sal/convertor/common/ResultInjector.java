/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

/**
 * Injecting match object into chosen target
 * 
 * @param <R> result to inject (e.g.: OF-API match) 
 * @param <T> target of injection
 */
public interface ResultInjector<R, T> {

    /**
     * @param result result
     * @param target target
     */
    void inject(R result, T target);
    
}
