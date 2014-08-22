/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.StringUtils;
import static org.opendaylight.util.StringUtils.EOL;
import org.opendaylight.util.net.IpAddress;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Default {@link DeviceType device type} implementation, which allows
 * assembly of device driver using off-the-shelf facet implementations via
 * XML-encoded resources and/or files.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultDeviceType implements DeviceType {

    private final DeviceDriverProvider provider;
    private final DefaultDeviceType parentType;
    private final Set<DeviceType> childTypes = new HashSet<DeviceType>();
    private final String typeName;

    private final String vendor;
    private final String family;
    private final String product;
    private final String model;
    private final Set<String> oids;
    private final String fw;

    private final String typeData;
    private final String instanceData;
    private final PresentationResources presentationResources;

    private final Class<? extends DeviceHandler> handlerClass;
    private final Class<? extends DeviceLoader> loaderClass;

    private final String description;
    private final String origin;

    private static final Class<?> FACET_ARGS[] =
            new Class<?>[] { DeviceInfo.class };
    private static final Class<?> HANDLER_ARGS[] =
            new Class<?>[] { AbstractDeviceInfo.class, IpAddress.class };
    private static final Class<?> LOADER_ARGS[] =
            new Class<?>[] { AbstractDeviceInfo.class, String.class };


    private final Bindings[] bindings = new Bindings[2];

    private final Set<String> flags = new TreeSet<String>();

    /** Bindings structure */
    private static class Bindings {
        Set<String> names;
        final Map<Class<? extends Facet>, Class<? extends AbstractFacet>> lookup =
                new HashMap<Class<? extends Facet>, Class<? extends AbstractFacet>>();
    }

    /**
     * Creates a base device type associated with the given originating
     * provider.
     *
     * @param provider device info provider from whence we come
     * @param parentType parent type that we extend
     * @param typeName device type name
     * @param vendor the vendor
     * @param family the product family
     * @param product product number string
     * @param model model number string
     * @param oids the set of OIDs
     * @param fw firmware version
     * @param typeData optional device type meta data
     * @param instanceData optional device info initialization (via importData())
     * @param presRes presentation resources (images)
     * @param facetBindings map of facet bindings supported by the device type
     * @param flags a set of flags for this device type
     * @param handlerClass class of the device handler
     * @param loaderClass class of the device loader
     * @param description a description for this device type
     * @param origin the origin of this device type
     */
    public DefaultDeviceType(DeviceDriverProvider provider, DefaultDeviceType parentType,
                             String typeName,
                             String vendor, String family, String product, String model,
                             Set<String> oids, String fw,
                             String typeData,  // TODO : use HierarchicalConfiguration (not Strings) for typeData
                             String instanceData,
                             PresentationResources presRes,
                             Map<Class<? extends Facet>, Class<? extends AbstractFacet>> facetBindings,
                             Set<String> flags,
                             Class<? extends DeviceHandler> handlerClass,
                             Class<? extends DeviceLoader> loaderClass,
                             String description, String origin) {
        this.provider = provider;
        this.parentType = parentType;
        this.typeName = typeName;

        this.vendor = vendor;
        this.family = family;
        this.product = product;
        this.model = model;
        this.oids = oids;
        this.fw = fw;
        this.typeData = typeData;

        // NOTE: this is a string that we will inject with DeviceInfo.importData(String)
        this.instanceData = instanceData;

        this.presentationResources = presRes;
        this.handlerClass = handlerClass;
        this.loaderClass = loaderClass;
        this.description = description;
        this.origin = origin;

        // instantiate our bindings array elements
        bindings[0] = new Bindings();
        bindings[1] = new Bindings();

        if (facetBindings != null) {
            // Register the facet bindings
            for (Map.Entry<Class<? extends Facet>, Class<? extends AbstractFacet>> fb : facetBindings.entrySet())
                addBinding(fb.getKey(), fb.getValue());
        }

        if (flags != null) {
            this.flags.addAll(flags);
        }

    }

    /** Convenience constructor for unit tests that produces a device type that has a name and nothing else.
     *
     * @param provider the provider of this type
     * @param typeName the type name
     */
    protected DefaultDeviceType(DeviceDriverProvider provider, String typeName) {
        this(provider, null, typeName, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public DeviceType getParentType() {
        return parentType;
    }

    @Override
    public Set<? extends DeviceType> getChildTypes() {
        return Collections.unmodifiableSet(childTypes);
    }


    /** Returns the vendor of the device represented by this type.
     *
     * @return the vendor
     */
    public String getVendor() {
        return vendor;
    }

    /** Returns the product family to which this device type belongs.
     *
     * @return the product family
     */
    public String getFamily() {
        return family;
    }

    /** Returns a view of our set of OIDs.
     *
     * @return the oids associated with this device type
     */
    public Set<String> getOids() {
        return Collections.unmodifiableSet(oids);
    }

    /** Returns the firmware version qualifier.
     *
     * @return the firmware version
     */
    public String getFw() {
        return fw;
    }

    /** Returns the meta data associated with the type.
     *
     * @return the type data
     */
    // TODO: change this to HierarchicalConfiguration
    public String getTypeData() {
        return typeData;
    }

    /** Returns the instance data to be stamped onto newly created DeviceInfo instances.
     *
     * @return the instance data
     */
    public String getInstanceData() {
        return instanceData;
    }


    /** Returns the presentation resources associated with this type.
     *
     * @return the presentation resources
     */
    public PresentationResources getPresentationResources() {
        return presentationResources;
    }

    /** Returns a view of our set of flags.
     *
     * @return the set of flags
     */
    public Set<String> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    /**
     * Adds a device type as a direct child of this device type. This method should
     * only be used by the device driver provider to maintain lineage.
     *
     * @param child the child type to add
     * @return true if the type was not already a child
     */
    public boolean addChild(DeviceType child) {
        return childTypes.add(child);
    }

    /**
     * Removes a device type from the list of direct children for this type.
     * This method should only be used by the device driver provider to maintain lineage.
     *
     * @param child the child type to remove
     * @return true if the type was a child
     */
    public boolean removeChild(DeviceType child) {
        return childTypes.remove(child);
    }

    /**
     * Returns true if the specified class implements HandlerFacet
     *
     * @param facetClass class to test
     * @return true if class implements HandlerFacet; false otherwise
     */
    private boolean isHandlerFacet(Class<? extends Facet> facetClass) {
        return HandlerFacet.class.isAssignableFrom(facetClass);
    }

    /**
     * Return an index into the bindings structure.
     *
     * @param isHandler true for the handler index; false for the info index
     * @return the index
     */
    private int index(boolean isHandler) {
        return isHandler ? 1 : 0;
    }

    @Override
    public DeviceDriverProvider getProvider() {
        return provider;
    }

    /**
     * Gets a read-only map of the facet bindings.
     *
     * @param isHandler true for handler bindings; false for info bindings
     * @return map of facet bindings currently registered
     */
    protected Map<Class<? extends Facet>, Class<? extends AbstractFacet>> getFacetBindings(boolean isHandler) {
        return Collections.unmodifiableMap(bindings[index(isHandler)].lookup);
    }

    /**
     * Allows subclasses to register a facet implementation binding.
     *
     * @param facetClass supported facet interface class
     * @param facetImplementationClass corresponding facet interface implementation
     *        class
     */
    protected synchronized void addBinding(Class<? extends Facet> facetClass,
                                           Class<? extends AbstractFacet> facetImplementationClass) {
        bindings[index(isHandlerFacet(facetClass))].lookup.put(facetClass, facetImplementationClass);
    }

    /**
     * Allows subclasses to unregister a facet implementation binding.
     *
     * @param facetClass unsupported facet interface class
     */
    protected synchronized void removeBinding(Class<? extends Facet> facetClass) {
        bindings[index(isHandlerFacet(facetClass))].lookup.remove(facetClass);
    }


    /** get facet class names.
     *
     * @param isHandler true for handler facet class names; false for info 
     *
     *        facet class names
     * @return facet class names
     */
    synchronized Set<String> getFacetClassNames(boolean isHandler) {
        // Lazy initialization.
        Bindings b = bindings[index(isHandler)];

        if (b.names == null) {
            b.names = new HashSet<String>();
            for (Class<? extends Facet> f: getFacetClasses(isHandler))
                b.names.add(f.getName());
        }
        return Collections.unmodifiableSet(b.names);
    }

    /** Returns true if the specified facet is supported.
     *
     * @param isHandler true to test for handler facets; false to test for info facets
     * @param facetClass facet class to test
     * @return true if supported; false otherwise
     */
    boolean isSupported(boolean isHandler, Class<? extends Facet> facetClass) {
        return bindings[index(isHandler)].lookup.keySet().contains(facetClass);
    }

    /** Returns the facet classes for handler or info facets.
     *
     * @param isHandler true for handler facets; false for info facets
     * @return the set of facet classes
     */
    Set<Class<? extends Facet>> getFacetClasses(boolean isHandler) {
        return Collections.unmodifiableSet(bindings[index(isHandler)].lookup.keySet());
    }

    /**
     * Return the requested facet bound to the supplied info context.
     * 
     * @param <T> type of facet 
     * @param isHandler true for a handler facet; false for an info facet
     * @param deviceInfo the device info context
     * @param facetClass the class of facet required @return a facet instance
     * @return the facet instance
     */
    @SuppressWarnings({"unchecked"})
            <T extends Facet> T getFacet(boolean isHandler,
                                         AbstractDeviceInfo deviceInfo, Class<T> facetClass) {
        Class<?> implClass = getFacetBindings(isHandler).get(facetClass);
        if (implClass == null)
            return null;
//            throw new IllegalArgumentException("Facet '" + facetClass.getName() +
//                          "', is not a " + (isHandler ? "handler" : "info") + " facet.");

        return (T) create(implClass, FACET_ARGS, new Object[] { deviceInfo }, "facet");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does not perform any evolution and simply returns
     * back the original device info instance.
     */
    @Override
    public DeviceInfo evolve(DeviceInfo deviceInfo) {
        if (isSupported(true, DeviceIdentityHandler.class)) {
            DeviceIdentityHandler facet =
                    getFacet(true, (AbstractDeviceInfo) deviceInfo,
                             DeviceIdentityHandler.class);
            return facet.evolve();
        }
        return deviceInfo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses reflection to instantiate a new handler for
     * the specified arguments.
     */
    @Override
    public DeviceHandler createHandler(DeviceInfo info, IpAddress ip) {
        return (DeviceHandler) create(handlerClass, HANDLER_ARGS,
                new Object[] { info, ip }, "handler");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses reflection to instantiate a new handler for
     * the specified arguments.
     */
    @Override
    public DeviceLoader createLoader(DeviceInfo info, String uid) {
        return (DeviceLoader) create(loaderClass, LOADER_ARGS,
                new Object[] { info, uid }, "loader");
    }

    @Override
    public String toString() {
        return new StringBuilder().append(product).append("/").append(model)
                .append(": ").append(getDescription())
                .append(": ").append(getOrigin()).toString();
    }

    public String toShortDebugString() {
        StringBuilder sb = new StringBuilder().append(getClass().getName())
                .append(" (").append(getTypeName()).append(") [")
                .append(Integer.toHexString(hashCode())).append("] parent=")
                .append(parentType==null?"null":parentType.getTypeName())
                ;
        return sb.toString();
    }

    /** Produce multi-line debug output for this type instance.
     *
     * @return debug output
     */
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Produce multi-line debug output for this type instance.
     *
     * @param indent number of spaces to indent
     * @return debug output
     */
    public String toDebugString(int indent) {
        String in = StringUtils.spaces(indent);
        String in2 = StringUtils.spaces(indent + 2);
        String in6 = StringUtils.spaces(indent + 6);

        StringBuilder sb = new StringBuilder(in).append(toString()).append(EOL);

        sb.append(in2).append(getClass().getName()).append(" (").append(getTypeName()).append(")").append(EOL);

        sb.append(in2).append("Parent Type: ")
                .append(parentType==null?"null":parentType.toShortDebugString()).append(EOL);

        sb.append(in2).append("Vendor: ").append(vendor).append(EOL);
        sb.append(in2).append("Family: ").append(family).append(EOL);
        sb.append(in2).append("Product: ").append(product).append(EOL);
        sb.append(in2).append("Model: ").append(model).append(EOL);
        sb.append(in2).append("F/W: ").append(fw).append(EOL);

        sb.append(in2).append("OIDs:").append(EOL);
        if (oids != null) {
            for (String oid: oids) {
                sb.append(in6).append(oid).append(EOL);
            }
        }

        sb.append(in2).append("Info Facets:").append(EOL);
        Map<Class<? extends Facet>, Class<? extends AbstractFacet>> facetBindings = getFacetBindings(false);
        for (Map.Entry<Class<? extends Facet>, Class<? extends AbstractFacet>> e : facetBindings.entrySet())
            sb.append(in6).append(e.getKey())
                    .append(" -> ").append(e.getValue()).append(EOL);

        sb.append(in2).append("Handler Facets:").append(EOL);
        facetBindings = getFacetBindings(true);
        for (Map.Entry<Class<? extends Facet>, Class<? extends AbstractFacet>> e : facetBindings.entrySet())
            sb.append(in6).append(e.getKey())
                    .append(" -> ").append(e.getValue()).append(EOL);

        sb.append(in2).append("Flags:").append(EOL);
        for (String f: flags) {
            sb.append(in6).append(f).append(EOL);
        }

        String handlerClassName = handlerClass == null ? "(null)" : handlerClass.getName();
        String loaderClassName = loaderClass == null ? "(null)" : loaderClass.getName();

        sb.append(in2).append("Handler Class: ").append(handlerClassName).append(EOL);
        sb.append(in2).append("Loader Class: ").append(loaderClassName).append(EOL);
        return sb.toString();
    }

    /** Returns the handler class. Support for unit tests.
     *
     * @return the handler class
     */
    Class<? extends DeviceHandler> getHandlerClass() {
        return handlerClass;
    }

    /** Returns the loader class. Support for unit tests.
     *
     * @return the loader class
     */
    Class<? extends DeviceLoader> getLoaderClass() {
        return loaderClass;
    }


    /**
     * Creates an entity of the specified class, using a constructor that 
     * takes the given types of arguments.
     *
     * @param objectClass class of an entity being created
     * @param argsClasses classes of arguments for the expected constructor
     * @param args values of arguments to be supplied to the constructor
     * @param thing string label of the entity being created, 
     *        e.g. "handler", "facet", etc.
     * @return created instance
     */
    static Object create(Class<?> objectClass, Class<?>[] argsClasses,
                         Object[] args, String thing) {
        try {
            Constructor<?> constructor = objectClass.getConstructor(argsClasses);
            return constructor.newInstance(args);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(thing + " class " + objectClass.getName() +
                    " does not provide a constructor with" +
                    args(argsClasses) + " arguments", e);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(thing + " class " + objectClass.getName() +
                    " constructor with " + args(argsClasses) +
                    " arguments is not accessible", e);

        } catch (InstantiationException e) {
            throw new RuntimeException(thing + "class " + objectClass.getName() +
                    " is an abstract class", e);

        } catch (InvocationTargetException e) {
            throw new RuntimeException("construction of the " + thing + " instance " +
                    objectClass.getName() + " failed", e);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("constructor of " + objectClass.getName() +
                    " instance rejected the provided arguments" +
                    Arrays.toString(args), e);
        }
    }

    private static String args(Class<?>[] argsClasses) {
        return Arrays.toString(argsClasses);
    }

}
