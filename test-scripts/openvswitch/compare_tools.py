'''
Created on Jan 24, 2014

@author: vdemcak
'''

import logging


class Comparator():

    log = logging.getLogger('Comparator')

    @staticmethod
    def compare_results(actual, expected):
        #print 'ACT: ', actual
        #print 'EXP: ', expected

        list_unused = list(set(actual.keys()) - set(expected.keys()))
        if len(list_unused) > 0:
            Comparator.log.info('unchecked tags: {}'.format(list_unused))

        list_duration = ['duration','hard_timeout','idle_timeout']

        Comparator.test_duration(actual, expected)

        # compare results from actual flow (mn dump result) and expepected flow (stored result)
        for k in expected.keys():
            if k not in list_duration:
                assert k in actual, 'cannot find key {} in flow {}'.format(k, actual)
                assert actual[k] == expected[k], 'key:{}, actual:{} != expected:{}'.format(k, actual[k], expected[k])

    @staticmethod
    def test_duration(actual, expected):
        duration_key = 'duration'
        hard_to_key = 'hard_timeout'

        if duration_key in expected.keys():
            assert duration_key in actual.keys(), '{} is not set in {}'.format(duration_key, actual)
            try:
                duration = float(expected['duration'].rstrip('s'))
                hard_timeout = int(actual['hard_timeout'])
                assert duration <= hard_timeout, 'duration is higher than hard_timeout, {} > {}'.format(duration, hard_timeout)
            except KeyError as e:
                Comparator.log.warning('cannot find keys to test duration tag', exc_info=True)
        else:
            # VD - what should we do in this case
            pass