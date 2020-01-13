package BGP_Simulation_v05_NetworkTopology;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Commander_Router extends MsgReceiver
{
    static final public String IN_NODES = "in_nod";
    static final public String OUT_NODES = "out_nod";
    
    static final public String IN_DECISION = "in_decs";
    static final public String OUT_DECISION = "out_decs";
    
    protected ArrayList<Object> respondMatrix;
    protected ArrayList<Object> whatDoYouHave;
    int level;
    int msgID;
    
    public Commander_Router(String name, NetStat netStat_) {
        super(name);
        addInport(IN_NODES);
        addOutport(OUT_NODES);
        addInport(IN_DECISION);
        addOutport(OUT_DECISION);
        netStat = netStat_;
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
            if (messageOnPort(x, IN_NODES, i)) {
               whatDoYouHave = ((WhatYouHaveMsg) x.getValOnPort(IN_NODES, i)).msgToSend;
               holdIn("transfer", 0);
            }
            if (messageOnPort(x, IN_DECISION, i)) {
                respondMsg = ((arrayListMsg) x.getValOnPort(IN_DECISION, i)).msgToSend;
                holdIn("transfer_dcs", 0);
            }
        } 
    }
    
    public void deltint() {
        passivate();
    }
       
    public message out() {
        message m = new message();
        if (phaseIs("transfer")) {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("TRANSFER", whatDoYouHave);
            m.add(makeContent(OUT_NODES, nm));
        }
        if (phaseIs("transfer_dcs")) {
            arrayListMsg al = new arrayListMsg("TRANSFER_DCS", respondMsg);
            m.add(makeContent(OUT_DECISION, al));
        }
        return m;
    }
    
}