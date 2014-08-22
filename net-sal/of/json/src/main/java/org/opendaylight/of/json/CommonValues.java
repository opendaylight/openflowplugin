/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.json;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * Localization resource for common terms.
 *
 * @author Simon Hunt
 */
public class CommonValues {

    private static final ResourceBundle RES =
            getBundledResource(CommonValues.class);

    /**
     * Returns the localized string for the given key.
     *
     * @param key the lookup key
     * @return the corresponding localized string
     * @throws NullPointerException if key is null
     * @throws MissingResourceException if no entry exists for the given key
     */
    public static String lookup(String key) {
        return RES.getString(key);
    }
}
