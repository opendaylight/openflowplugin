/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A collection of useful utilities revolving around Resource Bundle usage.
 *
 * @author Simon Hunt
 */
public final class ResourceUtils {

    // no instantiation
    private ResourceUtils() { }

    /**
     * This method takes a class and returns a {@link ResourceBundle} which is
     * loaded from a properties file with the same base name as the class.
     * Note that both the class and the properties file(s) need to be in the
     * same jar file.
     * <p>
     * For example, supposing the jar file contains:
     *
     * <pre>
     * com/hp/util/example/SomeObject.class
     * com/hp/util/example/SomeObject.properties
     * </pre>
     *
     * Then, to correctly load the resource bundle associated with
     * <code>SomeObject</code>, call:
     * <pre>
     * ResourceBundle res = ResourceUtils.getBundledResource(SomeObject.class);
     * </pre>
     *
     * Note that no error is thrown if the properties file does not exist.
     * This condition will not become apparent until you try and access a
     * property from the bundle, at which time a
     * {@link java.util.MissingResourceException} will be thrown.
     *
     * @param c the class
     * @return the associated resource bundle
     */
    public static ResourceBundle getBundledResource(Class<?> c) {
        return ResourceBundle.getBundle(c.getName(), Locale.getDefault(),
                c.getClassLoader());
    }


    private static final String DOT = ".";

    /** This method returns a {@link ResourceBundle} which is loaded from
     * a properties file with the specified base name from the same package
     * as the specified class.
     * Note that both the class and the properties file(s) need to be in the
     * same jar file.
     * <p>
     * For example, supposing the jar file contains:
     *
     * <pre>
     * com/hp/nis/util/example/SomeObject.class
     * com/hp/nis/util/example/DisplayStrings.properties
     * </pre>
     *
     * Then, to correctly load the resource bundle call:
     * <pre>
     * ResourceBundle res =
     *     ResourceUtils.getBundledResource(SomeObject.class, "DisplayStrings");
     * </pre>
     *
     * Note that no error is thrown if the properties file does not exist.
     * This condition will not become apparent until you try and access a
     * property from the bundle, at which time a
     * {@link java.util.MissingResourceException} will be thrown.
     *
     * @param c the class requesting the bundle
     * @param baseName the base name of the resource bundle
     * @return the associated resource bundle
     */
    public static ResourceBundle getBundledResource(Class<?> c, String baseName) {
        String className = c.getName();
        StringBuilder sb = new StringBuilder();
        int dot = className.lastIndexOf(DOT);
        sb.append(className.substring(0, dot));
        sb.append(DOT).append(baseName);
        return ResourceBundle.getBundle(sb.toString(), Locale.getDefault(),
                c.getClassLoader());
    }

}
