#-------------------------------------------------------------------------------
#   Copyright 2004 Hewlett Packard Co., All Rights Reserved.
#-------------------------------------------------------------------------------
#
#   XML Syntax Definition Tester
#
#   written by Thomas Vachuska
#   September 10, 2004
#                                                                    -- Doobs --
#-------------------------------------------------------------------------------
use strict;

#   Insist on JAVA_HOME being defined.
my $JAVA_HOME = $ENV{JAVA_HOME};
die "\nJAVA_HOME *MUST* be set!\n" if $JAVA_HOME eq "";

#   Insist on SRC_ROOT being defined.
my $SRC_ROOT = $ENV{SRC_ROOT};
die "\nSRC_ROOT *MUST* be set!\n" if $SRC_ROOT eq "";

#   Setup the class name and class-path.
my $CLASS="org.opendaylight.util.syntax.fixtures.SyntaxTester";
my $UTILS="$SRC_ROOT/projects/hp.util";

my $JARS="$ENV{CLASSPATH};$SRC_ROOT/lib/xercesImpl.jar";
$JARS="$JARS;$SRC_ROOT/ant/lib/junit.jar";
$JARS="$JARS;$UTILS/target/test/hp.util.test.jar;$UTILS/target/lib/hp.util.jar";
$JARS="$JARS;$SRC_ROOT/projects/legacycli/target/lib/legacycli.jar";

#   Fetch some other env. variables.
my $SYNTAX_SCHEMA = $ENV{SYNTAX_SCHEMA}; 
my $SYNTAX_LOCATION = $ENV{SYNTAX_LOCATION}; 
my $LOCALE = $ENV{LOCALE}; 

#   Point to the XML schema under the sand-box if need be.,
$SYNTAX_SCHEMA = "$UTILS/src/com/hp/util/syntax/syntax-schema.xsd" 
    if $SYNTAX_SCHEMA eq "";

#   Build up the command-line.
my @ARGS;
push @ARGS, ("$JAVA_HOME/bin/java", "-classpath", $JARS);
push @ARGS, "-DsyntaxSchema=$SYNTAX_SCHEMA";
push @ARGS, "-Dlocale=$LOCALE" if $LOCALE ne "";
push @ARGS, "-DsyntaxLocation=$SYNTAX_LOCATION" if $SYNTAX_LOCATION ne "";
push @ARGS, $CLASS;
push @ARGS, @ARGV;

#   Run it and return appropriate exit code.
my $returnValue = system @ARGS;
exit $returnValue eq 0 ? 0 : 1;

