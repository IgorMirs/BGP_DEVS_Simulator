package BGP_Simulation_v03_Internal_decisions;

import java.util.*;

import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Observer extends ViewableAtomic
{
    static final public String OUT_PORT = "out_msg";
    
    protected int ID;   //ID of the observer
    protected int msg;  //the message that the observer send to all other nodes
    protected Vector<Integer> input;
    protected ArrayList<Object> msgToSend;  //the collection of messages where each row is a particular message for each node in the network
    protected int type;   //the type of node (1 - traitor; 0 - loyal)
    protected String msgName;  //the name for the message
    protected NetStat netStat;
    

    
    protected NodeCoupledModel[] network;
    
    ///test
    protected int counter;
    
    private int seqCounter;  //the counter for the sequence number of the send message
    
    public Observer(String name, int id, int message, NetStat netStat_, NodeCoupledModel[] network_) {
        super(name);
            addOutport(OUT_PORT);
        this.ID = id;
        this.msg = message;
        if (msg == 0)
            msgName = "retreat";
        else
            msgName = "attack";
        netStat = netStat_;
        network = network_;
    }
    
    public void initialize() {
        //defining phase and sigma is equal to passivate()
        phase = "passive";
        sigma = INFINITY;
        //initialization of all state variables
        input = new Vector<Integer>();
      
        seqCounter = 1;
        setType();
        counter = 1;
        //creating the message ([msgId, msg, {srcId}, sqnsCounter, nTraitors])
        createMsg();
       
        //put the phase to active, send the output message, call deltint() 
        holdIn("active", 0);
        super.initialize();
        
    }
    

    public void deltint() {
        phase = "passive";
        sigma = netStat.nNodes + 1;
        if (Double.valueOf(getFormattedTN()) > netStat.nNodes) {
            printNodesDecisions();
            passivate();
        }
    }
    
    public void setType() {
        this.type = 0;
        for (int i =0; i < netStat.traitorVec.length; i++) {
            if (this.ID == netStat.traitorVec[i]) {
                this.type = 1;
            }
        }
    }
    
    public void printNodesDecisions() {
        System.out.println("Original message in the network " + netStat.msg);
        for (int i = 0; i < netStat.nNodes; i++) {
           System.out.println("Node ID " + network[i].fromCommander.ID + " type is " + network[i].fromCommander.type + " decision is " + network[i].fromCommander.nodeDecision);
        }
    }
    
    public void createMsg() {
        //[msgId, msg, {srcId}, sqnsCounter, nTraitors]
        msgToSend = new ArrayList<Object>();
        //add message ID
        msgToSend.add(netStat.getMsgId());
        //source ID as an array
        Vector<Integer> srcId = new Vector<Integer>();
        srcId.add(ID);
        msgToSend.add(srcId);
        //message value
        msgToSend.add(msg); 
        //message sequence number
        msgToSend.add(seqCounter);
        //add number of traitors
        msgToSend.add(netStat.nTraitors);
    }
    
       
    public message out() {
        message m = new message();
        //send the message only once in the beginning when time is 0
        if (Double.valueOf(getFormattedTN()) == 0) {
            WhatYouHaveMsg alm = new WhatYouHaveMsg(msgName, msgToSend);
            m.add(makeContent(OUT_PORT, alm));
        }
        return m;
    }
}
