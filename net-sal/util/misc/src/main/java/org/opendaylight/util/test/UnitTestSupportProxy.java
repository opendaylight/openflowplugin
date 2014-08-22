/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class can be used by subsystems that want to provide JUnit Support to
 * unit tests that are defined in external packages (i.e.&nbsp;they require
 * public access), and yet prevent the same access from production code.
 * <p>
 * For example, a class that wishes to allow its internal cache to be cleared
 * by unit tests, but to disallow production code the same access, might
 * implement something like this:
 * <pre>
 * public class MyClass {
 *
 *    // singleton pattern with our instance stored in the field INSTANCE ...
 *
 *    /&#42;&#42; Return a support instance. Calling clear() will clear
 *     &#42; the caches.
 *     &#42;/
 *    public static UnitTestSupport getUnitTestSupport() {
 *        return UnitTestSupportProxy.injectProxy(new MyUTS());
 *    }
 *
 *    // this is our support implementation that clears our caches
 *    private static class MyUTS extends UnitTestSupportAdapter {
 *
 *        // the proxy wrapper will ONLY allow this method to
 *        // run if it determines that the caller is a unit test
 *        // method annotated with a "Before/BeforeClass/After/AfterClass"
 *        &#64;Override
 *        public void clear() {
 *            clearCache();
 *        }
 *
 *        // clear the cache on our singleton instance
 *        private void clearCache() {
 *            synchronized (MyClass.INSTANCE.cache) {
 *                MyClass.INSTANCE.cache.clear();
 *            }
 *        }
 *    }
 * }
 * </pre>
 * <p>
 * When a method on the {@link UnitTestSupport} interface is invoked, the proxy
 * examines the calling stack to verify that the caller is a method that has
 * been annotated with any of the following JUnit annotations:
 * <ul>
 * <li> {@code org.junit.BeforeClass} </li>
 * <li> {@code org.junit.AfterClass} </li>
 * <li> {@code org.junit.Before} </li>
 * <li> {@code org.junit.After} </li>
 * </ul>
 * If this is the case, the method on the wrapped implementation is invoked.
 * If this is <u>not</u> the case, a {@code RuntimeException} is thrown.
 *
 * @author Simon Hunt
 */
public class UnitTestSupportProxy implements InvocationHandler {

    private static final ClassLoader CL = UnitTestSupport.class.getClassLoader();
    private static final Class<?>[] PROXIED = new Class[] {UnitTestSupport.class};

    private static final String BEFORE_CLASS = "org.junit.BeforeClass";
    private static final String BEFORE = "org.junit.Before";
    private static final String AFTER_CLASS = "org.junit.AfterClass";
    private static final String AFTER = "org.junit.After";

    private static final String INVOKE = "invoke";
    private static final String CLEAR = "clear";
    private static final String RESET = "reset";
    private static final String SET = "set";

    private static final Set<String> UTS_METHODS = new HashSet<String>(
            Arrays.asList(CLEAR, RESET, SET)
    );

    private Object obj;

    private UnitTestSupportProxy(final Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(final Object proxy, final Method method,
                         final Object[] args) throws Throwable {
        Object result;
        try {
            // need to examine the stack to confirm that the method that
            // called us is declared in a unit test class and annotated with
            // either "org.junit.Before" or "org.junit.BeforeClass"
            try {
                throw new RuntimeException();

            } catch (RuntimeException re) {
                StackTraceElement[] elements = re.getStackTrace();

                // [0] invoke       <-- called on this proxy
                //
                // [1] clear        <-- called on the wrapped UTS impl
                //                       (clear, reset, set, ...)
                // [2] initSuite    <-- the annotated method in the JUnit class
                //                       (can be any name)

                // bail if the top two elements of the stack are
                //  not "invoke" and "clear"
                if (!INVOKE.equals(elements[0].getMethodName()) ||
                        !UTS_METHODS.contains(elements[1].getMethodName())) {
                    throw new IllegalStateException(E_UNEX_METHOD);
                }

                String callerClassName = elements[2].getClassName();
                String callerMethodName = elements[2].getMethodName();

                Class<?> callerClass = Class.forName(callerClassName);
                Method callerMethod = callerClass.getMethod(callerMethodName);

                Annotation[] annots = callerMethod.getAnnotations();
                boolean foundJunitAnnot = false;
                for (Annotation a: annots) {
                    if (BEFORE.equals(a.annotationType().getName()) ||
                            BEFORE_CLASS.equals(a.annotationType().getName()) ||
                            AFTER.equals(a.annotationType().getName()) ||
                            AFTER_CLASS.equals(a.annotationType().getName())) {
                        foundJunitAnnot = true;
                        break;
                    }
                }

                if (!foundJunitAnnot)
                    throw new IllegalStateException(E_UNEX_CALLER);
            }

            // now we can allow the method to be invoked...
            result = method.invoke(obj, args);

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException(E_UNEX_IEX, e);
        }
        return result;
    }

    private static final String E_UNEX_METHOD = "unexpected method name";
    private static final String E_UNEX_CALLER = "unexpected caller";
    private static final String E_UNEX_IEX = "unexpected invocation exception";

    /** This method returns a proxy wrapper for the given UnitTestSupport
     * implementation.
     *
     * @param obj the UnitTestSupport implementation
     * @return the wrapped implementation
     */
    public static UnitTestSupport injectProxy(UnitTestSupport obj) {
        return (UnitTestSupport) Proxy.newProxyInstance(CL, PROXIED,
                new UnitTestSupportProxy(obj));
    }

}
