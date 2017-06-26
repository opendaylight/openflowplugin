package org.opendaylight.openflowjava.tools;

import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;

/**
 *
 * @author Jozef Bacigal
 * Date: 8.3.2016
 */
interface ConnectionToolConfigurationService {


    String OPENFLOWJAVA_TOOLS_SRC_MAIN_RESOURCES = "openflowjava-tools/src/main/resources/";
    String OPENFLOWJAVA_TOOLS_SRC_MAIN_RESOURCES1 = "openflowjava-tools/src/main/resources/";
    String CONFIGURATION_XSD = "configuration.xsd";
    String CONFIGURATION_XML = "configuration.xml";
    String XML_FILE_PATH_WITH_FILE_NAME = OPENFLOWJAVA_TOOLS_SRC_MAIN_RESOURCES + CONFIGURATION_XML;
    String XSD_SCHEMA_PATH_WITH_FILE_NAME = OPENFLOWJAVA_TOOLS_SRC_MAIN_RESOURCES1 + CONFIGURATION_XSD;

    /**
     * Method to save configuration into XML configuration file
     * @param params {@link ConnectionTestTool.Params}
     * @param configurationName {@link String}
     * @throws JAXBException
     * @throws SAXException
     */
    void marshallData(ConnectionTestTool.Params params, String configurationName) throws JAXBException, SAXException;

    /**
     * Method to load data from XML configuration file. Each configuration has a name.
     * @param configurationName {@link String}
     * @return parameters
     * @throws SAXException
     * @throws JAXBException
     */
    ConnectionTestTool.Params unMarshallData(String configurationName) throws SAXException, JAXBException;
}
