/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import static java.util.Objects.requireNonNull;

import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 3.4.2015.
 */
public class TranslatorKeyFactory {
    private final Uint8 version;

    public TranslatorKeyFactory(final Uint8 version) {
        this.version = requireNonNull(version);
    }

    public TranslatorKey createTranslatorKey(final Class<?> messageClass) {
        return new TranslatorKey(version, messageClass.getName());
    }
}
