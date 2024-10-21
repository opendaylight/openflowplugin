/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.clients;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for anonymous complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "scenario"
    })
@XmlRootElement(name = "scenarios")
public class Scenarios {

    @XmlElement(required = true)
    protected List<Scenario> scenario;

    /**
     * Gets the value of the scenario property.
     *
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the scenario property.
     *
     * <p>For example, to add a new item, do as follows:
     * <pre>
     *    getScenario().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list
     * {@link Scenario}
     */
    public List<Scenario> getScenario() {
        if (scenario == null) {
            scenario = new ArrayList<>();
        }
        return this.scenario;
    }

}
