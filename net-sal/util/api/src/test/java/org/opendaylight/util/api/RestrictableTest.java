/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import org.opendaylight.util.junit.ThrowableTester;
import org.opendaylight.util.junit.ThrowableTester.Instruction;

/**
 * {@link Restrictable} tests.
 * 
 * @author Fabiel Zuniga
 */
public class RestrictableTest {

    @Test
    public void testConstruction() {
        MyPublicService publicService = EasyMock.createMock(MyPublicService.class);
        MyRestrictedService restrictedService = EasyMock.createMock(MyRestrictedService.class);

        Restrictable<MyPublicService, MyRestrictedService> restrictable = Restrictable.create(publicService,
                restrictedService);
        Assert.assertSame(publicService, restrictable.getPublic());
        Assert.assertSame(restrictedService, restrictable.getRestricted());
    }

    @Test
    public void testInvalidConstruction() {
        final MyPublicService validPublicService = EasyMock.createMock(MyPublicService.class);
        final MyPublicService invalidPublicService = null;

        final MyRestrictedService validRestrictedService = EasyMock.createMock(MyRestrictedService.class);
        final MyRestrictedService invalidRestrictedService = null;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Restrictable.create(invalidPublicService, validRestrictedService);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Restrictable.create(validPublicService, invalidRestrictedService);
            }
        });
    }

    private static interface MyPublicService {

    }

    private static interface MyRestrictedService {

    }
}
