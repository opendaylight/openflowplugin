/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.StringUtils;

/**
 * A default implementation of the flags facet. This implementation
 * makes the assumption that the context is a {@link DefaultDeviceInfo} implementation.
 *
 * @author Simon Hunt
 */
public class DefaultFlags extends AbstractDefaultFacet implements Flags {
    /**
     * Constructs a flags facet that is projected onto the specified device info context.
     *
     * @param context the device info of which this is a facet
     */
    public DefaultFlags(DeviceInfo context) {
        super(context);
    }

    /** {@inheritDoc}
     *
     * @throws IllegalArgumentException if flag is not alphanumeric only
     */
    @Override
    public boolean hasFlag(String flag) {
        if (StringUtils.isEmpty(flag))
            throw new IllegalArgumentException("flag cannot be an empty string (or null)");

        if (!StringUtils.isAlphaNumeric(flag))
            throw new IllegalArgumentException("flag must be alphanumeric only (underscores allowed)");

        return getContext().getType().getFlags().contains(flag);
    }

    @Override
    public int flagCount() {
        return getContext().getType().getFlags().size();
    }
}
