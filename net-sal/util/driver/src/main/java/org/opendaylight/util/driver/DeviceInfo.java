/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * Collection of device attributes; both specific to a device instance and
 * more generally of the device type. Note that the underlying device type can
 * be refined over time as more is learned about the device.
 * 
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface DeviceInfo extends FacetProvider {

    /**
     * Returns the name of the device type backing this instance. This method
     * should delegate to the context method of the same name.
     * 
     * @return the device type name
     */
    public String getTypeName();

    /**
     * A request to "evolve" this device info instance to reflect current
     * information that may have been collected. Note that the returned
     * instance is not guaranteed to be 'this' instance.
     * <p>
     * If the device info contains additional information to warrant further
     * evolution then an altered or a different device info instance will be
     * returned, whose generation number will be greater than the original
     * device info generation number. Otherwise, the same device info
     * instance may be returned with its generation number unchanged to
     * indicate that no evolution occurred.
     * <p>
     * A recommended pattern of use is
     * 
     * <pre>
     * DeviceInfo info = ...
     * int originalGeneration = info.getGeneration();
     * ...
     * // Attempt to evolve the underlying device type when we think more 
     * // information about the underlying device may available to to 
     * // further narrow the specific of the device type.
     * info = info.evolve();
     * 
     * if (originalGeneration < info.getGeneration())
     *     // Device info has evolved
     *     ...
     * else
     *     // Device info did not evolve any further
     *     ...
     * 
     * </pre>
     * 
     * @return an evolved info instance
     */
    public DeviceInfo evolve();

    /**
     * Get the generation number of the device info. It represents the number
     * of times the device info has undergone type evolution since its
     * original creation via one of the {@code DeviceInfoProvider.create()}
     * methods.
     * 
     * @return generation number of the device info
     */
    public int getGeneration();
    

    /**
     * Exports the contents of the device info into a string form.
     * 
     * @return string containing an export encoding of the internal data,
     *         suitable to be re-imported via {@link #importData(String)}
     */
    public String exportData();
    
    /**
     * Imports the contents of the device info from the specified
     * export-encoded string.
     * <p>
     * There is no mandate to be able to import data exported from a device 
     * info of a different type.
     * 
     * @param data string previously encoded via {@link #exportData()}
     * @return true if the import was successful; false otherwise
     */
    public boolean importData(String data);

}
