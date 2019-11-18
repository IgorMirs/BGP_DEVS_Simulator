package BGP_Simulation_v02_Internal_decisions;

import java.util.Vector;

import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Observer extends ViewableAtomic
{
    static final public String IN_PORT = "in";
    static final public String OUT_PORT1 = "out_msg";
    static final public String OUT_DECISION = "out_decs";
    
    protected int ID;   //ID of the observer
    protected int msg;  //the message that the observer send to all other nodes
    protected Vector<Integer> input;
    protected Vector<Vector<Integer>> msgBag;  //the collection of messages where each row is a particular message for each node in the network
    protected int type;   //the type of node (1 - traitor; 0 - loyal)
    protected String msgName;  //the name for the message
    protected NetStat netStat;
    
    protected Node[] network;
    
    ///test
    protected int counter;
    
    private int seqCounter;  //the counter for the sequence number of the send message
    
    public Observer(String name, int id, int message, NetStat netStat_, Node[] network_) {
        super(name);
            addInport(IN_PORT);
            addOutport(OUT_PORT1);
            addOutport(OUT_DECISION);
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
        //creating the message (message, source, dest, nextHop, checkflag, sequence_number)
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
        for (int i = 0; i < netStat.nNodes; i++) {
            System.out.println("Node ID " + network[i].ID + " type is " + network[i].type + " decision is " + network[i].nodeDecision);
        }
    }
    
    public void createMsg() {
        msgBag = new Vector<Vector<Integer>>();
        for (int i = 0; i < netStat.nNodes; i++) {
            Vector<Integer> temp = new Vector<Integer>();
            //message
            temp.add(msg); 
            //source
            temp.add(ID);
            //destination
            temp.add(i + 1);
            //next hop
            temp.add(i + 1);
            //checked flag
            temp.add(0);
            //message sequence number
            temp.add(seqCounter);
            msgBag.add(temp);
        }
    }
    
    public void createDecMsg(int counter_) {
        msgBag = new Vector<Vector<Integer>>();
            Vector<Integer> temp = new Vector<Integer>();
            //message
            temp.add(counter_);
            msgBag.add(temp);
            counter++;
    }
       
    public message out() {
        message m = new message();
        nodeMsg stm = new nodeMsg(msgName, msgBag);
        netStat.time = Double.valueOf(getFormattedTN());
        if (sigma == 0)
            m.add(makeContent(OUT_PORT1, stm));
//        else
//            m.add(makeContent(OUT_DECISION, stm));
        return m;
    }
}
