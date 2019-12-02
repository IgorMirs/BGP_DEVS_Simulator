package BGP_Simulation_v03_Internal_decisions;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class GetDecision extends MsgReceiver
{

    public GetDecision(String name, int id, NetStat netStat_, NodeInput nodeInput_) {
        super(name);
        addInport(INPORT);
        addOutport(OUT_COMMANDER);
        addOutport(OUT_DECISION);
        addOutport(OUT_NODES);
        this.ID = id;
        netStat = netStat_;
        input = nodeInput_.input;
    }
    
    public void initialize() {
        //defining phase and sigma is equal to passivate()
        phase = "passive";
        sigma = INFINITY;
//        input = new Vector<Integer>();
        seqNumber = new Vector<Vector<Integer>>();
        receivedMsg = new Stack<ArrayList<Object>>();
        whatYouHaveMsg = new ArrayList<Object>();
 //       respondMsg = new ArrayList<Object>();
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
                if (messageOnPort(x, INPORT, i)) {
                  System.out.println("I got the decision");
                }
            }
    } //end deltext()
    
    public void deltint() {
        holdIn(phase, INFINITY);
//        sigma = ID - Double.valueOf(getFormattedTN());
    }
       
    public message out() {
        message m = new message();
        
        if (phaseIs("respond")) {
//            arrayListMsg alm = new arrayListMsg("Respond " + ID, respondMsg);
  //          m.add(makeContent(OUT_DECISION, alm));
        } else {
//            arrayListMsg nm = new arrayListMsg("WhatYouHave", whatYouHaveMsg);
//            m.add(makeContent(OUT_NODES, nm));
        }
        return m;
    }
    
    
}