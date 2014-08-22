/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Provides useful utilities concerning the run-time environment.
 *
 * @author Simon Hunt
 */
public final class EnvUtils {

    /**
     * Predicate that returns true if the given system property is set true. For example:
     * <pre>
     * if (EnvUtils.isPropertyTrue("foo.bar")) {
     *   ...
     * }
     * </pre>
     * will be true if the option {@code -Dfoo.bar=true} was included in the VM parameters.
     *
     * @param propertyName the name of the system property
     * @return true if the specified property is set true
     */
    public static boolean isPropertyTrue(String propertyName) {
        return Boolean.parseBoolean(System.getProperty(propertyName, "false"));
    }

    /** Predicate that returns true if the <em>"show.output"</em> system property is set.
     *
     * @return true if VM parameter defined: {@code -Dshow.output=true}
     */
    public static boolean showOutput() {
        return isPropertyTrue("show.output");
    }


}
