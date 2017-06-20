/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.translator;

import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;

public interface TranslatorLibrarian {

    /**
     * Method provides translator library held by Librarian.
     * @return translator library
     */
    TranslatorLibrary oook();

    /**
     * Method registers translator library for translating message objects.
     * @param translatorLibrary translator library
     */
    void setTranslatorLibrary(TranslatorLibrary translatorLibrary);

}
