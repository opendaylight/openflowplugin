/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.io.Closeable;

/**
 * Classes implementing this interface are able to be terminated: cease or
 * cause to cease operation. A terminated class might not able to resume
 * operations anymore; a new instance might be needed. The stop method is
 * invoked to release resources.
 * <p>
 * There is a difference in purpose with {@link Closeable}: {@link Closeable}
 * is used with try-with-resources. A resource is an object that must be
 * closed after the program is finished with it. try-with-resources is used
 * for small scope resources. A stoppable is normally stopped when the program
 * terminates (Like a service).
 * 
 * @author Fabiel Zuniga
 */
public interface Stoppable {

    /**
     * Stops (shutdowns) operations. Resources will be released.
     */
    public void stop();
}
