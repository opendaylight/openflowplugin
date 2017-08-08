/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.core.session.ExtensionSessionManager;

/**
 * @author mirehak
 */
public abstract class OFSessionUtil {

    /**
     * @return session manager singleton instance
     */
    public static ExtensionSessionManager getSessionManager() {
        return ExtensionSessionManagerImpl.getInstance();
    }

    /**
     * @return extension converters provider
     */
    public static ExtensionConverterProvider getExtensionConvertorProvider() {
        return getSessionManager().getExtensionConverterProvider();
    }

}
