package BGP_Simulation_v03_Internal_decisions;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class FromCommader extends MsgReceiver
{
    static final public String IN_DECISION = "in_decs";

    
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
        srcID = new Vector<Integer>();
        seqNumber = new Vector<Vector<Integer>>();
        receivedMsg = new Stack<ArrayList<Object>>();
        whatYouHaveMsg = new ArrayList<Object>();
        respondMsg = new Vector<ArrayList<Object>>();
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
               ArrayList <Object> fromCommander = ((WhatYouHaveMsg) x.getValOnPort(INPORT, i)).msgToSend;
               //put the received msg from the commander to the input
               input.add(getMsgValue(fromCommander));
               
               //if the node is traitor - he do not ask other nodes
               if (type == 1) {
                   nodeDecision = makeFakeMsg(netStat.msg);
                   passivate();
                   break;
               }
            
               //get number of traitors from the received message
               int nTraitors = getnTraitors(fromCommander);
               //check how many traitors in the message
               if (nTraitors > 0) {
                   //take the source ID array from the received from the commander msg
                   Vector<Integer> tempSrcID = new Vector<Integer>();
                   tempSrcID = getSrcID(fromCommander);
                   for (int j : tempSrcID) {
                       srcID.add(tempSrcID.elementAt(j));
                   }
                   //get received message ID
                   int msgID = getMsgID(fromCommander);
                   
                   //add to received message
                   ArrayList <Object> temp = new ArrayList <Object>();
                   temp.add(msgID);
                   temp.add(srcID);
                   receivedMsg.add(temp);
                   
                   //creating what you have msg
                   srcID.add(ID);
                   whatYouHaveMsg = new ArrayList<Object>();
                   //add new msg id
                   int newMsgId = netStat.getMsgId();
                   //create "what you have" msg to ask other nodes
                   crtWhatYouHaveMsg(newMsgId, srcID, nTraitors);
                   
                     //create the matrix to receive responds
                     respondMatrix = new Vector<Vector<Integer>>();
                     crtRespondMatrix(newMsgId);
                     
                     System.out.println("Respond matrix " + ID + " " + respondMatrix);
                     holdIn(phase, ID);
               } 
               //if there is no traitors in the message - just calc the decision
               else {
                   nodeDecision = inputMajority();
               }
            } //end if INPORT
            
            //if the fromCommander get decision
            if (messageOnPort(x, IN_DECISION, i) && phaseIs("calc_decs")) {
                Vector<ArrayList <Object>> decision = ((arrayListMsg) x.getValOnPort(IN_DECISION, i)).msgToSend;
                int dmsgID = (Integer) decision.elementAt(0).get(0);
                int responderID = (Integer) decision.elementAt(0).get(1);
                int decs = (Integer) decision.elementAt(0).get(2);
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
        if (phaseIs("passive") && Double.valueOf(getFormattedTN()) == ID) {
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
        
        return m;
    }
    
    
    
    
}
