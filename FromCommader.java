package BGP_Simulation_v05_NetworkTopology;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class FromCommader extends MsgReceiver
{
    static final public String IN_DECISION = "in_decs";
    protected ArrayList<Object> fromCommander;

    
    public FromCommader(String name, int id, NetStat netStat_, NodeInput nodeInput_, NodeNotRespond notRespond_) {
        super(name);
        addInport(INPORT);
        addInport(IN_DECISION);
        addOutport(OUT_COMMANDER);
        addOutport(OUT_NODES);
        this.ID = id;
        netStat = netStat_;
        input = nodeInput_.input;
        notRespond = notRespond_;
    }
    
    public void initialize() {
        //defining phase and sigma is equal to passivate()
        phase = "passive";
        sigma = INFINITY;
//        input = new Vector<Integer>();
        
        seqNumber = new Vector<Vector<Integer>>();
        whatYouHaveMsg = new ArrayList<Object>();
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
                   fromCommander = ((WhatYouHaveMsg) x.getValOnPort(INPORT, i)).msgToSend;
                   //put the received msg from the commander to the input
                   input.add(getMsgValue(fromCommander));
                   System.out.println("ID " + ID + " input " + input);
                   
                   //if the node is traitor - he do not ask other nodes
                   if (type == 1) {
                       nodeDecision = makeFakeMsg(netStat.msg);
                       //if the message wasn't previously changed
                       if (getMsgValue(fromCommander) != makeFakeMsg(netStat.msg)) {
                           ArrayList<Object> temp = fromCommander;
                           fromCommander = new ArrayList<Object>();
                           for (int k = 0; k < temp.size(); k++) {
                               if (k == 2)
                                   fromCommander.add(makeFakeMsg(netStat.msg));
                               else
                                   fromCommander.add(temp.get(k)); 
                           }
                       }
                       
                       
                       holdIn("transfer", 0);
                       break;
                   }
                
                   //get number of traitors from the received message
                   int nTraitors = getnTraitors(fromCommander);
                   //check how many traitors in the message
                   if (nTraitors > 0) {
                       //take the source ID array from the received from the commander msg
                       srcID = new Vector<Integer>();
                       Vector<Integer> tempSrcID = new Vector<Integer>();
                       tempSrcID = getSrcID(fromCommander);
                       for (int j : tempSrcID) {
                           srcID.add(tempSrcID.elementAt(j));
                       }
                       //add node ID to the source array
                       srcID.add(ID);
                       
                       //get received message ID
                       int msgID = getMsgID(fromCommander);
                       
                       //creating what you have msg
                       whatYouHaveMsg = new ArrayList<Object>();
                       //add new msg id
                       int newMsgId = netStat.getMsgId();
                       //create "what you have" msg to ask other nodes
    //                                   new ID,   list of nodes, num of traitors, level (always send to the highest from Nodes level)    
                       crtWhatYouHaveMsg(newMsgId, srcID, nTraitors, 0);
                       
                       System.out.println("WYH " + ID + " " + whatYouHaveMsg);
                       
                       //create the matrix to receive responds
                       respondMatrix = new Vector<Vector<Integer>>();
                       crtRespondMatrix(newMsgId);
                         
                       System.out.println("Respond matrix " + ID + " " + respondMatrix);
                       holdIn("transfer", 0);
                   } 
                   //if there is no traitors in the message - just calc the decision
                   else {
                       holdIn("transfer", 0);
                       nodeDecision = inputMajority();
                   }
                } //end if phaseIs("passive");
            } //end if INPORT
            
            //if the fromCommander get decision
            if (messageOnPort(x, IN_DECISION, i) && phaseIs("calc_decs")) {
                ArrayList <Object> decision = ((arrayListMsg) x.getValOnPort(IN_DECISION, i)).msgToSend;
                int dmsgID = (Integer) decision.get(0);
                int responderID = (Integer) decision.get(1);
                int decs = (Integer) decision.get(2);
                checkRespond(dmsgID, responderID, decs);
                if (isFinishedToCalc()) {
                    notRespond.notRespond = false;
                    passivate();
                }
                System.out.println("Respond matrix after getting decision " + ID + " " + respondMatrix);
            }
        }
    } //end deltext()
    
  
    
    public void deltint() {
        if (phaseIs("transfer")) {
            if (netStat.nTraitors > 0 && type == 0) {
                holdIn("transfered", ID);
            }
            else 
                holdIn("transfered", INFINITY);
        }
        else if (phaseIs("transfered") && Double.valueOf(getFormattedTN()) == ID) {
            notRespond.notRespond = true;
            holdIn("calc_decs", 0);
        }
        else
            holdIn(phase, INFINITY);
    }
       
    public message out() {
        message m = new message();
        
        if (phaseIs("calc_decs")) {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("WhatYouHave", whatYouHaveMsg);
            m.add(makeContent(OUT_NODES, nm));
        }
        else if (phaseIs("transfer")) {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("TRANSFER", fromCommander);
            m.add(makeContent(OUT_COMMANDER, nm));
        }
        
        return m;
    }
    
    
    
    
}
