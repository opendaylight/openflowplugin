/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * This class captures the presentation resources required for a
 * specific device. It is used by the {@link DefaultDeviceDriverProvider}
 * to encapsulate the data declared in the "&lt;images&gt;" block
 * of an XML description of a device type, and passed to the
 * {@link DefaultDeviceType} instance that is generated.
 *
 * @author Simon Hunt
 */
class PresentationResources {

    private String path;
    private String propImageRef;
    private String mapImageRef;


    /** Constructs a presentation resources encapsulation.
     *
     * @param path the resource path
     * @param propImageRef the name of the "properties tab" image
     * @param mapImageRef the name of the "topology map" image
     */
    public PresentationResources(String path, String propImageRef, String mapImageRef) {
        this.path = path;
        this.propImageRef = propImageRef;
        this.mapImageRef = mapImageRef;
    }

    private static final String SLASH = "/";

    /** Returns the properties tab image resource path.
     *
     * @return the properties tab image resource path
     */
    public String getPropImagePath() {
        return path + SLASH + propImageRef;
    }

    /** Returns the topology map image resource path.
     *
     * @return the topology map image resource path
     */
    public String getMapImagePath() {
        return path + SLASH + mapImageRef;
    }

}
