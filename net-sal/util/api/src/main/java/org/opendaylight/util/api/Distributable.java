/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Custom serialization of a class in distributed environments is enabled by
 * the class implementing this interface.
 * <p>
 * This depends on the coordination service implementation; not all require a
 * share-able object to implement this interface.
 * 
 * @author Fabiel Zuniga
 */
public interface Distributable {

}
