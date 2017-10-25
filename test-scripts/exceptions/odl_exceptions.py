'''
Created on Jan 24, 2014

@author: vdemcak
'''

class OdlValidationException(Exception):
    def __init__(self, *in_args):
        # *in_args is used to get a list of the parameters passed in
        self.args = [a for a in in_args]