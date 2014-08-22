/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.crypt;

/**
 * A {@link RuntimeException} to capture any exception arising out of doing 
 * encryption or decryption.
 *
 * @author Liem Nguyen
 */
public class EncryptionException extends RuntimeException {
    private static final long serialVersionUID = 3364409031227573838L;

    public EncryptionException(String msg) {
        super(msg);
    }
    
    public EncryptionException(Throwable t) {
        super(t);
    }
    
    public EncryptionException(String msg, Throwable t) {
        super(msg, t);
    }
}
