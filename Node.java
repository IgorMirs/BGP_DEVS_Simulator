package BGP_Simulation_temp;

import java.util.Hashtable;
import java.util.Vector;

import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Node extends ViewableAtomic
{
    static final public String IN_COMMANDER = "in_com";
    static final public String OUT_COMMANDER = "out_com";

    static final public String IN_NODES = "in_nod";
    static final public String OUT_NODES = "out_nod";

    protected int ID;
    protected Vector<Integer> input;
    protected Vector<Vector<Integer>> transferMsgBag;  //the commander's msg for further transferring
    protected Vector<Vector<Integer>> askOtherMsgBag;
    protected Vector<Vector<Integer>> seqNumber; 
    protected String msgName;
    public int type;
    protected boolean askOtherNodes;
    protected NetStat netStat;
    
    public Node(String name, int id, NetStat netStat_) {
        super(name);
        addInport(IN_COMMANDER);
        addOutport(OUT_COMMANDER);
        addInport(IN_NODES);
        addOutport(OUT_NODES);
        this.ID = id;
        netStat = netStat_;
    }
    
    public void initialize() {
        //defining phase and sigma is equal to passivate()
        phase = "passive";
        sigma = INFINITY;
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
        System.out.println("seq number " + ID + " " + seqNumber);
        System.out.println("My type " + ID + " " + type);
        if (phaseIs("passive")) {
            //iteration through the messages
            for (int i = 0; i < x.getLength(); i++) {
                if (messageOnPort(x, IN_COMMANDER, i)) {
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
                        for (int k = 0; k < temp.elementAt(j).size(); k++) 
                            tempV.add(temp.elementAt(j).elementAt(k));
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
                }
            }
            holdIn("transfer", 0);
        }
        
        //node starts asking other's nodes decision
        if (phaseIs("active") && !askOtherNodes) {
            for (int i = 0; i < x.getLength(); i++) {
                
                if (messageOnPort(x, IN_NODES, i)) {
                    System.out.println("" + this.ID + " " + ((askOthrMsg) x.getValOnPort(IN_NODES, i)).msgBag);
                    askOtherMsgBag = ((askOthrMsg) x.getValOnPort(IN_NODES, i)).msgBag;
                    for (int j = 0; j < askOtherMsgBag.size(); j++) {
                        if (askOtherMsgBag.elementAt(j).elementAt(1) == this.ID && 
                                askOtherMsgBag.elementAt(j).elementAt(3) == 999) {
                            askOtherMsgBag.elementAt(j).set(3, 1000);
                            break;
                        }
                    }
                    
                    askOtherNodes = true;
                    holdIn("active", 0);
                }
            }
        }
        System.out.println("INPUT " + ID + " " + input);
    }
    
    public void deltint() {
        holdIn("active", ID);
        createAskMsg();
    }
       
    public message out() {
        message m = new message();
        if (phaseIs("transfer")) {
            nodeMsg ndm = new nodeMsg(msgName, transferMsgBag);
            m.add(makeContent(OUT_COMMANDER, ndm));
        }
        else {
            askOthrMsg askOth = new askOthrMsg("ASK", askOtherMsgBag);
            m.add(makeContent(OUT_NODES,askOth));
        }
            
        return m;
    }
    
    public void createAskMsg(){
        askOtherMsgBag.clear();
        //creating the message (source, dest, nextHop)
        for (int i = 0; i < netStat.nNodes; i++) {
            if ((i + 1) == ID)
                continue;
            Vector<Integer> temp = new Vector<Integer>();
            //source
            temp.add(ID);
            //destination
            temp.add(i + 1);
            //next hop
            temp.add(i + 1);
            //nodes decision (999 - default value)
            temp.add(999);
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
    
    public int input_majority() {
      //if deleted all the values
        if (input.isEmpty())
            return 999;
        
        Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
        
      //count how many times each value is in the vector and put it to the map
     /*   for (int i = 0; i < input.size(); i++) {
            //if found the element in the hash
            if (hash.containsKey(input.elementAt(i)) == true) {
                hash.
            }
            auto it = hash.find(input[i]); //if not found the value return iterator to the end
            if (it != hash.end())
                it->second++;
            hash.emplace(val_vec[i], 1);
        }*/
        return input.elementAt(0);
    }
}