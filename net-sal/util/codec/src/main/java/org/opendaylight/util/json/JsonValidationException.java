/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

/**
 * A runtime exception to denote something went wrong with JSON validation.
 *
 * @author Liem Nguyen
 */
public class JsonValidationException extends JsonCodecException {

    private static final long serialVersionUID = -3375060817490467045L;

    public JsonValidationException(String msg) {
        super(msg);
    }
    
    public JsonValidationException(String msg, Throwable t) {
        super(msg, t);
    }
}
