/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import org.opendaylight.util.cache.Cache;
import org.opendaylight.util.cache.CacheModelProxy;
import org.opendaylight.util.cache.CacheModelProxy.Model;

/**
 * {@link org.opendaylight.util.cache.CacheModelProxy} tests.
 * 
 * @author Fabiel Zuniga
 */
public class CacheModelProxyTest {

    @Test
    public void testPut() {
        @SuppressWarnings("unchecked")
        Cache<String, Object> cacheMock = EasyMock.createMock(Cache.class);
        @SuppressWarnings("unchecked")
        Model<String, Object> modelMock = EasyMock.createMock(Model.class);

        final String key = "key";
        final Object value = new Object();

        cacheMock.put(EasyMock.eq(key), EasyMock.same(value));

        EasyMock.replay(cacheMock);

        Cache<String, Object> cacheModelProxy = new CacheModelProxy<String, Object>(cacheMock, modelMock);
        cacheModelProxy.put(key, value);

        EasyMock.verify(cacheMock);
    }

    @Test
    public void testGetFromCache() {
        @SuppressWarnings("unchecked")
        Cache<String, Object> cacheMock = EasyMock.createMock(Cache.class);
        @SuppressWarnings("unchecked")
        Model<String, Object> modelMock = EasyMock.createMock(Model.class);

        final String key = "key";
        final Object value = new Object();

        EasyMock.expect(cacheMock.get(EasyMock.eq(key))).andReturn(value);

        EasyMock.replay(cacheMock);

        Cache<String, Object> cacheModelProxy = new CacheModelProxy<String, Object>(cacheMock, modelMock);

        Assert.assertSame(value, cacheModelProxy.get(key));

        EasyMock.verify(cacheMock);
    }

    @Test
    public void testGetFromModel() {
        @SuppressWarnings("unchecked")
        Cache<String, Object> cacheMock = EasyMock.createMock(Cache.class);
        @SuppressWarnings("unchecked")
        Model<String, Object> modelMock = EasyMock.createMock(Model.class);

        final String key = "key";
        final Object value = new Object();

        EasyMock.expect(cacheMock.get(EasyMock.eq(key))).andReturn(null);
        EasyMock.expect(modelMock.get(EasyMock.eq(key))).andReturn(value);
        cacheMock.put(EasyMock.eq(key), EasyMock.same(value));

        EasyMock.replay(cacheMock, modelMock);

        Cache<String, Object> cacheModelProxy = new CacheModelProxy<String, Object>(cacheMock, modelMock);

        Assert.assertSame(value, cacheModelProxy.get(key));

        EasyMock.verify(cacheMock, modelMock);
    }

    @Test
    public void testInvalidate() {
        @SuppressWarnings("unchecked")
        Cache<String, Object> cacheMock = EasyMock.createMock(Cache.class);
        @SuppressWarnings("unchecked")
        Model<String, Object> modelMock = EasyMock.createMock(Model.class);

        final String key = "key";

        cacheMock.invalidate(EasyMock.eq(key));

        EasyMock.replay(cacheMock);

        Cache<String, Object> cacheModelProxy = new CacheModelProxy<String, Object>(cacheMock, modelMock);
        cacheModelProxy.invalidate(key);

        EasyMock.verify(cacheMock);
    }

    @Test
    public void testClear() {
        @SuppressWarnings("unchecked")
        Cache<String, Object> cacheMock = EasyMock.createMock(Cache.class);
        @SuppressWarnings("unchecked")
        Model<String, Object> modelMock = EasyMock.createMock(Model.class);

        cacheMock.clear();

        EasyMock.replay(cacheMock);

        Cache<String, Object> cacheModelProxy = new CacheModelProxy<String, Object>(cacheMock, modelMock);
        cacheModelProxy.clear();

        EasyMock.verify(cacheMock);
    }
}
