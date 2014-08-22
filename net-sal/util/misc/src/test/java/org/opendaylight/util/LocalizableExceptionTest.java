/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_NSR;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * This JUnit test class tests the LocalizableException class.
 *
 * @author Simon Hunt
 */
public class LocalizableExceptionTest {

    protected static final String MSGKEY = "~_MsgKey_~";
    protected static final String LOGMSG = "Some log message";
    protected static final Throwable CAUSE = new Throwable();

    protected LocalizableException ex;
    
    protected LocalizableException create() { 
        return new LocalizableException(); 
    }

    protected LocalizableException create(String key) { 
        return new LocalizableException(key); 
    }

    protected LocalizableException create(String key, String msg) { 
        return new LocalizableException(key, msg); 
    }

    protected LocalizableException create(String key, String msg, Throwable c) { 
        return new LocalizableException(key, msg, c); 
    }

    protected LocalizableException create(String key, Throwable c) { 
        return new LocalizableException(key, c); 
    }

    @Test
    public void noArgs() {
        print(EOL + "noArgs()");
        ex = create();
        print(ex);
        assertNull(AM_HUH, ex.getMessage());
        assertNull(AM_HUH, ex.getMessageKey());
        assertNull(AM_HUH, ex.getCause());
    }

    @Test
    public void msgKeyArg() {
        print(EOL + "msgKeyArg()");
        ex = create(MSGKEY);
        print(ex);
        assertNull(AM_HUH,  ex.getMessage());
        assertEquals(AM_NEQ, MSGKEY, ex.getMessageKey());
        assertNull(AM_HUH, ex.getCause());
    }

    @Test
    public void msgKeyAndLogMsg() {
        print(EOL + "msgKeyAndLogMsg()");
        ex = create(MSGKEY, LOGMSG);
        print(ex);
        assertEquals(AM_NEQ, LOGMSG, ex.getMessage());
        assertEquals(AM_NEQ, MSGKEY, ex.getMessageKey());
        assertNull(AM_HUH, ex.getCause());
    }

    @Test
    public void msgKeyLogMsgAndCause() {
        print (EOL + "msgKeyLogMsgAndCause()");
        ex = create(MSGKEY, LOGMSG, CAUSE);
        print(ex);
        assertEquals(AM_NEQ, LOGMSG, ex.getMessage());
        assertEquals(AM_NEQ, MSGKEY, ex.getMessageKey());
        assertSame(AM_NSR, CAUSE, ex.getCause());
    }

    @Test
    public void msgKeyAndCause() {
        print (EOL + "msgKeyAndCause()");
        ex = create(MSGKEY, CAUSE);
        print(ex);
        assertNotNull(AM_HUH, ex.getMessage()); // msg set to class of throwable
        assertEquals(AM_NEQ, MSGKEY, ex.getMessageKey());
        assertSame(AM_NSR, CAUSE, ex.getCause());
    }
}
