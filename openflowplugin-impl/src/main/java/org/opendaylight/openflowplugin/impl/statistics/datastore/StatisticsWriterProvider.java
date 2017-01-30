/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class StatisticsWriterProvider {
    private Map<MultipartType, AbstractStatisticsWriter> writers = new HashMap<>();

    /**
     * Register statistics writer.
     *
     * @param type    the writer type
     * @param writer the writer instance
     */
    public void register(MultipartType type, AbstractStatisticsWriter writer) {
        writers.put(type, writer);
    }

    /**
     * Lookup  statistics writer.
     *
     * @param type the writer type
     * @return the writer instance
     */
    public Optional<? extends AbstractStatisticsWriter> lookup(MultipartType type) {
        return Optional.ofNullable(writers.get(type));
    }
}
