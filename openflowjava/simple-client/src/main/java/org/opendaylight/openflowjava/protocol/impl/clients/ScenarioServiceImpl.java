package org.opendaylight.openflowjava.protocol.impl.clients;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Jozef Bacigal
 *         Date: 9.3.2016
 */
public class ScenarioServiceImpl implements ScenarioService {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioServiceImpl.class);

    private String XML_FILE_PATH_WITH_FILE_NAME = SIMPLE_CLIENT_SRC_MAIN_RESOURCES + SCENARIO_XML;

    public ScenarioServiceImpl(String scenarioFile){
        if (null != scenarioFile && !scenarioFile.isEmpty()) {
            this.XML_FILE_PATH_WITH_FILE_NAME = scenarioFile;
        }
    }

    @Override
    public Scenario unMarshallData(String scenarioName) throws SAXException, JAXBException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(XSD_SCHEMA_PATH_WITH_FILE_NAME));
        LOG.debug("Loading schema from: {}", XSD_SCHEMA_PATH_WITH_FILE_NAME);

        JAXBContext jc = JAXBContext.newInstance(Scenarios.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);

        Scenarios scenarios = (Scenarios) unmarshaller.unmarshal(new File(XML_FILE_PATH_WITH_FILE_NAME));
        LOG.debug("Scenarios ({}) are un-marshaled from {}", scenarios.getScenario().size(), XML_FILE_PATH_WITH_FILE_NAME);

        boolean foundConfiguration = false;
        Scenario scenarioType = null;
        for (Scenario scenario : scenarios.getScenario()) {
            if (scenario.getName().equals(scenarioName)) {
                scenarioType = scenario;
                foundConfiguration = true;
            }
        }
        if (!foundConfiguration) {
            LOG.warn("Scenario {} not found.", scenarioName);
        } else {
            LOG.info("Scenario {} found with {} steps.", scenarioName, scenarioType.getStep().size());
        }
        return scenarioType;
    }

    @Override
    public SortedMap<Integer, ClientEvent> getEventsFromScenario(Scenario scenario) throws IOException {
        Preconditions.checkNotNull(scenario, "Scenario name not found. Check XML file, scenario name or directories.");
        SortedMap<Integer, ClientEvent> events = new TreeMap<>();
        Integer counter = 0;
        for (Step step : scenario.getStep()) {
            LOG.debug("Step {}: {}, type {}, bytes {}", step.getOrder(), step.getName(), step.getEvent().value(), step.getBytes().toArray());
            switch (step.getEvent()) {
                case SLEEP_EVENT: events.put(counter++, new SleepEvent(1000)); break;
                case SEND_EVENT: events.put(counter++, new SendEvent(ByteBufUtils.serializeList(step.getBytes()))); break;
                case WAIT_FOR_MESSAGE_EVENT: events.put(counter++, new WaitForMessageEvent(ByteBufUtils.serializeList(step.getBytes()))); break;
            }
        }
        return events;
    }

}
