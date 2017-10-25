'''
Created on Jan 24, 2014

@author: vdemcak
'''


class ConvertorTools():
    """
        Tool class contains static conversion method
        for the value conversions
    """
    CONVERTORS = {
        'cookie': hex, 
        'metadata': hex
    }  
    
    @staticmethod
    def base_tag_values_conversion(key, value):
        """
        Check a need to conversion and convert if need
        """
        if value is None : return ''
        else:
            convertor = ConvertorTools.CONVERTORS.get(key, None)
            if convertor is None : return value
            else :
                return convertor(int(value))