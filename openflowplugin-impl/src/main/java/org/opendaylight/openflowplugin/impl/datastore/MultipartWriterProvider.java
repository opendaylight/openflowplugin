/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.openflowplugin.impl.datastore.multipart.AbstractMultipartWriter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartWriterProvider {

    private final Map<MultipartType, AbstractMultipartWriter> writers = new HashMap<>();

    /**
     * Register statistics writer.
     *
     * @param type    the writer type
     * @param writer the writer instance
     */
    public void register(final MultipartType type, final AbstractMultipartWriter writer) {
        writers.put(type, writer);
    }

    /**
     * Lookup  statistics writer.
     *
     * @param type the writer type
     * @return the writer instance
     */
    public Optional<AbstractMultipartWriter> lookup(final MultipartType type) {
        return Optional.ofNullable(writers.get(type));
    }

}
