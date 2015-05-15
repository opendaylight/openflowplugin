/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 3.4.2015.
 */
public class TranslatorKeyFactory {

    short version;

    public TranslatorKeyFactory(final short version) {
        this.version = version;
    }

    public TranslatorKey createTranslatorKey(final Class<?> messageClass) {
        return new TranslatorKey(version, messageClass.getName().toString());
    }
}
