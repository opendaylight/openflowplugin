/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * This class facilitates the binding of "name" and "description" strings to the constants
 * of given enumeration classes by allowing the registration of an enumeration class and its
 * corresponding resource bundle.
 * <p>
 * Note that it is expected that the resource bundle is in the class path, in the same
 * package as the enum class, and has the same base name as the enum class. For example, for
 * the following enumeration, there would be the properties resource bundle:
 * <pre>
 * com.company.SomeEnum
 * com.company.SomeEnum.properties
 *
 * com.company.SomeClass.SomeInnerEnum
 * com.company.SomeClass$SomeInnerEnum.properties
 * </pre>
 *
 * Moreover, it is required that each constant in the enum has a corresponding pair of
 * entries in the properties file that are the constant name, suffixed with
 * "-name" and "-desc" for the name and description respectively.
 * Thus for the following enum declaration:
 * <pre>
 * public enum Fruit { APPLE, ORANGE, BANANA }
 * </pre>
 * The contents of the resource bundle should look something like
 * <pre>
 * # fruity resources
 * APPLE-name = Apple
 * APPLE-desc = A delicious fruit, often red or green.
 *
 * ORANGE-name = Orange
 * ORANGE-desc = A citrus fruit, round in shape.
 *
 * BANANA-name = Banana
 * BANANA-desc = A yellow fruit that grows in bunches.
 * </pre>
 *
 * Using an enum presentation factory is very simple:
 * <pre>
 * EnumPresentationFactory epf = new EnumPresentationFactory();
 * epf.register(Fruit.class);
 * ...
 * String appleName = epf.getName(Fruit.APPLE);
 * String appleDesc = epf.getDescription(Fruit.APPLE);
 * </pre>
 *
 * @author Simon Hunt
 */
public class EnumPresentationFactory implements LocalizedEnums {

    private final Map<Class<? extends Enum<?>>, BundleWrapper> bindings;

    /** Constructs an enum presentation factory. */
    public EnumPresentationFactory() {
        bindings = new HashMap<Class<? extends Enum<?>>, BundleWrapper>();
    }

    @Override
    public String toString() {
        return new StringBuilder("[EnumPresentationFactory: bindings=")
                .append(getBindingsCount())
                .append("]").toString();
    }

    /** Returns the number of enum class bindings currently registered with this factory.
     *
     * @return the bindings count
     */
    public int getBindingsCount() {
        return bindings.size();
    }

    /** Registers the specified enum class with the factory.
     *
     * @param enumClass the enum class
     * @throws MissingResourceException if no associated resource bundle is found
     */
    public void register(Class<? extends Enum<?>> enumClass) throws MissingResourceException {
        // get the associated bundle
        ResourceBundle bundle = ResourceUtils.getBundledResource(enumClass);
        // wrap it nicely
        BundleWrapper wrapper = new BundleWrapper(bundle);
        // make sure all the required values are there
        new Validator().validate(enumClass, wrapper);
        // stuff it in the map
        bindings.put(enumClass, wrapper);
    }

    /** Returns the name for the specified enumeration constant.
     *
     * @param e the enum constant
     * @return the name
     */
    @Override
    public String getName(Enum<?> e) {
        return getWrapper(e).getName(e);
    }

    /** Returns the description for the specified enumeration constant.
     *
     * @param e the enum constant
     * @return the description
     */
    @Override
    public String getDescription(Enum<?> e) {
        return getWrapper(e).getDescription(e);
    }

    /** private method to retrieve the correct resource bundle.
     *
     * @param e the enum constant
     * @return the wrapped bundle associated with the enum class
     */
    private BundleWrapper getWrapper(Enum<?> e) {
        return bindings.get(e.getClass());
    }

    /** Returns a detailed, multi-line string showing the contents of this factory.
     *
     * @return a detailed debug string
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder("[").append(getClass().getName()).append(":").append(EOL);
        for (Class<? extends Enum<?>> eClass: bindings.keySet()) {
            sb.append("  ").append(eClass.getName()).append(EOL);
            for (Enum<?> e : eClass.getEnumConstants()) {
                sb.append("    ").append(e).append(" --> [").append(getName(e))
                        .append("], [").append(getDescription(e)).append("]").append(EOL);
            }
        }
        sb.append("]").append(EOL);
        return sb.toString();
    }


    /** Private inner class to wrap a resource bundle and expose the contents via name
     * and description getters.
     */
    private static class BundleWrapper {
        private ResourceBundle bundle;

        private BundleWrapper(ResourceBundle bundle) {
            this.bundle = bundle;
        }

        private String getName(Enum<?> e) {
            return bundle.getString(makeKey(e, AbstractResourceValidator.NAME));
        }

        private String getDescription(Enum<?> e) {
            return bundle.getString(makeKey(e, AbstractResourceValidator.DESC));
        }

        private String makeKey(Enum<?> e, String suffix) {
            return new StringBuilder().append(e).append(suffix).toString();
        }
    }

    /** private inner class to validate the resource bundle, ensuring that each
     *  enumeration constant has a name and description.
     */
    private static class Validator extends AbstractResourceValidator {

        private void validate(Class<? extends Enum<?>> enumClass, BundleWrapper bundle) {
            Object[] constants = enumClass.getEnumConstants();

            Enum<?>[] eConstants = (Enum<?>[]) constants;
            for (Enum<?> e: eConstants) {
                final String eName = e.toString();
                try {
                    String name = bundle.getName(e);
                    if (nullOrEmpty(name)) {
                        addEmptyNameError(eName, footnote(enumClass, e));
                    }
                } catch (MissingResourceException mre) {
                    addMissingNameError(eName, footnote(enumClass, e));
                }
                try {
                    String desc = bundle.getDescription(e);
                    if (nullOrEmpty(desc)) {
                        addEmptyDescError(eName, footnote(enumClass, e));
                    }
                } catch (MissingResourceException mre) {
                    addMissingDescError(eName, footnote(enumClass, e));
                }
            }

            if (this.numberOfMessages() > 0)
                addInfo("Validating: " + enumClass.getName());

            throwExceptionIfMessages();
        }

        // create a "footnote" to the error message consisting of the declaring class,
        //  enum class, and enum constant
        private String footnote(Class<?> eClass, Enum<?> eConst) {
            return new StringBuilder().append(eClass.getSimpleName()).append(".").append(eConst).toString();
        }

    }
}
