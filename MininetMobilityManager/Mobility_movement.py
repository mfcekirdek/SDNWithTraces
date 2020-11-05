#!/usr/bin/python
import socket               # Import socket module

s = socket.socket()         # Create a socket object
host = "192.168.56.101" # Get local machine name
port = 12347                 # Reserve a port for your service.
s.bind((host, port))        # Bind to the port
f = open('topology.csv','wb')
s.listen(5)                 # Now wait for client connection.
isFileReceived = False
while not isFileReceived:
    c, addr = s.accept()     # Establish connection with client.
    print 'Got connection from', addr
    print "Receiving..."
    l = c.recv(1024)
    while (l):
        print "Receiving..."
	f.write(l)
	l = c.recv(1024)
	
    f.close()
    isFileReceived = True
    c.send('read')
    print "Done Receiving"
    c.close()                # Close the connection

# create topology

from mininet.cli import CLI
from mininet.net import Mininet
from mininet.log import info, output,  setLogLevel
from random import randint

from mininet.node import RemoteController

from MobilitySwitch import MobilitySwitch
from MyTracesTopology import MyTraceTopo


def printConnections( switches ):
    "Compactly print connected nodes to each switch"
    for sw in switches:
        output( '%s: ' % sw )
        for intf in sw.intfList():
            link = intf.link
            if link:
                intf1, intf2 = link.intf1, link.intf2
                remote = intf1 if intf1.node != sw else intf2
                output( '%s(%s) ' % ( remote.node, sw.ports[ intf ] ) )
        output( '\n' )


def moveHost( host, oldSwitch, newSwitch, newPort=None ):
    "Move a host from old switch to new switch"
    hintf, sintf = host.connectionsTo( oldSwitch )[ 0 ]
    oldSwitch.moveIntf( sintf, newSwitch, port=newPort )
    return hintf, sintf


def mobilityTest():
    "A simple test of mobility"
    info( '* Simple mobility test\n' )
    myTraceTopo = MyTraceTopo()
    c0 = RemoteController('c0', ip='192.168.56.1', port=6633)
    net = Mininet( topo=myTraceTopo, switch=MobilitySwitch,controller=c0)
    info( '* Starting network:\n' )
    net.start()
    printConnections( net.switches )
    info( '* Testing network\n' )
    net.pingAll()
    info( '* Identifying switch interface for h1\n' )
    h1, old = net.get( 'h1', 's1' )

    path = [2, 3, 1, 4 , 15, 32 , 1]


    
    # for s in path:
    #     new = net[ 's%d' % s ]
    #     port = randint( 10, 222 )
    #     info( '* Moving', h1, 'from', old, 'to', new, 'port', port, '\n' )
    #     hintf, sintf = moveHost( h1, old, new, newPort=port )
    #     info( '*', hintf, 'is now connected to', sintf, '\n' )
    #     info( '* Clearing out old flows\n' )
    #     for sw in net.switches:
    #         sw.dpctl( 'del-flows' )
    #     info( '* New network:\n' )
    #     printConnections( net.switches )
    #     info( '* Testing connectivity:\n' )
    #     net.pingAll()
    #     old = new
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
mobilityTest()
