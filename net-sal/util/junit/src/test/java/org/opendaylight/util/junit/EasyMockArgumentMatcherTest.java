/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;

/**
 * {@link org.opendaylight.util.junit.EasyMockArgumentMatcher} tests.
 * 
 * @author Fabiel Zuniga
 */
public class EasyMockArgumentMatcherTest {

    @Test
    public void testMatch() {

        Mockable mockable = EasyMock.createMock(Mockable.class);

        IArgumentMatcher matcher = new IArgumentMatcher() {

            @Override
            public void appendTo(StringBuffer buffer) {
                buffer.append("<MyArgumentMatcher>");
            }

            @Override
            public boolean matches(Object arg) {
                if (arg == null) {
                    return false;
                }

                if (!(arg instanceof MyArgument<?>)) {
                    return false;
                }

                @SuppressWarnings("unchecked")
                MyArgument<String> argument = (MyArgument<String>) arg;

                if (argument.getValue() == null) {
                    return false;
                }

                if (!argument.getValue().equals("Hello World")) {
                    return false;
                }

                return true;
            }
        };

        mockable.myMethod(EasyMockArgumentMatcher
            .<MyArgument<String>> match(matcher));
        EasyMock.replay(mockable);
        mockable.myMethod(new MyArgument<String>("Hello World"));
        EasyMock.verify(mockable);
    }

    @Test
    public void testTypeSafeMatch() {
        Mockable mockable = EasyMock.createMock(Mockable.class);

        ArgumentMatcher<MyArgument<String>> matcher = new ArgumentMatcher<MyArgument<String>>("MyArgumentMatcher") {

            @Override
            public boolean verifyMatch(MyArgument<String> argument) {
                return verify(Matchable.<String> valueOf("property",
                                                         "Hello World",
                                                         argument.getValue()));
            }
        };

        mockable.myMethod(EasyMockArgumentMatcher.match(matcher));
        EasyMock.replay(mockable);
        mockable.myMethod(new MyArgument<String>("Hello World"));
        EasyMock.verify(mockable);
    }

    private static interface Mockable {
        public void myMethod(MyArgument<String> argument);
    }

    private static class MyArgument<T> {
        private T value;

        public MyArgument(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }
    }
}
