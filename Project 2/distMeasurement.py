# Sarah Whelan
# slw96
# Introduction to Computer Networks
# Project 2
# December 2, 2015

import socket
import sys
import time

def measure_info(destination):
    # The official traceroute port
    port = 33434
    # Get the expected protocols on the sockets
    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    # A known ttl 
    ttl = 32
    
    # Create sockets 
    
    # One to send a packet with a known ttl 
    # to a port that should return an ICMP Message with code 3
    sender = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)
    
    # Set the TTL explicitly so that we know what it was originally
    sender.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)
    
    # One to receive an ICMP packet ideally with code 3
    receiver = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
    
    # Bind the receiving socket to localhost/symbolic empty string on same port
    receiver.bind(("", port))
    
    # Set receiver to non-blocking to enable error handling if no response
    receiver.setblocking(0)
    
    # Start a timer to get RTT
    end_time = 0
    start_time = time.clock()
    
    # Send the packet to the destination
    sender.sendto("abcdefgh", (destination, port))
    
    # Attempt to get the data from the receiver by polling
    finished = False
    tries = 0
    max_tries = 5
    while not finished and tries < max_tries:
        try:
            # Get data from the receiver = the argument is the buffer size
            data, addr = receiver.recvfrom(2048)
            # Stop the timer
            end_time = time.clock()
            finished = True
        except socket.error as (errno, errmsg):
            tries = tries + 1
            # Wait a little bit before trying to poll again
            time.sleep(1)
    
    # Close the sockets
    sender.close()
    receiver.close()
        
    # If we didn't get a response exit
    if tries == max_tries:
        print "%s unreachable." % (max_tries, destination)
    else:
        # Process the data and determine how many hops the packet travelled
        #for i in range(len(data)):
        #   print i, "|", bin(ord(data[i]))
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
        
        if icmp_type != 3 or icmp_code != 3:
            print "Did not receive expected response."
        else:
            hop_count = ttl - new_ttl
            print "There were %d hops to get to %s" % (hop_count, destination)
            time_in_sec = end_time - start_time
            time_in_micro_sec = time_in_sec * 1000 * 1000
            print "RTT in micro seconds: ", time_in_micro_sec
            
            return hop_count, time_in_micro_sec
        

# Process Targets
    
# Open the targets text file in read mode
f = open('targets.txt', 'r')

# For every target
for line in f:
    # measure the hops/ get info for that target
    if line[0:1] != "#":
        measure_info(line.split()[0])
# Close the target file
f.close()
