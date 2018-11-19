/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.clients;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

/**
 * Interface for a scenario service.
 *
 * @author Jozef Bacigal
 */
interface ScenarioService {

    String SIMPLE_CLIENT_SRC_MAIN_RESOURCES = "simple-client/src/main/resources/";
    String SIMPLE_CLIENT_SRC_MAIN_RESOURCES1 = "simple-client/src/main/resources/";
    String SCENARIO_XSD = "scenario.xsd";
    String SCENARIO_XML = "scenario.xml";
    String XSD_SCHEMA_PATH_WITH_FILE_NAME = SIMPLE_CLIENT_SRC_MAIN_RESOURCES1 + SCENARIO_XSD;

    /**
     * Method to load data from XML configuration file. Each configuration has a name.
     * @param scenarioName {@link String}
     * @return scenarios
     */
    Scenario unMarshallData(String scenarioName) throws SAXException, JAXBException;

    List<ClientEvent> getEventsFromScenario(Scenario scenario) throws IOException;

}
