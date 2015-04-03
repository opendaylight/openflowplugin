/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.translator;

import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 3.4.2015.
 */
public interface TranslatorLibrarian {

    /**
     * Method provides translator library held by Librarian.
     * @return
     */
    public TranslatorLibrary oook();

    /**
     * Method registers translator library for translating message objects.
     *
     * @param translatorLibrary
     */
    public void setTranslatorLibrary(TranslatorLibrary translatorLibrary);

}
