package BGP_Simulation_v04_Internal_decisions;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class FromNodes extends MsgReceiver
{
    static final public String IN_DECISION = "in_decs";
    
    protected ArrayList<Object> respondMatrix;  
    int level;
    
    public FromNodes(String name, int id, NetStat netStat_, NodeInput nodeInput_, NodeNotRespond notRespond_, int level_) {
        super(name);
        addInport(INPORT);
        addInport(IN_DECISION);
        addOutport(OUT_NODES);
        addOutport(OUT_DECISION);
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
            if (messageOnPort(x, INPORT, i) && (notRespond.notRespond != true)) {
               ArrayList<Object> whatDoYouHave = ((WhatYouHaveMsg) x.getValOnPort(INPORT, i)).msgToSend;
               //get the level value
               int levelValue = getLevel(whatDoYouHave);
               //from node checks the message only for it's level
               if (levelValue == level) { 
                   //get received message ID
                   int msgID = getMsgID(whatDoYouHave);
                   //if the node is traitor - he do not ask other nodes
                   if (type == 1) {
                       nodeDecision = makeFakeMsg(netStat.msg);
                       respondMsg = new ArrayList<Object>();
                       crtRespondMsg(msgID);
//                       System.out.println("Respond of node " + ID + " is " + respondMsg);
                       holdIn("respond", 0);
                   } 
                   else {
                       //get number of traitors from the received message
                       int nTraitors = getnTraitors(whatDoYouHave);
                       //check how many traitors in the message
                       //if there are still traitors
                       if (nTraitors > 0) {
                           //take the source ID array from the received from the commander msg
                           srcID = new Vector<Integer>();    
                           Vector<Integer> tempSrcID = new Vector<Integer>();
                           tempSrcID = getSrcID(whatDoYouHave);
                           for (int j = 0; j < tempSrcID.size(); j++) {
                               srcID.add(tempSrcID.elementAt(j));
                           }
                           //creating what you have msg
                           srcID.add(ID);
                           whatYouHaveMsg = new ArrayList<Object>();
                           //add new msg id
                           int newMsgId = netStat.getMsgId();
                           //create msg to ask other nodes
                           crtWhatYouHaveMsg(newMsgId, srcID, nTraitors, level + 1);
                           
                           System.out.println("From Nodes WYH " + ID + " " + whatYouHaveMsg);
                           //create new respond matrix
                           respondMatrix = new ArrayList<Object>();
                           Vector<Vector<Integer>> tempRM = addRespondMatrix(newMsgId);
                           //add to the list msgID on which the node should respond
                           respondMatrix.add(msgID);
                           //add to the list respond matrix
                           respondMatrix.add(tempRM);
                           
                           System.out.println("FROM NODES Respond matrix " + ID + " " + respondMatrix);
                           holdIn("calc_decs", ID * countDelay());
                       
                       } 
                       //if there is no traitors in the message - respond the decision
                       else {
                           //create respond message
                             respondMsg = new ArrayList<Object>();
                             crtRespondMsg(msgID);
                             System.out.println("Respond of node " + ID + " is " + respondMsg);
                             holdIn("respond", 0);
                       }
                   }
               }
            } //end if INPORT
            
            if (messageOnPort(x, IN_DECISION, i) && phaseIs("calc_decs")/*&& type != 1 && (notRespond.notRespond != true)*/) {
                ArrayList <Object> decision = ((arrayListMsg) x.getValOnPort(IN_DECISION, i)).msgToSend;
                //get the values from the received decision
                int dmsgID = (Integer) decision.get(0);
                int responderID = (Integer) decision.get(1);
                int decs = (Integer) decision.get(2);
                //check in the respond matrix does the node waits for the decision on the 
                //given msg ID from the given responder
                checkRespond(dmsgID, responderID, decs);
                //create the respond of the node
                Vector<Integer> respond = iSFinishedToCalc();
                //if the respond is ready the node sends it
                if (!respond.isEmpty()) {
//                    System.out.println("Responds " + respond);
                    respondMsg = new ArrayList<Object>();
                    respondMsg.add(respond.elementAt(0));
                    respondMsg.add(ID);
                    respondMsg.add(respond.elementAt(1));
//                    System.out.println("Respond of node " + ID + " is " + respondMsg);
                    holdIn("respond", 0);
                }
            }
        }
    } //end deltext()
    
    public void deltint() {
        if (phaseIs("respond"))
            passivate();
        else 
            holdIn(phase, INFINITY);
    }
       
    public message out() {
        message m = new message();
        
        if (phaseIs("respond")) {
            arrayListMsg alm = new arrayListMsg("Respond of " + ID , respondMsg);
            m.add(makeContent(OUT_DECISION, alm));
        } else {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("WhatYouHave" + ID, whatYouHaveMsg);
            m.add(makeContent(OUT_NODES, nm));
        }
        return m;
    }
    
    public void checkRespond(int msgID, int nodeID, int decs) {
        for (int i = 0; i < respondMatrix.size(); i++) {
            //put the respond matrix to temp
            Vector<Vector<Integer>> temp = (Vector<Vector<Integer>>) respondMatrix.get(1);
            for (int j = 0; j < temp.size(); j++) {    
                if (temp.elementAt(j).elementAt(0) == msgID && temp.elementAt(j).elementAt(1) == nodeID) {
                    temp.elementAt(j).set(2, decs);
                    break;
                }
            }
        }
    }
    
   
    public Vector<Integer> iSFinishedToCalc() {
        Vector<Integer> result = new Vector<Integer>();  
            Vector<Integer> decisions = new Vector<Integer>();
            boolean finished = true;
            Vector<Vector<Integer>> temp = (Vector<Vector<Integer>>) respondMatrix.get(1);
            for (int j = 0; j < temp.size(); j++) {
                if (temp.elementAt(j).elementAt(2) == defaultDes) {
                    finished = false;
                    break;
                }
                decisions.add(temp.elementAt(j).elementAt(2));
            }
        
            if (finished) {
                //add the original input from the commander
                decisions.add(input.elementAt(0));
                //add the msgID on which respond
                result.add((Integer) respondMatrix.get(0));
                result.add(majority(decisions));
//                System.out.println("Respond matrix " + respondMatrix);
//                System.out.println("Decision " + majority(decisions));
//                System.out.println("Result " + ID + " " + result);
            }
        return result;
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