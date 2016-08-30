/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.RpcError;

/**
 * Util class for {@link RpcError}.
 */
public final class ErrorUtil {

    private ErrorUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static final String errorsToString(final Collection<RpcError> rpcErrors) {
        final StringBuilder errors = new StringBuilder();
        if ((null != rpcErrors) && (rpcErrors.size() > 0)) {
            for (final RpcError rpcError : rpcErrors) {
                errors.append(rpcError.getMessage());
            }
        }
        return errors.toString();
    }

}
