/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Lookup and register key for extension converters, basic case expects this to
 * correlate with input model type.
 *
 * @param <T> type of key
 */
public class ConverterExtensionKey<T extends ExtensionKey> extends TypeVersionKey<T> {
    @SuppressWarnings("unchecked")
    public ConverterExtensionKey(final T type, final Uint8 ofVersion) {
        super((Class<T>) type.implementedInterface(), ofVersion);
    }
}
