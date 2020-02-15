package BGP_Simulator_v06_SignedMessages;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import model.modeling.message;
import view.modeling.ViewableAtomic;
//import view.modeling.ViewableAtomic;

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
    protected int time;
    protected String fileName;
    protected int traitorsFileRow;
    
    protected NodeCoupledModel[] network;
    
    ///test
    protected int counter;
    
    private int seqCounter;  //the counter for the sequence number of the send message
    
    public Observer(String name, int id, int message, NetStat netStat_, NodeCoupledModel[] network_, String fileName_, int traitorsFileRow_) {
        super(name);
            addOutport(OUT_PORT);
        this.ID = id;
        this.msg = message;
        fileName = fileName_;
        traitorsFileRow = traitorsFileRow_;
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
        time = 0;
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
            //print nodes decisions to console
//            printNodesDecisions();
            //create the file with nodes decisions
            printToFile();
            //create succeed or failed file
            checkSolution();
            passivate();
        }
        else 
            time = netStat.nNodes + 1;
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
           System.out.println("Node ID " + network[i].ID + " type is " + network[i].fromCommander.type + " decision is " + network[i].fromCommander.nodeDecision);
        }
    }
    
    public void checkSolution() {
        String filePath = ".\\BGP_Simulator_v06_SignedMessages\\results\\" + fileName + "[" + traitorsFileRow + "]";
//        String filePath = "C:\\Users\\garik\\eclipse-workspace\\DEVS Enviroment\\Models\\BGP_Simulator_v06_SignedMessages\\results\\" + Arrays.toString(netStat.traitorVec);
        for (int i = 0; i < netStat.nNodes; i++) {
            if (network[i].fromCommander.nodeDecision != netStat.msg && network[i].fromCommander.type != 1 || 
                netStat.nTraitors == netStat.nNodes) {
                File file = new File(filePath + "._failed");
                try {
                    file.createNewFile();
                    System.out.println('\n' + file.getName() + '\n');
                }
                catch (Exception e)
                {
                    System.out.println("Error open the file");
                    // TODO: handle exception
                    e.printStackTrace();
                }
                return;
            }
        }  
        File file = new File(filePath + "._succeed");
        try {
            file.createNewFile();
            System.out.println('\n' + file.getName() + '\n');
        }
        catch (Exception e)
        {
            System.out.println("Error open the file");
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    public void printToFile() {
        try {
            String file = ".\\BGP_Simulator_v06_SignedMessages\\results\\" + fileName + "[" + traitorsFileRow + "]" + ".result";
            PrintWriter pw = new PrintWriter(new File(file)); 
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < netStat.nNodes; i++) {
                sb.append(network[i].ID);
                if (i != netStat.nNodes - 1)
                    sb.append(",");
            }
            sb.append("\r\n");
            for (int i = 0; i < netStat.nNodes; i++) {
                sb.append(network[i].fromCommander.nodeDecision);
                if (i != netStat.nNodes - 1)
                    sb.append(",");
            }
            sb.append("\r\n");
            pw.write(sb.toString());
            pw.close();
        }
        catch (Exception e)
        {
            System.out.println("Error open the file");
            // TODO: handle exception
            e.printStackTrace();
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
///TEST add srcID to the list of sending nodes
        netStat.srcID.add(ID);
        
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
        if (time == 0) {
            WhatYouHaveMsg alm = new WhatYouHaveMsg(msgName, msgToSend);
            m.add(makeContent(OUT_PORT, alm));
        }
        return m;
    }
}
