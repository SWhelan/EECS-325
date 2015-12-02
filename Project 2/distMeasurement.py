# Sarah Whelan
# slw96
# Introduction to Computer Networks
# Project 2
# December 2, 2015

import socket
import sys
import time

def get_local_ip():
    # Second answer:
    # http://stackoverflow.com/questions/166506/finding-local-ip-addresses-using-pythons-stdlib
    # but that answer probably got it from here:
    # http://zeth.net/archive/2007/11/24/how-to-find-out-ip-address-in-python/
    # which I believe indicates someone posted it on a mailing list
    # Either way I ended up trying the first answer from first link and only getting 127.0.0.1
    
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("case.edu", 80))
    local_ip = s.getsockname()[0]
    s.close()
    return local_ip
    
def make_string_ip(ip):
    return str(ord(ip[0])) + "." + str(ord(ip[1])) + "." + str(ord(ip[2])) + "." + str(ord(ip[3]))
    
def do_bytes_match_string(bytes, string):
    return bytes[:].decode("ascii") == string

def measure_info(destination):
    # The official traceroute port should return port unreachable ICMP packets/messages
    # Other ports would work as well ie 49152 through 65535 and really any normally unused port
    # https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers#Dynamic.2C_private_or_ephemeral_ports
    port = 33434    
    # Get the expected protocols for the sockets
    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    # A known ttl 
    ttl = 32
    # A known message
    message = "abcdefgh"
    # Get the destination IP - non-deterministic gets first DNS response
    destination_ip = socket.gethostbyname(destination)
    
    # Create sockets 
    
    # One to send a packet with a known ttl 
    sender = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)
    
    # Set the TTL explicitly so that we know what it was originally
    sender.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)
    
    # One to receive an ICMP packet ideally with code 3
    receiver = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
    
    # Set the blocking receiver to time out if no response
    receiver.settimeout(3)
    
    # Start a timer to get RTT
    start_time = time.clock()
    
    # Send the packet to the destination
    sender.sendto(message, (destination_ip, port))
    
    # Attempt to get the data from the receiver by polling
    try:
        # Get data from the receiver = the argument is the buffer size
        data, addr = receiver.recvfrom(2048)
        # Stop the timer
        end_time = time.clock()
    except socket.error:
        print "%s did not respond or the packet was lost." % (destination)        
        return
    finally:
        # Close the sockets
        sender.close()
        receiver.close()
        
    # Process the data and determine how many hops the packet travelled
    
    # The first 20 bytes of the response 0 - 19 is the containing IPv4 header
    # The next 8 are the ICMP response 20-27
    # Specifically byte 20 is the type should be 3 for destination unreachable
    # Specifically byte 21 is the code should be 3 for port unreachable
    # The next 20 28 - 47 bytes are the IPv4 headers sent earlier
    # Specifically byte 36 is the ttl field indicating how many steps it took to get to the destination
    # The last 8 48 - 55 are the data in the packet sent
    
    icmp_type = ord(data[20])
    icmp_code = ord(data[21])
    new_ttl = ord(data[36])
    
    response_source_ip = make_string_ip(data[40:44])
    response_destination_ip = make_string_ip(data[44:48])  
    
    icmp_source_ip = make_string_ip(data[12:16])
    icmp_destination_ip = make_string_ip(data[16:20])
 
    if  (   
            # The message is not ICMP destination / port unreachable
            icmp_type != 3 or icmp_code != 3 or
            # The source IP on the IPv4 packet within the ICMP does not match our IP
            response_source_ip  != get_local_ip() or
            # The destination IP on the IPv4 packet within the ICMP does not match the destination
            response_destination_ip != destination_ip or
            # The source IP on the ICMP packet is not the destination
            icmp_source_ip != destination_ip or
            # The destination IP on the ICMP packet is not this address
            icmp_destination_ip != get_local_ip() or
            # The ICMP packet returned my message and it doesn't match
            (len(data) == 64 and not do_bytes_match_string(data[len(data)-8:], message))
        ):
        print "Did not receive expected response for host %s." % (destination)
        return
        
    hop_count = ttl - new_ttl
    print "Data for host: %s (%s)" % (destination, destination_ip)
    print "Hops: %d" % (hop_count)
    time_in_sec = end_time - start_time
    time_in_micro_sec = time_in_sec * 1000 * 1000
    print "RTT (micro seconds): %d"  % (time_in_micro_sec)
    
    return hop_count, time_in_micro_sec
    

# Process Targets
    
# Open the targets text file in read mode
f = open('targets.txt', 'r')

# For every target
for line in f:
    # measure the hops/ get info for that target
    if not line.startswith("#"):
        measure_info(line.split()[0])
# Close the target file
f.close()
