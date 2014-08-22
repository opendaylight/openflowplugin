/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import org.junit.Test;
import org.opendaylight.util.api.auth.AuthenticationException;
import org.opendaylight.util.api.auth.AuthorizationException;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.assertException;

/**
 * Simple tests for various exceptions.
 *
 * @author Thomas Vachuska
 * @author Steve Britt
 * @author Feroz Ahmed
 * @author Ankit Kumar
 */
public class ExceptionTest {

    @Test
    public void notFoundException() {
        assertException(new NotFoundException(), null, null);
        assertException(new NotFoundException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new NotFoundException("bar", c), "bar", c);
    }

    @Test
    public void duplicateIdException() {
        assertException(new DuplicateIdException(), null, null);
        assertException(new DuplicateIdException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new DuplicateIdException("bar", c), "bar", c);
    }
    
    @Test
    public void serviceNotFoundException() {
        assertException(new ServiceNotFoundException(), null, null);
        assertException(new ServiceNotFoundException("foo"), "foo", null);
    }

    @Test
    public void loginFailedException() {
        assertException(new LoginFailedException(), null, null);
        assertException(new LoginFailedException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new LoginFailedException("bar", c), "bar", c);
    }

    @Test
    public void authorizationException() {
        assertException(new AuthorizationException(), null, null);
        assertException(new AuthorizationException("foo"), "foo", null);
        Throwable c = new AuthorizationException();
        assertException(new AuthorizationException("bar", c), "bar", c);
    }

    @Test
    public void authenticationException() {
        assertException(new AuthenticationException(), null, null);
        assertException(new AuthenticationException("foo"), "foo", null);
        String token = "Yo, I'm a token";
        AuthenticationException ae = new AuthenticationException("foo", token);
        assertException(ae, "foo", null);
        assertEquals("unexpected token", token, ae.getToken());
        Throwable c = new IllegalStateException();
        assertException(new AuthenticationException("bar", c), "bar", c);
        ae = new AuthenticationException("foo", token, c);
        assertException(ae, "foo", c);
        assertEquals("unexpected token", token, ae.getToken());
    }

    @Test
    public void createException() {
        assertException(new CreateException(), null, null);
        assertException(new CreateException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new CreateException("bar", c), "bar", c);
    }

    @Test
    public void updateException() {
        assertException(new UpdateException(), null, null);
        assertException(new UpdateException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new UpdateException("bar", c), "bar", c);
    }

    @Test
    public void deleteException() {
        assertException(new DeleteException(), null, null);
        assertException(new DeleteException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new DeleteException("bar", c), "bar", c);
    }
    
    @Test
    public void duplicateDataException() {
        assertException(new DuplicateDataException(), null, null);
        assertException(new DuplicateDataException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new DuplicateDataException("bar", c), "bar", c);
    }
    
    @Test
    public void preconditionFailedException() {
        assertException(new PreconditionFailedException(), null, null);
        assertException(new PreconditionFailedException("foo"), "foo", null);
        Throwable c = new IllegalStateException();
        assertException(new PreconditionFailedException("bar", c), "bar", c);
    }
}