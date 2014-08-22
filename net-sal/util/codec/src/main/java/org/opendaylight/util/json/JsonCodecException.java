/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

/**
 * Json codec exception.
 * 
 * @author liemmn
 *
 */
public class JsonCodecException extends RuntimeException {
    private static final long serialVersionUID = -9208119902867877247L;

    public JsonCodecException(String msg) {
        super(msg);
    }
    
    public JsonCodecException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
