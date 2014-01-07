from xml.dom import minidom

xmldoc=minidom.parse('./xmls/f1.xml')
flow=xmldoc.childNodes[0]
match=flow.childNodes[15]

ipv4dst = match.getElementsByTagName("ipv4-destination")
print ipv4dst[0].childNodes[0].data

cvok = match.getElementsByTagName("cvok")
print cvok

print "ethernet type", match.getElementsByTagName("type")[0].childNodes[0].data
# print "ethernet cvok", match.getElementsByTagName("cvok")[0].childNodes[0].data
