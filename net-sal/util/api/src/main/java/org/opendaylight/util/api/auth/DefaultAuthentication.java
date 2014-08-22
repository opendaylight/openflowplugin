/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api.auth;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Simple carrier of authentication record.
 *
 * @author Thomas Vachuska
 */
public class DefaultAuthentication implements Authentication, Serializable {
    
    private static final long serialVersionUID = 6832163207106035696L;
    
    private final String token;
    private long expiration;
    private final String userId, userName;
    private final String domainId, domainName;
    
    private final Set<String> roles = new HashSet<String>();
    
    /**
     * Create an authentication descriptor using the supplied information.
     * 
     * @param token token identifying the authentication
     * @param expiration time specified in number of milliseconds since start
     *        of UTC epoch
     * @param userId user unique id
     * @param userName user name
     * @param roles user roles as a comma-separated list
     * @param domainId id of the issuing authentication domain
     * @param domainName authentication domain name
     */
    public DefaultAuthentication(String token, long expiration,
                                 String userId, String userName, String roles,
                                 String domainId, String domainName) {
        this.token = token;
        this.expiration = expiration;
        this.userId = userId;
        this.userName = userName;
        this.domainId = domainId;
        this.domainName = domainName;
        
        StringTokenizer st = new StringTokenizer(roles, ",");
        while (st.hasMoreElements())
            this.roles.add(st.nextToken().trim());
    }
    
    @Override
    public String token() {
        return token;
    }
    
    @Override
    public long expiration() {
        return expiration;
    }
    
    @Override
    public String userId() {
        return userId;
    }

    @Override
    public String userName() {
        return userName;
    }

    @Override
    public Set<String> roles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public String domainId() {
        return domainId;
    }
    
    @Override
    public String domainName() {
        return domainName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultAuthentication other = (DefaultAuthentication) obj;
        if (token == null) {
            if (other.token != null)
                return false;
        } else if (!token.equals(other.token))
            return false;
        return true;
    }
    
}
