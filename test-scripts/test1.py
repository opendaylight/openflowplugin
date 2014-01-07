from xml.dom import minidom

xmldoc=minidom.parse('./xmls/f7.xml')
flow=xmldoc.childNodes[0]
match=flow.childNodes[11]

print match.toxml()

ip_match = match.getElementsByTagName("ip-match")
print 'ip-match:', ip_match


# print "ethernet type", match.getElementsByTagName("type")[0].childNodes[0].data
# print "ethernet cvok", match.getElementsByTagName("cvok")[0].childNodes[0].data
