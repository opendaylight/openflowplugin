/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;


/**
 * Classes implementing this interface can be suspended and resumed.
 * 
 * @author Fablel Zuniga
 * @author Vamsi Krishna Devaki
 */
public interface Suspendable {

    /**
     * Suspend operations
     */
    void suspend();

    /**
     * Resume operations
     */
    void resume();

}
