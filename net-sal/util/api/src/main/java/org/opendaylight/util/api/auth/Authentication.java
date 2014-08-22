/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api.auth;

import java.util.Set;

/**
 * Abstraction of authentication record with an opaque identifying token and
 * an expiration.
 * 
 * @author Thomas Vachuska
 */
public interface Authentication {

    /**
     * Authentication token uniquely identifying this authentication.
     * 
     * @return authentication token
     */
    public String token();

    /**
     * Get the authentication expiration date/time in number of milliseconds 
     * since start of epoch.
     * 
     * @return expiration milliseconds since start of UTC epoch
     */
    public long expiration();

    /**
     * Get the user id.
     * 
     * @return unique user id
     */
    public String userId();
    
    /**
     * Get the user name.
     * 
     * @return user name
     */
    public String userName();
    
    /**
     * Get the authentication domain.
     * 
     * @return unique domain id
     */
    public String domainId();
    
    /**
     * Get the authentication domain name.
     * 
     * @return domain name
     */
    public String domainName();
    
    /**
     * Get a set of user roles.
     * 
     * @return set of user roles
     */
    public Set<String> roles();

}
