# Sarah Whelan
# slw96
# Introduction to Computer Networks
# Project 2
# December 2, 2015

import socket

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