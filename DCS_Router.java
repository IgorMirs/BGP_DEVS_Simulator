package BGP_Simulation_v05_NetworkTopology;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class DCS_Router extends MsgReceiver
{
    static final public String IN_DECISION = "in_decs";
    
    protected ArrayList<Object> respondMatrix;
    protected ArrayList<Object> respondMsg;
    int level;
    
    public DCS_Router(String name, int id, NetStat netStat_, NodeInput nodeInput_, NodeNotRespond notRespond_) {
        super(name);
        addInport(INPORT);
//        addInport(IN_DECISION);
        addOutport(OUT_NODES);
        addOutport(OUT_DECISION);
        this.ID = id;
        netStat = netStat_;
        input = nodeInput_.input;
        notRespond = notRespond_;
    }
    
    public void initialize() {
        //defining phase and sigma is equal to passivate()
        phase = "passive";
        sigma = INFINITY;
        

        seqNumber = new Vector<Vector<Integer>>();
        receivedMsg = new Stack<ArrayList<Object>>();
        whatYouHaveMsg = new ArrayList<Object>();
        respondMsg = new ArrayList<Object>();
        respondMatrix = new ArrayList<Object>();
        //put the values in SeqNum
        fillSeqNum();
        askOtherNodes = false;
        setType();
        getDecision = false;
        nodeDecision = defaultDes;

        super.initialize();
        
        
    }
    
    public void deltext(double e, message x) {
        Continue(e);
        time = netStat.time;
        //iteration through the messages
        for (int i = 0; i < x.getLength(); i++) {
            if (phaseIs("passive")) {
                if (messageOnPort(x, INPORT, i)) {
                   respondMsg = ((arrayListMsg) x.getValOnPort(INPORT, i)).msgToSend;
                   System.out.println("Before changing " + ID + " " + respondMsg);
                   if (type == 1 && (Integer) respondMsg.get(2) != makeFakeMsg(netStat.msg)) {
                       ArrayList<Object> temp = respondMsg;
                       respondMsg = new ArrayList<Object>();
                       respondMsg.add(temp.get(0));
                       respondMsg.add(temp.get(1));
                       respondMsg.add(makeFakeMsg(netStat.msg));
                       System.out.println("After changing " + ID + " " + respondMsg);
                   }
                   holdIn("transfer", 0);
                } 
            }
        }
    }
    
    public void deltint() {
        if (phaseIs("transfer")) {
            holdIn("transfered", 0.000000001);
        }
        else {
            passivate();
        }
     
    }
       
    public message out() {
        message m = new message();
        if (phaseIs("transfer")) {
            arrayListMsg nm = new arrayListMsg("TRANSFER" + ID, respondMsg);
            m.add(makeContent(OUT_DECISION, nm));
        }
        else {
            arrayListMsg nm = new arrayListMsg("Transfered" + ID, respondMsg);
            m.add(makeContent(OUT_NODES, nm));
        }
        return m;
    }
    
    
    
    /*
     * Count the time delay    
     */
    public double countDelay() {
        double res = 0.01;
        int count = 0;
        while (count != level) {
            res /= 10;
            count++;
        }
        return res;
    }
}