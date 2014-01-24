'''
Created on Jan 24, 2014

@author: vdemcak
'''
import unittest


class OF_TestXmlInputs_Base(unittest.TestCase):
    """
    Base TEST class extends unittest.TestCase and
    it provides possibility to add parameters for 
    all subclasses by call a static constructor:
    
    OF_TestXmlInputs_Base.load_file_name(sub_class_name, param)
    """

    def __init__(self, methodName='runTest', path_to_xml=None):
        """
        private default constructor
        """
        super(OF_TestXmlInputs_Base, self).__init__(methodName)
        self.path_to_xml = path_to_xml

    @staticmethod
    def load_file_name(clazz, path_to_xml=None):
        """
        static constructor for all subclasses with param
        param -> path_to_xml (default None)
        """
        testloader = unittest.TestLoader()
        testnames = testloader.getTestCaseNames(clazz)        
        suite = unittest.TestSuite()
        for name in testnames:
            suite.addTest(clazz(name, path_to_xml=path_to_xml))
        return suite