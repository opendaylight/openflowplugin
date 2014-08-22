/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import org.opendaylight.util.junit.TestTools;

/**
 * {@link org.opendaylight.util.cache.SmallDataCache} tests.
 * 
 * @author Fabiel Zuniga
 */
public class SmallDataCacheTest {

    @Test
    public void testPutGetInvalidate() {
        final String key = "my key";

        Cache<String, Object> cache = new SmallDataCache<String, Object>();

        Assert.assertNull(cache.get(key));

        Object object = new Object();
        cache.put(key, object);
        Assert.assertSame(object, cache.get(key));

        cache.invalidate(key);
        Assert.assertNull(cache.get(key));
    }

    @Test
    public void testClear() {
        final String key1 = "my key 1";
        final String key2 = "my key 2";

        Cache<String, Object> cache = new SmallDataCache<String, Object>();

        cache.put(key1, new Object());
        cache.put(key2, new Object());

        cache.clear();

        Assert.assertNull(cache.get(key1));
        Assert.assertNull(cache.get(key2));
    }

    @Test
    public void testGetContent() {
        final String key1 = "my key 1";
        final String key2 = "my key 2";
        final Object value1 = new Object();
        final Object value2 = new Object();

        SmallDataCache<String, Object> cache = new SmallDataCache<String, Object>();

        cache.put(key1, value1);
        cache.put(key2, value2);

        Collection<Object> content = cache.getContent();

        Assert.assertNotNull(content);
        Assert.assertEquals(2, content.size());
        Assert.assertTrue(content.contains(value1));
        Assert.assertTrue(content.contains(value2));
    }

    @Test
    public void testPutLock() throws Exception {
        ReadWriteLock readWriteLockMock = EasyMock.createMock(ReadWriteLock.class);
        Lock lockMock = EasyMock.createMock(Lock.class);

        EasyMock.expect(readWriteLockMock.writeLock()).andReturn(lockMock);
        lockMock.lock();
        lockMock.unlock();

        EasyMock.replay(readWriteLockMock, lockMock);

        SmallDataCache<String, Object> cache = new SmallDataCache<String, Object>();
        TestTools.setPrivateField("readWriteLock", readWriteLockMock, cache);

        cache.put("key", new Object());

        EasyMock.verify(readWriteLockMock, lockMock);
    }

    @Test
    public void testGutLock() throws Exception {
        ReadWriteLock readWriteLockMock = EasyMock
            .createMock(ReadWriteLock.class);
        Lock lockMock = EasyMock.createMock(Lock.class);

        EasyMock.expect(readWriteLockMock.readLock()).andReturn(lockMock);
        lockMock.lock();
        lockMock.unlock();

        EasyMock.replay(readWriteLockMock, lockMock);

        SmallDataCache<String, Object> cache = new SmallDataCache<String, Object>();
        TestTools.setPrivateField("readWriteLock", readWriteLockMock, cache);

        cache.get("key");

        EasyMock.verify(readWriteLockMock, lockMock);
    }

    @Test
    public void testInvalidateLock() throws Exception {
        ReadWriteLock readWriteLockMock = EasyMock
            .createMock(ReadWriteLock.class);
        Lock lockMock = EasyMock.createMock(Lock.class);

        EasyMock.expect(readWriteLockMock.writeLock()).andReturn(lockMock);
        lockMock.lock();
        lockMock.unlock();

        EasyMock.replay(readWriteLockMock, lockMock);

        SmallDataCache<String, Object> cache = new SmallDataCache<String, Object>();
        TestTools.setPrivateField("readWriteLock", readWriteLockMock, cache);

        cache.invalidate("key");

        EasyMock.verify(readWriteLockMock, lockMock);
    }

    @Test
    public void testClearLock() throws Exception {
        ReadWriteLock readWriteLockMock = EasyMock
            .createMock(ReadWriteLock.class);
        Lock lockMock = EasyMock.createMock(Lock.class);

        EasyMock.expect(readWriteLockMock.writeLock()).andReturn(lockMock);
        lockMock.lock();
        lockMock.unlock();

        EasyMock.replay(readWriteLockMock, lockMock);

        SmallDataCache<String, Object> cache = new SmallDataCache<String, Object>();
        TestTools.setPrivateField("readWriteLock", readWriteLockMock, cache);

        cache.clear();

        EasyMock.verify(readWriteLockMock, lockMock);
    }

    @Test
    public void testGetContentLock() throws Exception {
        ReadWriteLock readWriteLockMock = EasyMock
            .createMock(ReadWriteLock.class);
        Lock lockMock = EasyMock.createMock(Lock.class);

        EasyMock.expect(readWriteLockMock.readLock()).andReturn(lockMock);
        lockMock.lock();
        lockMock.unlock();

        EasyMock.replay(readWriteLockMock, lockMock);

        SmallDataCache<String, Object> cache = new SmallDataCache<String, Object>();
        TestTools.setPrivateField("readWriteLock", readWriteLockMock, cache);

        cache.getContent();

        EasyMock.verify(readWriteLockMock, lockMock);
    }
}
