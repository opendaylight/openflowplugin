'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on Jan 24, 2014

@author: vdemcak
'''

import logging


class FileLoaderTools():
    """
    Tool class provides the static methods for load files 
    with expected format for testing in ODL_TESTs
    """
    log = logging.getLogger( 'FileLoaderTools' )

    @staticmethod
    def load_file_to_string( path_to_file ):
        """
        Primary use for loading the xml files as a string
        """
        output_string = None

        try:
            with open( path_to_file ) as f:
                output_string = f.read()
        except IOError, e:
            FileLoaderTools.log.error( 'cannot find {}: {}'.format( path_to_file, e.strerror ), exc_info = True )

        return output_string

    @staticmethod
    def load_file_to_dict( path_to_file ):
        """
        Primary use for loading the csv files as dictionaries
        """
        dictionary = None

        try :
            with open( path_to_file ) as f:
                dictionary = dict( line.strip().split( ';' ) for line in f
                            if not line.startswith( '#' ) )
        except IOError, e:
            FileLoaderTools.log.error( 'cannot find {}: {}'.format( path_to_file, e.strerror ), exc_info = True )
        return dictionary
