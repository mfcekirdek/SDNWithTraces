"""Custom topology example

Two directly connected switches plus a host for each switch:

   host --- switch --- switch --- host

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=mytopo' from the command line.
"""
import numpy as np

import pandas
from mininet.topo import Topo
from MobilitySwitch import MobilitySwitch


# def loadArrayFromFile(dir, filename):
#     arr = np.load(dir + "/" + filename + '.npy')
#     return arr
# komsuluk_matrisi = loadArrayFromFile(".","komsuluk")

class MyTraceTopo( Topo ):
    "Simple topology example."

    def __init__( self):
        "Create custom topo."


        data_frame = pandas.read_csv("topology.csv", sep=',', header=0)
        data = data_frame.values
        cids = data[:, 5]

        unique_cids = np.unique(cids)
        adjaceny_matrix = np.zeros((len(unique_cids), len(unique_cids)), dtype="int")

        # Initialize topology
        Topo.__init__( self )

        first_cell_number = 1
        first_location = "s%s" % first_cell_number

        for i in range(len(adjaceny_matrix)):
            temp_switch = self.addSwitch('s%s' % str(i+1), cls=MobilitySwitch)

        user = self.addHost("h1")
        self.addLink( user, first_location )

        for i in range(len(adjaceny_matrix)):
            komsular = adjaceny_matrix[i]
            for j in range(len(komsular)):
                if(komsular[j] == 1):
                    temp_switch = self.switches()[i]
                    temp_komsu_switch = self.switches()[j]

                    # print temp_switch,temp_komsu_switch ,i ,j

                    if ((temp_switch, temp_komsu_switch) in self.links() or (temp_komsu_switch, temp_switch) in self.links()):
                        print "Already exist .. ", temp_switch , temp_komsu_switch
                    else:
                        self.addLink(temp_switch, temp_komsu_switch)
                        print "Link created .. ", temp_switch, temp_komsu_switch

                    # print ('\n')

# k = MyTraceTopo()
# print k.hosts()
# topos = { 'mytopo': (lambda: MyTraceTopo())}




