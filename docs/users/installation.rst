.. _ofp-installation:

OpenFlow Plugin Installation
============================

OpenFlow Plugin installation follows standard OpenDaylight installation procedure
described in install-odl_.

Next sections describe typical OpenFlow user and test features. For a complete list
of available features check the OpenFlow Plugin Release Notes.

Typical User features
---------------------

- **odl-openflowplugin-flow-services-rest**: OF plugin with REST API.
- **odl-openflowplugin-app-table-miss-enforcer**: Adds default flow to controller.
- **odl-openflowplugin-nxm-extensions**: Nicira extensions for OVS.

Typical Test features
---------------------

- **odl-openflowplugin-app-table-miss-enforcer**: Adds default flow to controller.
- **odl-openflowplugin-drop-test**: Test application for pushing flows on packet-in.
- **odl-openflowplugin-app-bulk-o-matic**: Test application for pushing bulk flows.

.. _install-odl: https://docs.opendaylight.org/en/latest/getting-started-guide/installing_opendaylight.html

