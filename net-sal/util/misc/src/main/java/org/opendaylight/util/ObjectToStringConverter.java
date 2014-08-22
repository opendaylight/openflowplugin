/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Utility class to override {@link Object#toString()}.
 * <p>
 * {@link Object#toString()} should returns a string representation of the
 * object. In general, a string that "textually represents" the object. The
 * result should be a concise but informative representation that is easy for
 * a person to read.
 * <p>
 * Note that {@link Object#toString()} is used for debugging purposes (Like
 * log files), it is not meant to represent a display-able text for the object
 * (for example, to display the information in user interfaces). Java
 * toString() implementations for basic type values should not be taken as an
 * example for composite objects:
 * <p>
 * <table>
 * <tr>
 * <th>Object</th>
 * <th>toString() result</th>
 * <th>Suitable</th>
 * <th>Comments</th>
 * <th>Better alternative</th>
 * </tr>
 * <tr>
 * <td>Integer.valueOf(1)</td>
 * <td>1</td>
 * <td>Yes</td>
 * <td>This is a basic data type and it is easy to see from the toString()
 * result that is a numeric type.</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>new String("Hello World")</td>
 * <td>Hello World</td>
 * <td>Yes</td>
 * <td>This is a basic data type and it is easy to see from the toString()
 * result that is a string.</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>Boolean.TRUE</td>
 * <td>true</td>
 * <td>Yes</td>
 * <td>This is a basic data type and it is easy to see from the toString()
 * result that is a boolean.</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>new Phone(38246188)</td>
 * <td>38246188</td>
 * <td>No</td>
 * <td>This is not a basic type but a value type object, the toString() result
 * is ambiguous with numeric values.</td>
 * <td>Phone[value=38246188]</td>
 * </tr>
 * </table>
 * 
 * @author Fabiel Zuniga
 */
public final class ObjectToStringConverter {

    private ObjectToStringConverter() {

    }

    /**
     * Converts an object to a String to be used as implementation of {@link Object#toString()}.
     *
     * @param subject object to convert to string
     * @param properties object's properties to include
     * @return a string representation of {@code subject} as described in {@link Object#toString()}
     */
    @SafeVarargs
    public static String toString(Object subject, Property<String, ?>... properties) {
        if (subject == null) {
            throw new NullPointerException("obj cannot be null");
        }

        if (properties == null || properties.length <= 0) {
            throw new IllegalArgumentException("properties cannot be empty");
        }

        StringBuilder str = new StringBuilder(64);
        str.append(subject.getClass().getSimpleName());
        str.append('[');
        for (Property<?, ?> property : properties) {
            str.append(property.getIdentity());
            str.append('=');
            str.append(property.getValue());
            str.append(", ");
        }

        // Deletes the last ", "
        str.delete(str.length() - 2, str.length());

        str.append(']');

        return str.toString();
    }
}
