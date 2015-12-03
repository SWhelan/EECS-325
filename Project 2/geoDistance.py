import urllib2
import socket
import json
from math import radians, cos, sin, atan2, sqrt, fabs
import getLocalIP

def get_lat_long(ipAddress):
    # http://stackoverflow.com/questions/645312/what-is-the-quickest-way-to-http-get-in-python
    # According to comments also closes once out of scope
    res = urllib2.urlopen("http://freegeoip.net/json/" + ipAddress).read()    
    data = json.loads(res)
    return radians(data["latitude"]), radians(data["longitude"])
    
def measure_distance(destination):
    lat1, long1 = get_lat_long(getLocalIP.get_local_ip())
    lat2, long2 = get_lat_long(socket.gethostbyname(destination))
    delta_lat = fabs(lat2-lat1)
    delta_long = fabs(long2-long1)
    # The Haversine formula
    # http://www.movable-type.co.uk/scripts/latlong.html
    # "a is the square of half of the chord length between the points"
    # "c is the angular distance in radians"
    a = sin(delta_lat/2)**2 + (cos(lat1) * cos(lat2) * sin(delta_long/2)**2)
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    # Mean Earth Radius in kilometers
    earthRadius = 6371
    # Distance in kilometers
    distance = earthRadius * c
    print "Distance for %s: %d km" % (destination, distance)
    
# Process Targets
    
# Open the targets text file in read mode
f = open('targets.txt', 'r')

# For every target
for line in f:
    # measure the hops/ get info for that target
    if not line.startswith("#"):
        measure_distance(line.split()[0])
# Close the target file
f.close()