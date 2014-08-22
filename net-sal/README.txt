Instructions for how to build and launch Net-SAL and OF plugin.

Build
	1. Go into net-sal directory and do a mvn clean install.
	2. mvn javadoc:aggregate to create javadocs and documentation will be located at: base_dir/target/site/apidocs

	Basic Directory Structure		
		apps - examples of apps, forwarding app provides path paving functionality, netvue provides topology web-ui
		commands - karaf shell commands
		drivers - device drivers, currently only contains a base driver, but can be used to support device specific differences between devices/HW
		features - karaf features definition
		net - core net-sal definition and implementations including model, network services, supplier/broker definitions
		of - openflow subsystem
		of-adapter - an adapter layer to allow the openflow subsystem to access some functionality in other layers
		src - high-level javadocs
		suppliers - supplier implementations, currently contains only openflow implementations of link, host, and device discovery
		util - utilities for basic functionality, including eventing and driver framework		
	

Setup
	1. Download and install Karaf.  Has been tested against Karaf version 3.0.1
	2. Modify the org.apache.karaf.features.cfg featuresRepositories and featuresBoot attributes to include the following:
		- featuresRepositories: mvn:org.opendaylight/odl-features/1.0.0-SNAPSHOT/xml/features
		- featuresBoot: odl-commands,odl-app-forwarding,odl-app-netvue
		NOTE: The org.apache.karaf.features.cfg file is located in YOUR_KARAF_FOLDER/etc
	
Run
	1. Run karaf by executing YOUR_KARAF_FOLDER/bin/karaf
	2. Verify Net-SAL and OF features are installed by executing the following commands in the karaf shell:
		- feature:list and check that odl-commands, odl-app-forwarding, and odl-app-netvue are present in the list
	3. Run mininet against the controller
	4. The following commands are available via the shell to access network data:
		- clusters
		- devices
		- interfaces
		- links
		- hosts
		- paths (provide src & dst devices)
		- host-paths (provide src & dest hosts)
	5. odl-app-netvue provides a topology UI which can be accessed at: localhost:8181/netvue