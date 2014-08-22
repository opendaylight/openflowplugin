/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import java.io.InputStream;

/**
 * A JSON Factory interface.
 * 
 * @author Liem Nguyen
 *
 */
public interface JsonFactory {
    
    /**
     * Returns the JSON schema for this factory.
     *  
     * @return JSON schema for this factory
     */
    InputStream schema();
    
    /**
     * Sees if this JsonFactory has a codec for the given POJO class.
     * 
     * @param pojo POJO class to be encoded/decoded
     * @return true if there is a codec, false otherwise
     */
    boolean hasCodec(Class<?> pojo);

    /** 
     * Returns a given codec for the given POJO.
     * 
     * @param pojo POJO for which a JSON codec is requested.
     * @return a JSON codec for the given POJO.
     * @throws JsonCodecException if no codec found for the given POJO
     */
    JsonCodec<?> codec(Class<?> pojo); 

}
