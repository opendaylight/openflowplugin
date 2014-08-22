/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Rudimentary facility for generic testing of basic bean getter and setter
 * functionality. It uses reflection to locate matching getter/setter pairs in
 * the supplied bean instance.
 * <p>
 * If there are any setters that should not be tested, for whatever reason,
 * they can be annotated with {@link BeanTestIgnore}.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Steve Britt
 */
public class BeanTest {
    
    private static final int BAD_SETTER_MODIFIERS = 
            Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC;

    /**
     * Determines whether the given method fits a profile of a getter.
     * 
     * @param m method to be tested for being a getter
     * @return true if method is a getter
     */
    private static boolean isGetter(Method m) {
        String n = m.getName();
        Class<?> rt = m.getReturnType();
        
        // A method is a getter only if it takes no parameters and does 
        // not return void type.
        if (!n.startsWith("get") ||
                (n.startsWith("get") && Character.isUpperCase(n.charAt(3))))
            return !rt.equals(Void.class) && m.getParameterTypes().length == 0;
        
        // An is* method, is a getter only if it takes no parameters and 
        // returns boolean type.
        if (n.length() > 2 && n.startsWith("is") && 
                Character.isUpperCase(n.charAt(2)))
            return (rt.equals(Boolean.class) || rt.equals(boolean.class)) &&
                        m.getParameterTypes().length == 0;
        
        return false;
    }

    /**
     * Determines whether the given method fits a profile of a setter.
     * 
     * @param m method to be tested for being a setter
     * @return true if method is a setter
     */
    private static boolean isSetter(Method m) {
        // A set* method is a getter only if it takes one parameter and 
        // returns void
        String n = m.getName();
        Class<?> rt = m.getReturnType();
        return n.length() > 3 && n.startsWith("set") && 
                Character.isUpperCase(n.charAt(3)) &&
                rt.equals(void.class) &&
                m.getParameterTypes().length == 1 &&
                !m.getParameterTypes()[0].equals(Void.class) &&
                (m.getModifiers() & BAD_SETTER_MODIFIERS) == 0;
    }
    

    /**
     * Gets the setter method corresponding to the supplied getter.
     * 
     * @param clazz class to search for setter
     * @param getter getter
     * @return corresponding setter or null if no setter method is available
     */
    private static Method getSetter(Class<?> clazz, Method getter) {
        String gn = getter.getName();
        
        // For boolean setters, allow both setIs* and set* variants.
        String[] setterNames;
        if (gn.startsWith("is"))
            setterNames = new String[] { "setI" + gn.substring(1), "set" + gn.substring(2) };
        else if (gn.startsWith("get"))
            setterNames = new String[] { "set" + gn.substring(3) };
        else
            setterNames = new String[] { "set" + Character.toUpperCase(gn.charAt(0)) + gn.substring(1) };
        
        for (String sn : setterNames)        
            for (Method m : clazz.getMethods())
                if (sn.equals(m.getName()) && isSetter(m))
                    return m;
        return null;
    }
    
    public static final String CHARS = "01234567890" + 
            "abcdefghijklmnopqrstuvWXYZ" + 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int CL = CHARS.length();
    
    /**
     * Generate parameter array to be supplied to a setter method.
     * 
     * @param pvc class of random parameter value to be generated
     * @return parameter array with one item of the specified type; null or
     *         empty array if value could not be generated
     */
    private static Object[] generateArgValue(Class<?> pvc) {
        Object v;
        Random r = TestTools.random();
        if (pvc.equals(Integer.class) || pvc.equals(int.class))
            v = r.nextInt();
        else if (pvc.equals(Long.class) || pvc.equals(long.class))
            v = r.nextLong();
        else if (pvc.equals(Float.class)|| pvc.equals(float.class))
            v = r.nextFloat();
        else if (pvc.equals(Double.class)|| pvc.equals(double.class))
            v = r.nextDouble();
        else if (pvc.equals(Short.class)|| pvc.equals(short.class))
            v = (short) r.nextInt();
        else if (pvc.equals(Byte.class)|| pvc.equals(byte.class))
            v = (byte) r.nextInt();
        else if (pvc.equals(Character.class)|| pvc.equals(char.class))
            v = CHARS.charAt(r.nextInt(CL));
        else if (pvc.equals(Boolean.class)|| pvc.equals(boolean.class))
            v = System.currentTimeMillis() % 2 != 0;
        else if (pvc.equals(String.class))
            v = CHARS.substring(r.nextInt(CL)) + CHARS.substring(0, r.nextInt(CL));
        else if (pvc.equals(Date.class))
            v = new Date(System.currentTimeMillis() - r.nextInt());
        else
            return new Object[] {};
        return new Object[] { v };
    }


