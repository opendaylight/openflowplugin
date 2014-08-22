<?xml version="1.0"?>

<suite name="mx-test" directory="src/test/resources/org/opendaylight/util/syntax/">

	<!-- terse test specifications -->
    <test args="mxagentconfig" 
          syntax="mx.mxagentconfig-g"/>
    <test args="mXagentconfig" 
          error="0"/>
          
    <test args="mxagentconfig -a -n foo"
          syntax="mx.mxagentconfig-a"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke"/>
    <test args="mxagentconfig -a -n foo -n bar"
          syntax="mx.mxagentconfig-a"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke"/>
    <test args="mxagentconfig -a -n foo -n"
          error="5"/>
   
    <test args="mxagentconfig -a -n foo -n bar"
          syntax="mx.mxagentconfig-a"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
        <param name="mx.hostname#0"/>
        <param name="mx.hostname#1"/>
    </test>

    <test args="mxagentconfig -a -n foo -n bar"
          syntax="mx.mxagentconfig-a"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
        <param name="mx.hostname#1" value="bar"/>
        <param name="mx.hostname#0" value="foo"/>
    </test>

    <test args="mxagentconfig -a -n foo -n bar -p pass"
          syntax="mx.mxagentconfig-ap"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
        <param name="mx.hostname#1" value="bar"/>
        <param name="mx.hostname#0" value="foo"/>
        <param name="mx.password" value="pass"/>
    </test>

    <test args="mxagentconfig -a -n foo -n bar -f filespec"
          syntax="mx.mxagentconfig-af"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
        <param name="mx.hostname#1" value="bar"/>
        <param name="mx.hostname#0" value="foo"/>
        <param name="mx.filespec" value="filespec"/>
    </test>

    <test args="mxagentconfig -f filespec1 -O filespec2"
          syntax="mx.mxagentconfig-t"
          actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
        <param name="mx.mxagentconfig-t.filespec1" value="filespec1"/>
        <param name="mx.mxagentconfig-t.filespec2" value="filespec2"/>
    </test>


	<test args="mxexec -t tool -A foo -A bar"
	      syntax="mx.mxexec"
	      actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
	    <param name="mx.toolname" value="tool"/>
	    <param name="mx.argOption.argValue#0" value="foo"/>
	    <param name="mx.argOption.argValue#1" value="bar"/>
	</test>

	<test args="mxexec -t tool -A foo bar"
	      syntax="mx.mxexec"
	      actionName="org.opendaylight.mx.foo.LegacyHandler:invoke">
	    <param name="mx.toolname" value="tool"/>
	    <param name="mx.argOption.argValue#0" value="foo"/>
	    <param name="mx.argOption.argValue#1" value="bar"/>
	</test>




    <!-- verbose test specifications -->
    <test name="a1" args="mxagentconfig">
        <expect syntax="mx.mxagentconfig-g"/>
    </test>

    <test name="a2" args="mXagentconfig">
        <expect error="0"/>
    </test>

    <test name="a3" args="mxagentconfig -a -n foo">
        <expect syntax="mx.mxagentconfig-a"
        		actionName="org.opendaylight.mx.foo.LegacyHandler:invoke"/>
    </test>

    <test name="a4" args="mxagentconfig -a -n foo -n bar">
        <expect syntax="mx.mxagentconfig-a"
        		actionName="org.opendaylight.mx.foo.LegacyHandler:invoke"/>
    </test>

</suite> 
