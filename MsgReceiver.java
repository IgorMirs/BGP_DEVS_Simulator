package BGP_Simulation_v05_NetworkTopology;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import view.modeling.ViewableAtomic;

public class MsgReceiver extends ViewableAtomic {

    static final public String OUT_COMMANDER = "out_com";

    static final public String OUT_NODES = "out_nod";
    
    static final public String INPORT = "in";
    static final public String OUT_DECISION = "out_decs";
    
    static public double trDelay = 0.01;
    static public int defaultDes = 999;
    static public double calcDelay = 0.5;
    
    protected int ID;
    protected Vector<Integer> input;
    protected Vector<Integer> testVec;
    protected Vector<Integer> srcID;
    protected Vector<Vector<Integer>> seqNumber; 
    protected String msgName;
    public int type;
    protected boolean askOtherNodes;
    protected NetStat netStat;
    protected NodeNotRespond notRespond;
    protected int nodeDecision;
    
    protected Stack<ArrayList<Object>> receivedMsg;
    protected ArrayList<Object> whatYouHaveMsg;
    protected ArrayList<Object> respondMsg;
    protected Vector<Vector<Integer>> respondMatrix;  
    
    boolean getDecision;
    
    protected double time = 0;
	

	public MsgReceiver(String name) {
		super(name);
		
	}
	
	/**
     * Gets the integer message value from the given ArrayList
     * @param msg
     * @return
     */
    public int getMsgValue(ArrayList<Object> msg) {
        //the message value on the second place
        return (Integer) msg.get(2);
    }
    
    /**
     * Gets the message ID from the given ArrayList
     * @param msg
     * @return
     */
    public int getMsgID(ArrayList<Object> msg) {
        //the message ID on the first place
        return (Integer) msg.get(0);
    }
    
    public Vector<Integer> getSrcID(ArrayList<Object> msg){
        return (Vector<Integer>) msg.get(1);
    }
    
    public int getnTraitors(ArrayList<Object> msg) {
        //the message ID on the first place
        return (Integer) msg.get(4);
    }
    
    /**
     * Gets the level value from the given arraylist
     */
    public int getLevel(ArrayList<Object> msg) {
        //the message ID on the first place
        return (Integer) msg.get(5);
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
     * Make the opposite message to the given one.
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
    
    /**
     * Calculating nodes decision function
     */
    
    public int calcNodeDecs(int nodeId, int nTraitors) {
        if (type == 1) {
            return makeFakeMsg(netStat.msg);
        }
        if (nTraitors == 0)
            return inputMajority();
        
        for (int i = 0; i < netStat.nNodes; i++) {
            
        }
            
            calcNodeDecs(ID - 1, nTraitors - 1);
            holdIn("calc_decs", calcDelay);
        return 0;
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
    
    public int majority(Vector<Integer> v) {
      //if deleted all the values
        if (v.isEmpty())
            return 999;
        
        Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
        
      //count how many times each value is in the vector and put it to the hash table
        for (int i = 0; i < v.size(); i++) {
            //if found the element in the hash
            if (hash.containsKey(v.elementAt(i)) == true) {
                int temp = hash.get(v.elementAt(i));
                temp++;
                hash.put(v.elementAt(i), temp);
            }
            else
                hash.put(v.elementAt(i), 1);
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

    public void crtWhatYouHaveMsg(int msgID, Vector<Integer> srcID, int nTraitors, int level) {
        
        whatYouHaveMsg.add(msgID);
        whatYouHaveMsg.add(srcID);  ///later on we should put here the commander ID
        whatYouHaveMsg.add(1);
        whatYouHaveMsg.add(1);
        whatYouHaveMsg.add(nTraitors - 1); 
        whatYouHaveMsg.add(level);
    }
    
    public void crtRespondMatrix(int msgID) {
        for (int j = 1; j <= netStat.nNodes; j++) {
            boolean respond = true;
            for (int z = 0; z < srcID.size(); z++) {
                if (j == srcID.elementAt(z)) {
                    respond = false;
                    break;
                }
            }
            //the node ID is not on the srcID array, so we will wait the respond from this node
            if (respond) {
                Vector<Integer> tempV = new Vector<Integer>();
                tempV.add(msgID);
                tempV.add(j);
                tempV.add(defaultDes);
                respondMatrix.add(tempV);
            }
        }
    }
    
    public Vector<Vector<Integer>> addRespondMatrix(int msgID) {
        Vector<Vector<Integer>> respondMatrix = new Vector<Vector<Integer>>(); 
        for (int j = 1; j <= netStat.nNodes; j++) {
            boolean respond = true;
            for (int z = 0; z < srcID.size(); z++) {
                if (j == srcID.elementAt(z)) {
                    respond = false;
                    break;
                }
            }
            //the node ID is not on the srcID array, so we will wait the respond from this node
            if (respond) {
                Vector<Integer> tempV = new Vector<Integer>();
                tempV.add(msgID);
                tempV.add(j);
                tempV.add(defaultDes);
                respondMatrix.add(tempV);
            }
        }
        return respondMatrix;
    }
    
    public void crtRespondMsg(int msgID) {
        //Add msg ID on which the node respond
        respondMsg.add(msgID);
        //add the responder ID
        respondMsg.add(ID);
        //add the result (traitor send the fake msg)
        if (type == 0)
            respondMsg.add(inputMajority());
        else {
            respondMsg.add(makeFakeMsg(netStat.msg));
        }
    }
	
    /**
     * Checking if received response is in the waiting list of responses
     */
    public void checkRespond(int msgID, int nodeID, int decs) {
        for (int i = 0; i < respondMatrix.size(); i++) {
                if (respondMatrix.elementAt(i).elementAt(0) == msgID && respondMatrix.elementAt(i).elementAt(1) == nodeID) {
                    respondMatrix.elementAt(i).set(2, decs);
                    break;
                }
        }
    }
    
    /**
     * Check if the node gets all the responses that he is waiting for.
     */
    
    public boolean isFinishedToCalc() {
        boolean finished = true;
        Vector<Integer> decisions = new Vector<Integer>();
        for (int i = 0; i < respondMatrix.size(); i++) {
            if (respondMatrix.elementAt(i).elementAt(2) == defaultDes) {
                finished = false;
                break;
            }
            decisions.add(respondMatrix.elementAt(i).elementAt(2));
        }
        if (finished) {
            //add the original input from the commander
            decisions.add(input.elementAt(0));
            System.out.println("isFinishedToCalc() Respond matrix " + ID + " " + respondMatrix);
//            System.out.println("Decision " + majority(decisions));
            nodeDecision = majority(decisions);
            return true;
        }
        else
            return false;
    }
    

}