    /**
     * Test all getter and setter methods.  Uses default configuration and Verbosity.SILENT.
     *
     * @param bean bean to be tested
     * @throws Exception if issues encountered invoking setters or getters
     */
    public static void testGettersAndSetters(Object bean) throws Exception {
        testGettersAndSetters(bean, DEFAULT_CONFIG, Verbosity.SILENT);
    }

    /**
     * Test all getter and setter methods.
     *
     * @param bean bean to be tested
     * @param config test config
     * @throws Exception if issues encountered invoking setters or getters
     */
    public static void testGettersAndSetters(Object bean, Config config) throws Exception {
        testGettersAndSetters(bean, config, Verbosity.SILENT);
    }

    /**
     * Test all getter and setter methods.
     *
     * @param bean bean to be tested
     * @param verbosity how much test output should be generated
     * @throws Exception if issues encountered invoking setters or getters
     * @return test output
     */
    public static String testGettersAndSetters(Object bean, Verbosity verbosity)
            throws Exception {
        return testGettersAndSetters(bean, DEFAULT_CONFIG, verbosity);
    }

    /**
     * Test all getter and setter methods.
     * 
     * @param bean bean to be tested
     * @param config test configuration
     * @param verbosity how much test output should be generated
     * @throws Exception if issues encountered invoking setters or getters
     * @return test output
     */
    // TODO: Add parameter for random value generator bank
    public static String testGettersAndSetters(Object bean, Config config, 
                                               Verbosity verbosity) throws Exception {
        Class<?> bc = bean.getClass();
        Method methods[] = bc.getMethods();
        Verbosity verb = verbosity==null ? Verbosity.SILENT : verbosity;
        final boolean matchedSetters = verb.equals(Verbosity.VERBOSE) || verb.equals(Verbosity.TERSE);
        final boolean ignoredSetters = verb.equals(Verbosity.VERBOSE);
        StringBuffer sb = matchedSetters ? new StringBuffer() : null;

        Set<String> excludedSetterNames = extractExcludedSetterNames(config);

        // Iterate over all methods looking for getters.
        for (Method m : methods) {
            if (!isGetter(m))
                continue;
            
            // Find the corresponding setter or bail.
            Method s = getSetter(bc, m);
            if (s == null)
                continue;

            // Look to see if the setter has been annotated with @BeanTestIgnore
            if (s.getAnnotation(BeanTestIgnore.class) != null) {
                if (ignoredSetters)
                    sb.append("**IGNORED**> ").append(s.getName()).append(TestTools.EOL);
                continue;
            }

            // Look to see if the setter has been identified in the exclusion list.
            if (excludedSetterNames.contains(s.getName())) {
                if (ignoredSetters)
                    sb.append("**IGNORED**> ").append(s.getName()).append(TestTools.EOL);
                continue;
            }

            // Now that we have both the setter and getter methods generate
            // a value using our bank of random value generators. If we can't
            // generate an argument value, bail.
            Object av[] = generateArgValue(m.getReturnType());
            if (av == null || av.length < 1)
                continue;

            if (matchedSetters)
                sb.append("TESTED> ").append(s.getName()).append(TestTools.EOL);

            try {
                s.setAccessible(true);
                s.invoke(bean, av);
                Object v = m.invoke(bean);
                assertEquals("incorrect " + m.getName() + "/" + s.getName(), 
                             av[0], v);
            } catch (RuntimeException e) {
                fail("incorrect " + m.getName() + "/" + s.getName() + 
                     " with " + av[0]);
            }
            
        }
        return sb==null ? "" : sb.toString();
    }

    // The set of strings
    private static Set<String> extractExcludedSetterNames(Config config) {
        return new HashSet<String>(Arrays.asList(config.setterExclusionList()));
    }

    /** An enumeration of the amount of output required. */
    public static enum Verbosity {
        /** No test output (empty string). */
        SILENT,
        /** Output just the names of the setters that were tested. */
        TERSE,
        /** Output the names of both setters that were tested and ignored. */
        VERBOSE,
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * An instance of this will configure the tests. Either create one,
     * passing in an array of exclusions via the constructor, or extend and
     * override as required.
     */
    public static class Config {

        private final String[] exclusions;

        /** Constructs a default config with no method exclusions. */
        public Config() {
            exclusions = EMPTY_STRING_ARRAY;
        }

        /**
         * Constructs a default config with the given setter exclusions.
         * 
         * @param exclusions the list of setter method names to exclude from
         *        the bean test.
         */
        public Config(Set<String> exclusions) {
            this.exclusions = exclusions.toArray(new String[exclusions.size()]);
        }

        /**
         * Returns a list of setter method names that should be excluded from
         * the test. This default implementation returns an empty array.
         * 
         * @return the list of setter method names to exclude
         */
        public String[] setterExclusionList() {
            return Arrays.copyOf(exclusions, exclusions.length);
        }
    }

    /** A default instance of the config. */
    protected static final Config DEFAULT_CONFIG = new Config();
}
