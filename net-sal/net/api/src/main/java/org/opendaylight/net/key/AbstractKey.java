/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.key;

import org.opendaylight.util.crypt.EncryptedString;
import org.opendaylight.util.crypt.EncryptionException;
import org.opendaylight.util.crypt.MasterKey;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Base abstraction of a device key.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
public abstract class AbstractKey implements Key {

    private final String id;
    private final KeyType type;
    private String label;


    protected AbstractKey(String id, KeyType type, String label) {
        notNull(type, id);
        this.id = id;
        this.type = type;
        this.label = label;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public KeyType type() {
        return type;
    }

    @Override
    public String label() {
        return label;
    }

    // FIXME: is this correct?

    public void setDescription(String description) {
        this.label = description;
    }

    protected String encrypt(String data) {
        EncryptedString es = new EncryptedString(MasterKey.key());
        try {
            return es.encrypt(data);
        } catch (EncryptionException e) {
        }
        return null;
    }

    protected String decrypt(String data) {
        EncryptedString es = new EncryptedString(MasterKey.key());
        try {
            return es.decrypt(data);
        } catch (EncryptionException e) {
        }
        return null;
    }

    abstract boolean dataEquals(Object other);
    abstract int dataHashCode();

}
