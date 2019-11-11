package BGP_Simulation_temp;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Node extends ViewableAtomic
{
    static final public String IN_COMMANDER = "in_com";
    static final public String OUT_COMMANDER = "out_com";

    static final public String IN_NODES = "in_nod";
    static final public String OUT_NODES = "out_nod";
    
    static final public String IN_DECISION = "in_decs";
    static final public String OUT_DECISION = "out_decs";
    
    static public double trDelay = 0.01;
    
    protected int ID;
    protected Vector<Integer> input;
    protected Vector<Vector<Integer>> transferMsgBag;  //the commander's msg for further transferring
    protected Vector<Vector<Integer>> askOtherMsgBag;
    protected Vector<Vector<Integer>> seqNumber; 
    protected String msgName;
    public int type;
    protected boolean askOtherNodes;
    protected NetStat netStat;
    protected int nodeDecision;
    
    protected double time = 0;
    
    public Node(String name, int id, NetStat netStat_) {
        super(name);
        addInport(IN_COMMANDER);
        addOutport(OUT_COMMANDER);
        addInport(IN_NODES);
        addOutport(OUT_NODES);
        addInport(IN_DECISION);
        addOutport(OUT_DECISION);
        this.ID = id;
        netStat = netStat_;
    }
    
    public void initialize() {
        //defining phase and sigma is equal to passivate()
        phase = "passive";
        sigma = 9999;
        input = new Vector<Integer>();
        askOtherMsgBag = new Vector<Vector<Integer>>();
        seqNumber = new Vector<Vector<Integer>>();
        //put the values in SeqNum
        fillSeqNum();
        askOtherNodes = false;
        setType();
        super.initialize();
        
    }
    
    public void deltext(double e, message x) {
        Continue(e);
        time = netStat.time;
        System.out.println("Time " + time);
//       System.out.println("FormattedTN " + Double.valueOf(getFormattedTN()));
//       else
//           System.out.println("Formated Time " + Double.valueOf(getFormattedTL()));
        
       if (phaseIs("passive")) {
            //iteration through the messages
            for (int i = 0; i < x.getLength(); i++) {
                if (messageOnPort(x, IN_COMMANDER, i)) {
                    System.out.println("recived mes " + ID + " " + ((nodeMsg) x.getValOnPort(IN_COMMANDER, i)).msgBag);
                    //the name for further transferring
                    msgName = setMsgName();
                    //create "the pointer" to the received vector of vectors
                    Vector<Vector<Integer>> temp = ((nodeMsg) x.getValOnPort(IN_COMMANDER, i)).msgBag; 
                   
                    //create msg for transferring
                    transferMsgBag = new Vector<Vector<Integer>>();
                    
                    //copy the received msg to the transferring msg
                    for (int j = 0; j < temp.size(); j++) {
                        //do not transfer the messages which were previously checked
                        int checked = temp.elementAt(j).elementAt(4); 
                        if (checked == 1)
                            continue;
                        Vector<Integer> tempV = new Vector<Integer>();
                        //if the node is traitor and received message which was not changed before
                        //change the transfer message
                        if (type == 1 && temp.elementAt(j).elementAt(0) != makeFakeMsg(netStat.msg)) {
                            for (int k = 0; k < temp.elementAt(j).size(); k++) {
                                if (k == 0)
                                    tempV.add(makeFakeMsg(netStat.msg));
                                else
                                    tempV.add(temp.elementAt(j).elementAt(k));
                            }
                        } else {
                            for (int k = 0; k < temp.elementAt(j).size(); k++) 
                                tempV.add(temp.elementAt(j).elementAt(k));
                        }
                        transferMsgBag.add(tempV);
                    }
                    
                    for (int j = 0; j < transferMsgBag.size(); j++) {
                        //if the message was previously checked by the other node ->
                        //skip such message
                        int alreadyChecked = transferMsgBag.elementAt(j).elementAt(4); 
                        int destId = transferMsgBag.elementAt(j).elementAt(2);
                        int seqNum = transferMsgBag.elementAt(j).elementAt(5);
                        int sourceId = transferMsgBag.elementAt(j).elementAt(1);
                        int msg = transferMsgBag.elementAt(j).elementAt(0);
                        if (alreadyChecked == 1) 
                            continue;

                        //if the dest of the message equal to nodes ID
                        else if (destId == this.ID && getSeqVal(sourceId) < seqNum) {
                            //put the message to the input of the node
                            this.input.add(msg);
                            //set the checked flag to this msg
                            transferMsgBag.elementAt(j).set(4, 1);
                            addSeqNum(sourceId);
                            break;
                        }
                    }
                    holdIn("transfer", 0);
                }
                
                if (messageOnPort(x, IN_DECISION, i)) {
                    if (((nodeMsg) x.getValOnPort(IN_DECISION, i)).msgBag.elementAt(0).elementAt(0) == ID) {
//                        nodeDecision = calcNodeDecs(netStat.nTraitors);
                        System.out.println("Decision msg " + this.ID + " " + ((nodeMsg) x.getValOnPort(IN_DECISION, i)).msgBag);
                    }
                    else {
                        crtTransferDecsMsg(((nodeMsg) x.getValOnPort(IN_DECISION, i)).msgBag);
                        holdIn("trfDecs",0);
                    }
                }
            }
        }
    }
    
    public void deltint() {
        if(phase == "transfer" && Double.valueOf(getFormattedTN()) > time) {
            phase = "passive";
            sigma = 9999;
            time = Double.valueOf(getFormattedTN());
        }
        
        else if (phaseIs("trfDecs") && Double.valueOf(getFormattedTN()) > time) {
            phase = "passive";
            sigma = 9999;
            System.out.println("I am here");
            time = Double.valueOf(getFormattedTN());
        }
    }
       
    public message out() {
       // time = Double.valueOf(getFormattedTN());
        message m = new message();
        if (phaseIs("transfer") && Double.valueOf(getFormattedTN()) <= time) {
            nodeMsg ndm = new nodeMsg(msgName, transferMsgBag);
            m.add(makeContent(OUT_COMMANDER, ndm));
            sigma = trDelay;
        }
        if(phaseIs("trfDecs") && Double.valueOf(getFormattedTN()) <= time) {
            nodeMsg ndm = new nodeMsg("DecisionTransf", transferMsgBag);
            m.add(makeContent(OUT_DECISION, ndm));
            sigma = trDelay;
        }
//        else {
//            nodeMsg ndm = new nodeMsg("WhatYouHave", askOtherMsgBag);
//            m.add(makeContent(OUT_NODES, ndm));
//        }
            
        return m;
    }
    
    public void crtTransferDecsMsg(Vector<Vector<Integer>> sample) {
        transferMsgBag = new Vector<Vector<Integer>>();
        Vector<Integer> temp = new Vector<Integer>();
        temp.add(sample.elementAt(0).elementAt(0));
        transferMsgBag.add(temp);
    }
    
    public void createAskMsg(){
        askOtherMsgBag = new Vector<Vector<Integer>>();
        //creating the message (source, dest, nextHop)
        for (int i = 0; i < netStat.nNodes; i++) {
            if ((i + 1) == ID)
                continue;
            Vector<Integer> temp = new Vector<Integer>();
            //source
            temp.add(ID);
            //destination
            temp.add(i + 1);
            askOtherMsgBag.add(temp);
        }
    }
    
    /**
     * Create a matrix with the list of all nodes in the network.
     * Put the sequence number of received messages for all the nodes.
     * Initially sequence number equal to 0. 
     */
    public void fillSeqNum() {
        for (int i = 0; i <= netStat.nNodes; i++) {
            Vector<Integer> temp = new Vector<Integer>();
            temp.add(i);
            temp.add(0);
            seqNumber.add(temp);
        }
    }
    
    /**
     * Return the current message sequence number for the given node.
     */
    public int getSeqVal(int nodeId) {
        for (int i = 0; i < seqNumber.size(); i++) {
            if (seqNumber.elementAt(i).elementAt(0) == nodeId) {
                return seqNumber.elementAt(i).elementAt(1);
            }
        }
        return 0;
    }
    
    /**
     * Increment the message sequence number for the given node.
     */
    public void addSeqNum(int nodeId) {
        for (int i = 0; i < seqNumber.size(); i++) {
            if (seqNumber.elementAt(i).elementAt(0) == nodeId) {
                int a = seqNumber.elementAt(i).elementAt(1);
                a++;
                seqNumber.elementAt(i).set(1, a);
            }
        }
    }
    
    /**
     * Set the type of the node (1 - traitor; 0 - loyal).
     * If the node ID is in traitors vector - this node is a traitor.
     */
    public void setType() {
        this.type = 0;
        for (int i =0; i < netStat.traitorVec.length; i++) {
            if (this.ID == netStat.traitorVec[i]) {
                this.type = 1;
            }
        }
    }
    
    /**
     * Make the opposite message from the given one.
     */
    public int makeFakeMsg(int msg) {
        if (msg == 0)
            return 1;
        else
            return 0;
    }
    
    /**
     *  Set the message name according to the type of the node.
     */
    public String setMsgName() {
        int tempMsg;
        if (type == 1) 
            tempMsg = makeFakeMsg(netStat.msg);
        else
            tempMsg = netStat.msg;
        if (tempMsg == 0)
            return "retreat";
        else
            return "attack";
    }
    
    public int calcNodeDecs(int nTraitors) {
        if (type == 1) {
            return makeFakeMsg(netStat.msg);
        }
        else if (nTraitors == 0)
            return inputMajority();
        else {
            createAskMsg();
       //     holdIn("WhatYouHave", trDelay);
            return 0;
        }
    }
    
    public int inputMajority() {
      //if deleted all the values
        if (input.isEmpty())
            return 999;
        
        Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
        
      //count how many times each value is in the vector and put it to the hash table
        for (int i = 0; i < input.size(); i++) {
            //if found the element in the hash
            if (hash.containsKey(input.elementAt(i)) == true) {
                int temp = hash.get(input.elementAt(i));
                temp++;
                hash.put(input.elementAt(i), temp);
            }
            else
                hash.put(input.elementAt(i), 1);
        }
        
      //searching for the most often value
        int max = 0, key_ = 0;
        Set<Integer> keys = hash.keySet();
        for (int key: keys) {
            if (hash.get(key) > max) {
                max = hash.get(key);
                key_ = key;
            }
        }
        
      //max should be unique, if it is not unique, so there is no majority and we return 0
        for (int key: keys) {
            if (hash.get(key) == max && key_ != key)
                return 0;
        }
    
        return key_;
    }
    
    
    
}
