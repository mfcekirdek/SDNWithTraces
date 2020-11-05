import sys
import socket
import os

from HmmModelWithCountingApproach import HMM_Learning_Object
from MarkovChainModel import Markov_Chain_Model
from PlotUtil import Plotter

learner = None
plotter = None

def make_string_from_results(*args):
    return "_".join(map(str, args))

def sendTopologyFileToMininetMachine(file):
    s = socket.socket()  # Create a socket object
    host = "192.168.56.101"  # socket.gethostname() # Get local machine name
    port = 12347  # Reserve a port for your service.

    s.connect((host, port))
    f = open(file, 'rb')
    l = f.read(1024)
    while (l):
        s.send(l)
        l = f.read(1024)
    f.close()
    s.shutdown(socket.SHUT_WR)
    response = s.recv(1024)
    s.close()
    response = str(response,'utf-8')
    if "read" in response:
        return "read"
    else:
        return "notRead"

def init_learner(csv_path,model_type):
    global learner

    if(model_type == "HMM"):
        sys.stdout.write("Learning from %s\n" % csv_path)
        sys.stdout.flush()
        learner = HMM_Learning_Object(csv_path)
        learner.train()
        learner.setFirstLocationAndWhen("2", "A")
        return "Ready"
    elif(model_type == "MCM"):
        sys.stdout.write("Learning from %s\n" % csv_path)
        sys.stdout.flush()
        learner = Markov_Chain_Model(csv_path)
        learner.setFirstLocationAndWhen("2", "1")
        return "Ready"



sys.stdout.write("Initialized\n")
sys.stdout.flush()

s = sys.stdin.readline().strip()

while s not in ['break', 'quit']:

    result = s

    if s == "topologyFileIsReady":
        try:
            dir_path = os.path.dirname(os.path.realpath(__file__))
            result = sendTopologyFileToMininetMachine(dir_path + "/topology.csv")
        except Exception as e :
            print(e)
            result = "notRead"
    elif "predictionFileIsReady" in s:
        try:
            parameters = s.split(sep='_')
            dir_path = os.path.dirname(os.path.realpath(__file__))
            plotter = Plotter(dir_path + "/predictions.csv")
            if(parameters[1] == "real"):
                plotter.plotRealMovement()
                result = "plotted"
            elif(parameters[1] == "predicted"):
                plotter.plotPredictedMovement()
                result = "plotted"
        except Exception as e:
            result = "notPlotted"
    elif s == "getArrays":
        where_inputs, when_inputs, predictions = learner.getArrays()
        result = make_string_from_results(where_inputs, when_inputs, predictions)
    elif s == "getAccuracy":
        true_counter,total_counter,accuracy = learner.getAccuracyScore()
        result = make_string_from_results(true_counter, total_counter, accuracy)
    elif s == "clearHistoryStatistics":
        learner.clearHistoryStatistics()
    elif "predictAndLearn" in s:
        parameters = s.split(sep='_')[1:]
        cid = parameters[0]
        time_segment = parameters[1]

        if (type(learner) is HMM_Learning_Object):
            isPredictionTrue, true_cell, predicted_cell, when = learner.predictAndLearn(cid, time_segment)
        elif (type(learner) is Markov_Chain_Model):
            time_segment = ["A","E","M","N"].index(time_segment)
            isPredictionTrue, true_cell, predicted_cell, when = learner.predictAndLearn(cid, time_segment)
        result = make_string_from_results(isPredictionTrue, true_cell, predicted_cell, when)
    elif "newData" in s:
        parameters = s.split(" ")[1:]
        result = init_learner(parameters[0],parameters[1])
    sys.stdout.write(result + '\n')
    sys.stdout.flush()
    s = sys.stdin.readline().strip()















    ##############



    # # !/usr/bin/python
    #
    # """
    # Simple example of Mobility with Mininet
    # (aka enough rope to hang yourself.)
    # We move a host from s1 to s2, s2 to s3, and then back to s1.
    # Gotchas:
    # The reference controller doesn't support mobility, so we need to
    # manually flush the switch flow tables!
    # Good luck!
    # to-do:
    # - think about wifi/hub behavior
    # - think about clearing last hop - why doesn't that work?
    # """
    # from mininet.cli import CLI
    # from mininet.net import Mininet
    # from mininet.log import info, output, setLogLevel
    # from random import randint
    #
    # from mininet.node import RemoteController
    #
    # from MobilitySwitch import MobilitySwitch
    # from MyTracesTopology import MyTraceTopo
    #
    #
    # def printConnections(switches):
    #     "Compactly print connected nodes to each switch"
    #     for sw in switches:
    #         output('%s: ' % sw)
    #         for intf in sw.intfList():
    #             link = intf.link
    #             if link:
    #                 intf1, intf2 = link.intf1, link.intf2
    #                 remote = intf1 if intf1.node != sw else intf2
    #                 output('%s(%s) ' % (remote.node, sw.ports[intf]))
    #         output('\n')
    #
    #
    # def moveHost(host, oldSwitch, newSwitch, newPort=None):
    #     "Move a host from old switch to new switch"
    #     hintf, sintf = host.connectionsTo(oldSwitch)[0]
    #     oldSwitch.moveIntf(sintf, newSwitch, port=newPort)
    #     return hintf, sintf
    #
    #
    # def mobilityTest():
    #     "A simple test of mobility"
    #     info('* Simple mobility test\n')
    #     myTraceTopo = MyTraceTopo()
    #     c0 = RemoteController('c0', ip='192.168.56.1', port=6633)
    #     net = Mininet(topo=myTraceTopo, switch=MobilitySwitch, controller=c0)
    #     info('* Starting network:\n')
    #     net.start()
    #     printConnections(net.switches)
    #     info('* Testing network\n')
    #     net.pingAll()
    #     info('* Identifying switch interface for h1\n')
    #     h1, old = net.get('h1', 's1')
    #
    #     path = [2, 3, 1, 4, 15, 32, 1]
    #
    #     # for s in path:
    #     #     new = net[ 's%d' % s ]
    #     #     port = randint( 10, 222 )
    #     #     info( '* Moving', h1, 'from', old, 'to', new, 'port', port, '\n' )
    #     #     hintf, sintf = moveHost( h1, old, new, newPort=port )
    #     #     info( '*', hintf, 'is now connected to', sintf, '\n' )
    #     #     info( '* Clearing out old flows\n' )
    #     #     for sw in net.switches:
    #     #         sw.dpctl( 'del-flows' )
    #     #     info( '* New network:\n' )
    #     #     printConnections( net.switches )
    #     #     info( '* Testing connectivity:\n' )
    #     #     net.pingAll()
    #     #     old = new
    #     CLI(net)
    #     net.stop()
    #
    #
    # if __name__ == '__main__':
    #     setLogLevel('info')
    # mobilityTest()
