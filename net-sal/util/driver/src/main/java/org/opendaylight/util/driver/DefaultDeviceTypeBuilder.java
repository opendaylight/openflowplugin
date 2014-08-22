/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import java.util.Map;
import java.util.Set;

/**
 * This class is used to aid construction of {@link DefaultDeviceType} objects. The type name is
 * mandatory and is specified at builder construction. All other fields are optional.
 *
 * @author Simon Hunt
 */
public class DefaultDeviceTypeBuilder {

    private final String typeName;

    private String vendor;
    private String family;
    private String product;
    private String model;
    private Set<String> oids;
    private String fw;
    private String typeData;
    private String instanceData;
    private Map<Class<? extends Facet>, Class<? extends AbstractFacet>> facetBindings;
    private Set<String> flags;

    private DeviceDriverProvider provider;
    private DefaultDeviceType parentType;
    // NOTE: children are added AFTER construction, so we don't need to cater for childTypes
    private Class<? extends DeviceHandler> handlerClass;
    private Class<? extends DeviceLoader> loaderClass;
    private String description;
    private String origin;
    private PresentationResources presRes;


    /** Constructs a builder for a device type of the given name.
     *
     * @param typeName the device type name
     */
    public DefaultDeviceTypeBuilder(String typeName) {
        this.typeName = typeName;
    }

    /** Builds the default device type instance.
     *
     * @return the device type instance
     */
    public DefaultDeviceType build() {
        return new DefaultDeviceType(provider, parentType, typeName, vendor, family, product, model, oids, fw,
                typeData, instanceData, presRes, facetBindings, flags, handlerClass, loaderClass, description, origin);
    }

    public DefaultDeviceTypeBuilder provider(DeviceDriverProvider provider) { this.provider = provider; return this; }
    public DefaultDeviceTypeBuilder parentType(DefaultDeviceType parentType) { this.parentType = parentType; return this; }
    public DefaultDeviceTypeBuilder vendor(String vendor) { this.vendor = vendor; return this; }
    public DefaultDeviceTypeBuilder family(String family) { this.family = family; return this; }
    public DefaultDeviceTypeBuilder product(String product) { this.product = product; return this; }
    public DefaultDeviceTypeBuilder model(String model) { this.model = model; return this; }
    public DefaultDeviceTypeBuilder fw(String fw) { this.fw = fw; return this; }
    public DefaultDeviceTypeBuilder typeData(String typeData) { this.typeData = typeData; return this; }
    public DefaultDeviceTypeBuilder instanceData(String instanceData) { this.instanceData = instanceData; return this; }
    public DefaultDeviceTypeBuilder description(String description) { this.description = description; return this; }
    public DefaultDeviceTypeBuilder origin(String origin) { this.origin = origin; return this; }
    public DefaultDeviceTypeBuilder handlerClass(Class<? extends DeviceHandler> handlerClass) { this.handlerClass = handlerClass; return this; }
    public DefaultDeviceTypeBuilder loaderClass(Class<? extends DeviceLoader> loaderClass) { this.loaderClass = loaderClass; return this; }
    public DefaultDeviceTypeBuilder oids(Set<String> oids) { this.oids = oids; return this; }
    public DefaultDeviceTypeBuilder presentation(PresentationResources presRes) { this.presRes = presRes; return this; }

    public DefaultDeviceTypeBuilder facetBindings(Map<Class<? extends Facet>, Class<? extends AbstractFacet>> facetBindings) {
        this.facetBindings = facetBindings; return this; 
    }
    public DefaultDeviceTypeBuilder flags(Set<String> flags) { this.flags = flags; return this; }
}
