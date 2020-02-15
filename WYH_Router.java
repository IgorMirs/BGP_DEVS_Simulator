package BGP_Simulator_v06_SignedMessages;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class WYH_Router extends MsgReceiver
{
    static final public String IN_DECISION = "in_decs";
    static final public String OUT_FROMNODES = "out_fNodes";
    
    protected ArrayList<Object> respondMatrix;
    protected ArrayList<Object> whatDoYouHave;
    int level;
    int msgID;
    
    public WYH_Router(String name, int id, NetStat netStat_, NodeInput nodeInput_, NodeNotRespond notRespond_, int level_) {
        super(name);
        addInport(INPORT);
//        addInport(IN_DECISION);
        addOutport(OUT_NODES);
        addOutport(OUT_FROMNODES);
//        addOutport(OUT_DECISION);
        this.ID = id;
        netStat = netStat_;
        input = nodeInput_.input;
        notRespond = notRespond_;
        level = level_;
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
//                System.out.println("X length " + ID + " " + x.getLength());
                if (messageOnPort(x, INPORT, i)) {
                   whatDoYouHave = ((WhatYouHaveMsg) x.getValOnPort(INPORT, i)).msgToSend;
                   msgID = getMsgID(whatDoYouHave);
                 //get the level value
                   int levelValue = getLevel(whatDoYouHave);
                   //from node checks the message only for it's level
                   if (levelValue == level) { 
                       holdIn("transfer", 0);
                   }
                } 
            }
        }
    }
    
    public void deltint() {
        if (phaseIs("transfer")) {
            holdIn("transfered", ID * countDelay());
        }
        else {
            passivate();
        }
     
    }
       
    public message out() {
        message m = new message();
        if (phaseIs("transfer")) {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("TRANSFER" + msgID, whatDoYouHave);
            m.add(makeContent(OUT_NODES, nm));
        }
        else {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("Transfered" + msgID, whatDoYouHave);
            m.add(makeContent(OUT_FROMNODES, nm));
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
            res /= 1000;
            count++;
        }
        return res;
    }
}