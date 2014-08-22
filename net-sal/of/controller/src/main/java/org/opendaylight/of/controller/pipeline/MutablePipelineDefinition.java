/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline;

import org.opendaylight.of.lib.MutableObject;

/**
 * Mutable variant of {@link PipelineDefinition}. Provides {@link TableContext}
 * and state modification capability to the definition.
 *
 * @author Pramod Shanbhag
 */
public interface MutablePipelineDefinition extends 
                    MutableType<PipelineDefinition>, MutableObject {
    
    /** Adds the given table context to the context list. 
     * 
     * @param tc the table context
     * @return self, for chaining
     * @throws NullPointerException if tc is null
     */
    MutablePipelineDefinition addTableContext(TableContext tc);
    
    /** Removes the given table context from the context list. 
     * 
     * @param tc the table context
     * @return self, for chaining
     * @throws NullPointerException if tc is null
     */
    MutablePipelineDefinition removeTableContext(TableContext tc);
}
