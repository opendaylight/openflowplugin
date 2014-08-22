/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link DeviceDriverProvider device driver provider} implementation,
 * which allows assembly of device driver using off-the-shelf implementations
 * via XML-encoded resources and/or files.
 * <p>
 * The XML configuration is expected to resemble the following format:
 * <pre>&lt;deviceDriver description="..." [origin="..."]&gt;
 *
 *     &lt;{type|abstractType} name="..." [alias="..."] [extends="..."]
 *                          description="..."&gt;
 *         [
 *          &lt;oid&gt;...&lt;/oid&gt;
 *          &lt;oid&gt;...&lt;/oid&gt;
 *          ...
 *         ]
 *         [&lt;fw&gt;...&lt;/fw&gt;]
 *
 *         [&lt;vendor&gt;...&lt;/vendor&gt;]
 *         [&lt;family&gt;...&lt;/family&gt;]
 *         [&lt;product&gt;...&lt;/product&gt;]
 *         [&lt;model&gt;...&lt;/model&gt;]
 *
 *         [&lt;handler class="..." /&gt;]
 *         [&lt;loader class="..." [platform="..."] /&gt;]
 * 
 *         &lt;facet name="..." class="..." /&gt;
 *         [&lt;facet drop="..." /&gt;]
 *
 *         &lt;flag name="..." /&gt;
 *         [&lt;flag drop="..." /&gt;]
 *
 *         [
 *          &lt;typeInfo&gt;
 *             ...
 *          &lt;/typeInfo&gt;
 *
 *          &lt;instanceInfo&gt;
 *             ...
 *          &lt;/instanceInfo&gt;
 *         ]
 *
 *         [
 *          &lt;images path=".../someResourcePath"&gt;
 *             &lt;propertyPageImage&gt;file.jpg&lt;/propertyPageImage&gt;
 *             &lt;topologyMapImage&gt;file.gif&lt;/topologyMapImage&gt;
 *          &lt;/path&gt;
 *         ]
 *
 *         [
 *          &lt;trafficCapabilities&gt;
 *             ...
 *          &lt;/trafficCapabilities&gt;
 *         ]
 *     &lt;/type&gt;
 * 
 *     ...
 * &lt;/deviceDriver&gt;
 * </pre>
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultDeviceDriverProvider extends AbstractDeviceDriverProvider {
    
    private static final String DESCRIPTION = "[@description]";
    private static final String ORIGIN = "[@origin]";
    private static final String TYPE = "type";
    private static final String ABSTRACT_TYPE = "abstractType";

    private static final String TYPE_NAME = "[@name]";
    private static final String TYPE_ALIAS = "[@alias]";
    private static final String TYPE_EXTENDS = "[@extends]";

    private static final String OID = "oid";
    private static final String FW = "fw";
    private static final String VENDOR = "vendor";
    private static final String FAMILY = "family";
    private static final String PRODUCT = "product";
    private static final String MODEL = "model";

    private static final String HANDLER_CLASS = "handler[@class]";
    
    private static final String LOADER = "loader";
    private static final String LOADER_CLASS = "[@class]";
    private static final String LOADER_PLATFORM = "[@platform]";

    private static final String FACET = "facet";
    private static final String FACET_NAME = "[@name]";
    private static final String FACET_CLASS = "[@class]";
    private static final String FACET_DROP = "[@drop]";

    private static final String FLAG = "flag";
    private static final String FLAG_NAME = "[@name]";
    private static final String FLAG_DROP = "[@drop]";

    private static final String IMAGES = "images";
    private static final String PATH = "[@path]";
    private static final String PROP_IMAGE = "propertyPageImage";
    private static final String MAP_IMAGE = "topologyMapImage";

    // === static fields allow unit tests to access them
    static final String EMSG_EXTENDED_TYPE_NOT_BOUND = "Type declares it extends non-bound type: ";
    static final String EMSG_TOO_MANY_IMAGE_BLOCKS = "More than one 'image' clause defined";

    // Context platform name (server/agent/etc.) used for filtering driver
    // configuration relevant for a specific platform.
    private final String platform;

    // Map of alias to typeName bindings.
    private Map<String, String> aliases = new HashMap<String, String>();
    
    // Map of instantiated "abstract" types
    private Map<String, DefaultDeviceType> abstractTypes = 
        new HashMap<String, DefaultDeviceType>();
    
    private ClassLoader classLoader = this.getClass().getClassLoader();

    /**
     * Create a driver provider with the specified platform context.
     * 
     * @param platform platform context name
     */
    public DefaultDeviceDriverProvider(String platform) {
        this.platform = platform;
    }
    
    /**
     * Get the platform context name.
     * 
     * @return platform context name
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Override the default class loader
     * 
     * @param classLoader preferred class loader
     */
    public void setClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new IllegalArgumentException("Null loader not allowed");
        }
        this.classLoader = classLoader;
    }

    /**
     * Resolves the given alias into a typeName. If the resolution fails, the
     * original name is returned.
     * 
     * @param reference type name alias or type name
     * @return type name associated with the reference or the reference itself
     */
    String getTypeName(String reference) {
        String typeName = aliases.get(reference);
        return typeName != null ? typeName : reference;
    }

    /** Returns the type instance (either abstract or concrete) for the given name
     * or alias, if it exists; null otherwise.
     *
     * @param nameOrAlias the name or alias of the required type
     * @return the "abstract" or concrete type
     */
    DefaultDeviceType getTypeForName(String nameOrAlias) {
        DefaultDeviceType t = getAbstractTypes().get(getTypeName(nameOrAlias));
        return t != null ? t : getDeviceType(getTypeName(nameOrAlias));
    }

    /** Predicate that returns true if the specified type is currently registered as an
     * "abstract" type.
     * @param t the type to check
     * @return true if the type is abstract; false otherwise
     */
    boolean isAbstractType(DefaultDeviceType t) {
        return getAbstractTypes().values().contains(t);
    }
    
    /**
     * Parses the XML-encoded device driver definition from the specified file.
     * 
     * @param file device driver definition file
     * @throws DeviceException if problems reading or parsing the file
     */
    public void addDriver(File file) throws DeviceException {
        FileInputStream fis = null;
        try {
            addDriver(fis = new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new DeviceException("Unable to load type definition", e);
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new DeviceException("Unable to close file", e);
                }
        }
    }
    
    /**
     * Parses the XML-encoded data off the specified input stream into a
     * device type instance.
     * 
     * @param in input stream with XML-encoding of device type definition
     * @throws DeviceException if problems parsing the input stream
     */
    public void addDriver(InputStream in) throws DeviceException {
        XMLConfiguration driverDefinition = new XMLConfiguration();
        driverDefinition.setDelimiterParsingDisabled(true);
        try {
            // Load the XML configuration from the input stream.
            driverDefinition.load(in);
            addDriver(driverDefinition);
        } catch (ConfigurationException e) {
            throw new DeviceException("Unable to load type definition", e);
        }
    }
    
    // TODO: Add URL-based addDriver - to support remote update from portal, etc.
    
    /**
     * Parses the device driver definition from the specified hierarchical
     * configuration.
     * 
     * @param driverDefinition device driver definition
     * @throws DeviceException if problems parsing the definition
     */
    @SuppressWarnings("unchecked")
    public void addDriver(HierarchicalConfiguration driverDefinition)
                                                throws DeviceException {
        String description = driverDefinition.getString(DESCRIPTION);
        String origin = driverDefinition.getString(ORIGIN);
        
        List<HierarchicalConfiguration> typeDefinitions =
            driverDefinition.configurationsAt(ABSTRACT_TYPE);
        for (HierarchicalConfiguration typeDefinition : typeDefinitions)
            addType(true, typeDefinition, description, origin);

        typeDefinitions =
            driverDefinition.configurationsAt(TYPE);
        for (HierarchicalConfiguration typeDefinition : typeDefinitions)
            addType(false, typeDefinition, description, origin);
    }

    /**
     * Removes support for the specified device type.
     * 
     * @param typeReference type name or type alias of the device type to be
     *        removed
     */
    public void removeType(String typeReference) {
        removeType(getDeviceType(getTypeName(typeReference)));
    }

    
    /**
     * Processes the supplied hierarchical configuration, that contains device
     * type definition.
     * 
     * @param isAbstract true if the type is an abstract base type
     * @param typeDefinition hierarchical configuration of a type definition
     * @param description a description for the type
     * @param origin an origin of the type (who wrote the device driver) @throws DeviceException if problems
     * @throws DeviceException if issues processing the type definition
     */
    //  TODO: break this method up into smaller chunks
    @SuppressWarnings("unchecked")
    public void addType(boolean isAbstract, HierarchicalConfiguration typeDefinition,
                        String description, String origin) 
                                throws DeviceException {

        // check for extended type as the first thing we do
        DefaultDeviceType parent = null;
        boolean parentIsAbstract = false;
        String parentRef = typeDefinition.getString(TYPE_EXTENDS);
        if (parentRef != null) {
            parent = resolveDeviceType(getTypeName(parentRef));
            parentIsAbstract = abstractTypes.values().contains(parent);
            if (parent == null)
                    throw new DeviceException(EMSG_EXTENDED_TYPE_NOT_BOUND + parentRef);
        }

        // Extract the configuration properties
        String typeName = typeDefinition.getString(TYPE_NAME);
        String typeAlias = typeDefinition.getString(TYPE_ALIAS);
        String typeDescription = typeDefinition.getString(DESCRIPTION);

        String vendor = typeDefinition.getString(VENDOR);
        String family = typeDefinition.getString(FAMILY);
        String product = typeDefinition.getString(PRODUCT);
        String model = typeDefinition.getString(MODEL);
        String fw = typeDefinition.getString(FW);
        String handlerClassName = typeDefinition.getString(HANDLER_CLASS);
        String loaderClassName = getLoaderClassName(typeDefinition);
        
        String typeData = getTypeDataSnippet(typeDefinition);
        String instanceData = getInfoDataSnippet(typeDefinition);

        PresentationResources presRes = getPresentationResources(typeDefinition);

        Set<String> oids = new HashSet<String>();
        List<HierarchicalConfiguration> oidDefs = typeDefinition.configurationsAt(OID);
        for (HierarchicalConfiguration oidDef : oidDefs) {
            oids.add((String) oidDef.getRoot().getValue());
        }

        // If alias has been provided, register it
        if (typeAlias != null)
            aliases.put(typeAlias, typeName);

        // Create a map of facet bindings
        Map<Class<? extends Facet>, Class<? extends AbstractFacet>> facetBindings = 
            new HashMap<Class<? extends Facet>, Class<? extends AbstractFacet>>();

        // Create a set of flags
        Set<String> flagSet = new HashSet<String>();
        
        // If there is a parent type, inherit some properties from it.
        if (parent != null) {
            if (oids.size() == 0) oids.addAll(parent.getOids());

            if (presRes == null) presRes = parent.getPresentationResources();

            if (vendor == null) vendor = parent.getVendor();
            if (family == null) family = parent.getFamily();
            if (product == null) product = parent.getProduct();
            if (model == null) model = parent.getModel();
            if (fw == null) fw = parent.getFw();
            if (handlerClassName == null)
                handlerClassName = parent.getHandlerClass()==null ? null : parent.getHandlerClass().getName();
            if (loaderClassName == null)
                loaderClassName = parent.getLoaderClass()==null ? null : parent.getLoaderClass().getName();

            if (typeData == null) typeData = parent.getTypeData();
            if (instanceData == null) instanceData = parent.getInstanceData();

            // Prime the facet bindings with those of the parent.
            facetBindings.putAll(parent.getFacetBindings(false));
            facetBindings.putAll(parent.getFacetBindings(true));

            // Prime the flags with those of the parent.
            flagSet.addAll(parent.getFlags());
        }

        // TODO: Refactor inheritable data processing
//        DefaultDeviceTypeBuilder builder = new DefaultDeviceTypeBuilder(typeName);
//        processInheritableData(typeDefinition, builder);

        // Process facet definitions
        processFacets(typeDefinition, facetBindings);

        // Process flag definitions
        processFlags(typeDefinition, flagSet);


        // And finally add the type binding.
        Class<? extends DeviceHandler> handlerClass;
        Class<? extends DeviceLoader> loaderClass;

        // Load the handler class if needed
        handlerClass = handlerClassName == null ? null :
                (Class<? extends DeviceHandler>) load(handlerClassName, "handler");

        // Load the loader class if needed
        loaderClass = loaderClassName == null ? null :
                (Class<? extends DeviceLoader>) load(loaderClassName, "loader");

        DefaultDeviceTypeBuilder builder = new DefaultDeviceTypeBuilder(typeName);
        builder.provider(this).parentType(parentIsAbstract ? null : parent)
                .vendor(vendor).family(family).product(product).model(model)
                .oids(oids).fw(fw)
                .typeData(typeData).instanceData(instanceData)
                .presentation(presRes)
                .facetBindings(facetBindings).flags(flagSet)
                .handlerClass(handlerClass).loaderClass(loaderClass)
                .description(makeDescription(description, typeDescription)).origin(origin);

        DefaultDeviceType ddt = builder.build();

        if (isAbstract) {
            abstractTypes.put(typeName, ddt);
        } else {
            addType(ddt);
            // add all oids as aliases
            String fwSuffix = fw==null ? "" : "-"+fw;
            if (oids.size() > 0) {
                for (String oid: oids) {
                    addType(oid+fwSuffix, ddt);
                }
            }
        }
    }

    // TODO: Refactor inheritable data processing
