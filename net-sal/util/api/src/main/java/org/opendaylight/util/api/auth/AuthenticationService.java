/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api.auth;

import org.opendaylight.util.api.ServiceNotFoundException;

/**
 * Authentication service facade.
 * 
 * @author Thomas Vachuska
 * @author Liem Nguyen
 */
public interface AuthenticationService {
    
    /**
     * Authenticate against the specified domain using the supplied user name
     * and password.
     * 
     * @param domain authentication domain
     * @param user user name
     * @param password user password
     * @return authentication containing unique identifying token and
     *         associated authentication information
     * @throws AuthenticationException if authentication fails for some reason
     * @throws ServiceNotFoundException if the required authentication service
     *         is not available
     */
    Authentication authenticate(String domain, String user, String password);
    
    /**
     * Determines whether the specified authentication token is valid and if
     * so, provides the associated authentication data.
     * 
     * @param token token to be validated
     * @return authentication data associated with the given token via prior
     *         call to {@link #authenticate(String, String, String)}
     * @throws AuthenticationException if validation fails for some reason
     * @throws ServiceNotFoundException if the required authentication service
     *         is not available
     */
    Authentication validate(String token);

    /**
     * Invalidates the specified authentication token.
     * 
     * @param token token to be invalidated
     * @throws AuthenticationException if token cannot be invalidated for some
     *         reason
     * @throws ServiceNotFoundException if the required authentication service
     *         is not available
     */
    void invalidate(String token);
    
}
