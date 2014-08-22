/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.MutablePipelineDefinition;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.dt.TableId;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable variant of {@link DefaultPipelineDefinition}.
 *
 * @author Pramod Shanbhag
 */
public class DefaultMutablePipelineDefinition extends DefaultPipelineDefinition
        implements MutablePipelineDefinition {

    private final Mutable mutt = new Mutable();
    
    public DefaultMutablePipelineDefinition() {
        super();
    }
    
    @Override
    public PipelineDefinition toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        DefaultPipelineDefinition definition = 
                new DefaultPipelineDefinition();
        definition.tableIdToContextMap = 
                new ConcurrentHashMap<TableId, TableContext>(
                        this.tableIdToContextMap);
        return definition;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public MutablePipelineDefinition addTableContext(TableContext tc) {
        tableIdToContextMap.put(tc.tableId(), tc);
        return this;
    }

    @Override
    public MutablePipelineDefinition removeTableContext(TableContext tc) {
        tableIdToContextMap.remove(tc.tableId());
        return this;
    }
 }