//    private void processInheritableData(HierarchicalConfiguration typeDefinition,
//                                        DefaultDeviceTypeBuilder builder) {
//
//    }

    
    /**
     * Locate the loader class name that applies to the current platform.
     * 
     * @param typeDefinition type definition
     * @return class name of the loader that applies to our platform
     */
    @SuppressWarnings("unchecked")
    private String getLoaderClassName(HierarchicalConfiguration typeDefinition) {
        List<HierarchicalConfiguration> loaderDefinitions = typeDefinition.configurationsAt(LOADER);
        for (HierarchicalConfiguration loaderDefinition : loaderDefinitions) {
            String lpn = loaderDefinition.getString(LOADER_PLATFORM);
            if (lpn != null && lpn.equals(platform))
                return loaderDefinition.getString(LOADER_CLASS);
        }
        // Return null if, we did not find loader relevant to our platform
        return null;  
    }

    // helper method to create an aggregated description from provider container and specific type
    private String makeDescription(String description, String typeDescription) {
        return typeDescription == null ? description : (description + " - " + typeDescription);
    }

    // helper method to check for "abstract" types before asking the provider for "concrete" types
    private DefaultDeviceType resolveDeviceType(String typeName) {
        if (abstractTypes.containsKey(typeName))
            return abstractTypes.get(typeName);
        return getDeviceType(typeName);
    }


    /** Produce an encapsulation of the data declared in the "images" block of the XML.
     *
     * @param typeDefinition device type configuration
     * @return the
     */
    @SuppressWarnings("unchecked")
    private PresentationResources getPresentationResources(HierarchicalConfiguration typeDefinition) {
        List<HierarchicalConfiguration> imageDefinitions = typeDefinition.configurationsAt(IMAGES);
        if (imageDefinitions != null && imageDefinitions.size() > 1)
            throw new DeviceException(EMSG_TOO_MANY_IMAGE_BLOCKS);

        if (imageDefinitions != null && imageDefinitions.size() == 1) {
            HierarchicalConfiguration hc = imageDefinitions.get(0);
            String path = hc.getString(PATH);
            String propImage = hc.getString(PROP_IMAGE);
            String mapImage = hc.getString(MAP_IMAGE);
            return new PresentationResources(path, propImage, mapImage);
        }
        return null;
    }


    /**
     * Produce a string encoding of the optional type data snippet, if one is
     * present in the specified type definition.
     *
     * @param typeDefinition device type configuration
     * @return type data to be stored in the device type instance; may
     *         be null if none is specified in the type definition.
     */
    private String getTypeDataSnippet(HierarchicalConfiguration typeDefinition) {
        // TODO Implement getting the snippet and flattening it into a string.
        return null;
    }

    /**
     * Produce a string encoding of the optional info data snippet, if one is
     * present in the specified type definition.
     *
     * @param typeDefinition device type configuration
     * @return info data to be injected into fresh device info instances; may
     *         be null if none is specified in the type definition.
     */
    private String getInfoDataSnippet(HierarchicalConfiguration typeDefinition) {
        // TODO Implement getting the snippet and flattening it into a string.
        return null;
    }

    /**
     * Processes the facet bindings and overrides specified in given type
     * definition to augment the provided facet bindings map.
     * 
     * @param typeDefinition device type configuration
     * @param facetBindings facet binding map to be augmented
     */
    @SuppressWarnings("unchecked")
    protected void processFacets(HierarchicalConfiguration typeDefinition,
                                 Map<Class<? extends Facet>, Class<? extends AbstractFacet>> facetBindings) {
        // Locate all facet definitions in the type definition.
        List<HierarchicalConfiguration> facetDefinitions = 
            typeDefinition.configurationsAt(FACET);

        for (HierarchicalConfiguration facetDefinition : facetDefinitions) {
            // Extract the facet name, class and the optional drop attributes.
            String facetName = facetDefinition.getString(FACET_NAME);
            String facetClass = facetDefinition.getString(FACET_CLASS);
            String facetDrop = facetDefinition.getString(FACET_DROP);

            // Locate the facet interface class
            if (facetDrop != null) {
                // If the drop attribute indicates so, drop the facet.
                Class<?> facetInterface = load(facetDrop, "facet");
                facetBindings.remove(facetInterface);
            } else {
                // Otherwise, locate the facet implementation and add the
                // interface-to-implementation binding into our map
                Class<?> facetInterface = load(facetName, "facet");
                Class<?> facetImplementation = load(facetClass, "facet implementation");
                facetBindings.put((Class<? extends Facet>) facetInterface,
                                  (Class<? extends AbstractFacet>) facetImplementation);
            }
        }
    }

    /** Processes the flags and augments the provided set.
     *
     * @param typeDefinition device type configuration
     * @param flagSet flag set to be augmented
     */
    @SuppressWarnings("unchecked")
    protected void processFlags(HierarchicalConfiguration typeDefinition,
                                 Set<String> flagSet) {
        // Locate all flag definitions in the type definition.
        List<HierarchicalConfiguration> flagDefinitions =
            typeDefinition.configurationsAt(FLAG);

        for (HierarchicalConfiguration flagDefinition : flagDefinitions) {
            // Extract the flag name, drop attributes.
            String flagName = flagDefinition.getString(FLAG_NAME);
            String flagDrop = flagDefinition.getString(FLAG_DROP);

            if (flagDrop != null) {
                flagSet.remove(flagDrop);
            } else {
                flagSet.add(flagName);
            }
        }
    }



    /**
     * Loads the named class using the appropriate class loader.
     *
     * @param className name of the class to be loaded
     * @param thing friendly label of the thing being loaded
     * @return loaded class
     * @throws DeviceException when the requested class could not be loaded
     */
    private Class<?> load(String className, String thing) throws DeviceException {
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new DeviceException("Unable to locate " + thing + " class ("+className+")", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ""); // remove closing ']'

        sb.append(", abstractTypes=").append(getAbstractTypes().size())
        .append(", aliases=").append(getAliases().size())
        .append("]");
        return sb.toString();
    }

    //==== For Unit Test Support ===

    /** Return reference to our internal aliases map.
     *
     * @return the aliases map
     */
    Map<String, String> getAliases() {
        return aliases;
    }

    /** Return reference to our internal abstract types map.
     *
     * @return the abstract types map
     */
    Map<String, DefaultDeviceType> getAbstractTypes() {
        return abstractTypes;
    }
}
