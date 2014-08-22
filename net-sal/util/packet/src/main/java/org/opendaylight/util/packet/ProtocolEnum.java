/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * Interface implemented by all protocol enumerations to provide code.
 * 
 * @author Frank Wood
 */
public interface ProtocolEnum {

    /**
     * Returns the code value for the enumeration constant.
     * 
     * @return the code value
     */
    int code();

}
