package org.opendaylight.openflowjava.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Jozef Bacigal
 *         Date: 8.3.2016
 */
public class ConnectionToolConfigurationServiceImpl implements ConnectionToolConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionToolConfigurationServiceImpl.class);

    @Override
    public void marshallData(ConnectionTestTool.Params params, String configurationName) throws JAXBException, SAXException {
        File file = new File(XML_FILE_PATH_WITH_FILE_NAME);
        LOG.info("Marshaling configuration data to: {}", XML_FILE_PATH_WITH_FILE_NAME);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(XSD_SCHEMA_PATH_WITH_FILE_NAME));
        LOG.info("with schema: {}", XSD_SCHEMA_PATH_WITH_FILE_NAME);

        JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, CONFIGURATION_XSD);

        jaxbMarshaller.setSchema(schema);

        Configurations configurations = new Configurations();

        List<ConfigurationType> configurationTypes = configurations.getConfiguration();

        for (ConfigurationType configurationType : this.getSavedConfigurations()) {
            configurationTypes.add(configurationType);
        }

        ConfigurationType configurationType = new ConfigurationType();
        configurationType.name = configurationName;

        configurationType.controllerIp = params.controllerIP;
        configurationType.devicesCount = BigInteger.valueOf(params.deviceCount);
        configurationType.freeze = BigInteger.valueOf(params.freeze);
        configurationType.port = BigInteger.valueOf(params.port);
        configurationType.sleep = params.sleep;
        configurationType.ssl = params.ssl;
        configurationType.threads = BigInteger.valueOf(params.threads);
        configurationType.timeout = BigInteger.valueOf(params.timeout);

        configurationTypes.add(configurationType);

        configurations.setConfiguration(configurationTypes);
        jaxbMarshaller.marshal(configurations, file);

    }

    @Override
    public ConnectionTestTool.Params unMarshallData(String configurationName) throws SAXException, JAXBException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(XSD_SCHEMA_PATH_WITH_FILE_NAME));
        LOG.debug("Loading schema from: {}", XSD_SCHEMA_PATH_WITH_FILE_NAME);

        JAXBContext jc = JAXBContext.newInstance(Configurations.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);

        Configurations configurations = (Configurations) unmarshaller.unmarshal(new File(XML_FILE_PATH_WITH_FILE_NAME));
        LOG.debug("Configurations ({}) are un-marshaled from {}", configurations.getConfiguration().size(), XML_FILE_PATH_WITH_FILE_NAME);

        boolean foundConfiguration = false;
        ConfigurationType configuration = null;
        for (ConfigurationType configurationType : configurations.getConfiguration()) {
            if (configurationType.getName().equals(configurationName)) {
                configuration = configurationType;
                foundConfiguration = true;
            }
        }
        ConnectionTestTool.Params params = null;
        if (foundConfiguration) {
            LOG.info("Configuration {} found, loading parameters.", configurationName);
            params = new ConnectionTestTool.Params();
            params.controllerIP = configuration.getControllerIp();
            params.deviceCount = configuration.getDevicesCount().intValue();
            params.freeze = configuration.getFreeze().intValue();
            params.port = configuration.getPort().intValue();
            params.sleep = configuration.getSleep();
            params.ssl = configuration.isSsl();
            params.threads = configuration.getThreads().intValue();
            params.timeout = configuration.getTimeout().intValue();
        } else {
            LOG.warn("Configuration {} not found. Using default parameters.", configurationName);
        }

        return params;
    }

    private List<ConfigurationType> getSavedConfigurations() throws SAXException, JAXBException{

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(XSD_SCHEMA_PATH_WITH_FILE_NAME));

        JAXBContext jc = JAXBContext.newInstance(Configurations.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);

        Configurations configurations = (Configurations) unmarshaller.unmarshal(new File(XML_FILE_PATH_WITH_FILE_NAME));

        return configurations.getConfiguration();

    }
}
