package BGP_Simulation_v03_Internal_decisions;

import java.util.*;


import model.modeling.message;
import view.modeling.ViewableAtomic;

public class FromNodes extends MsgReceiver
{
    static final public String IN_DECISION = "in_decs";
    
    protected Vector<ArrayList<Object>> respondMatrix;  

    
    public FromNodes(String name, int id, NetStat netStat_, NodeInput nodeInput_, NodeNotRespond notRespond_) {
        super(name);
        addInport(INPORT);
        addInport(IN_DECISION);
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
        srcID = new Vector<Integer>();

        seqNumber = new Vector<Vector<Integer>>();
        receivedMsg = new Stack<ArrayList<Object>>();
        whatYouHaveMsg = new ArrayList<Object>();
        respondMsg = new Vector<ArrayList<Object>>();
        respondMatrix = new Vector<ArrayList<Object>>();
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
                   ArrayList <Object> whatDoYouHave = ((WhatYouHaveMsg) x.getValOnPort(INPORT, i)).msgToSend;
                   //get received message ID
                   int msgID = getMsgID(whatDoYouHave);
                   //if the node is traitor - he do not ask other nodes
                   if (type == 1) {
                       nodeDecision = makeFakeMsg(netStat.msg);
                       respondMsg = new Vector<ArrayList<Object>>();
                       crtRespondMsg(msgID);
                       System.out.println("Respond of node " + ID + " is " + respondMsg);
                       holdIn("respond", 0);
                       break;
                   }
                   //get number of traitors from the received message
                   int nTraitors = getnTraitors(whatDoYouHave);
                   //check how many traitors in the message
                   //if there are still traitors
                   if (nTraitors > 0) {
//                       if (ID == 2) {
                         //take the source ID array from the received from the commander msg
                           Vector<Integer> tempSrcID = new Vector<Integer>();
                           tempSrcID = getSrcID(whatDoYouHave);
                           System.out.println("SOURCE ID BEFORE ADDING " + ID + " " + srcID);
                           for (int j = 0; j < tempSrcID.size(); j++) {
                               srcID.add(tempSrcID.elementAt(j));
                           }
                           //creating what you have msg
                           srcID.add(ID);
                           System.out.println("SOURCE ID AFTER ADDING " + ID + " " + srcID);
                           whatYouHaveMsg = new ArrayList<Object>();
                           //add new msg id
                           int newMsgId = netStat.getMsgId();
                           //create msg to ask other nodes
                           crtWhatYouHaveMsg(newMsgId, srcID, nTraitors);
                           
                           System.out.println("From Nodes WYH " + ID + " " + whatYouHaveMsg);
                           //create new respond matrix
                           respondMatrix = new Vector<ArrayList<Object>>();
                           Vector<Vector<Integer>> tempRM = addRespondMatrix(newMsgId);
                           ArrayList tempAL = new ArrayList();
                           //add to the list msgID on which the node should respond
                           tempAL.add(msgID);
                           //add to the list respond matrix
                           tempAL.add(tempRM);
                           respondMatrix.add(tempAL);
                           
                           System.out.println("Respond matrix " + ID + " " + respondMatrix);
                           holdIn("calc_decs", 0);
//                       }
                       
                   } 
                   //if there is no traitors in the message - respond the decision
                   else {
                       //create respond message
                         respondMsg = new Vector<ArrayList<Object>>();
                         crtRespondMsg(msgID);
                         System.out.println("Respond of node " + ID + " is " + respondMsg);
                         holdIn("respond", 0);
                   }
                } //end if INPORT
                
                if (messageOnPort(x, IN_DECISION, i) && phaseIs("calc_decs")/*&& type != 1 && (notRespond.notRespond != true)*/) {
                    Vector<ArrayList <Object>> decision = ((arrayListMsg) x.getValOnPort(IN_DECISION, i)).msgToSend;
                    int dmsgID = (Integer) decision.elementAt(0).get(0);
                    int responderID = (Integer) decision.elementAt(0).get(1);
                    int decs = (Integer) decision.elementAt(0).get(2);
                    checkRespond(dmsgID, responderID, decs);
                    System.out.println("Check respond matrix " + ID + " " + respondMatrix);
                    Vector<Vector<Integer>> responds = iSFinishedToCalc();
                    if (!responds.isEmpty()) {
                        System.out.println("Responds " + responds);
                        ArrayList<Object>  tempRespondMsg = new ArrayList<Object>();
                        tempRespondMsg.add(responds.elementAt(0).elementAt(0));
                        tempRespondMsg.add(ID);
                        tempRespondMsg.add(responds.elementAt(0).elementAt(1));
                        respondMsg.add(tempRespondMsg);
                        System.out.println("Respond of node " + ID + " is " + respondMsg);
                        holdIn("respond", 0);
                    }
                }
            }
    } //end deltext()
    
    public void deltint() {
        holdIn(phase, INFINITY);
    }
       
    public message out() {
        message m = new message();
        
        if (phaseIs("respond")) {
            arrayListMsg alm = new arrayListMsg("Respond of " + ID , respondMsg);
            m.add(makeContent(OUT_DECISION, alm));
        } else {
            WhatYouHaveMsg nm = new WhatYouHaveMsg("WhatYouHave", whatYouHaveMsg);
            m.add(makeContent(OUT_NODES, nm));
        }
        return m;
    }
    
    public void checkRespond(int msgID, int nodeID, int decs) {
        for (int i = 0; i < respondMatrix.size(); i++) {
            //put the respond matrix to temp
            Vector<Vector<Integer>> temp = (Vector<Vector<Integer>>) respondMatrix.elementAt(i).get(1);
            for (int j = 0; j < temp.size(); j++) {    
                if (temp.elementAt(j).elementAt(0) == msgID && temp.elementAt(j).elementAt(1) == nodeID) {
                    temp.elementAt(j).set(2, decs);
                    break;
                }
            }
        }
    }
    
    public Vector<Vector<Integer>> iSFinishedToCalc() {
        Vector<Vector<Integer>> result = new Vector<Vector<Integer>>();  
        for (int i = 0; i < respondMatrix.size(); i++) {
            Vector<Integer> decisions = new Vector<Integer>();
            boolean finished = true;
            Vector<Vector<Integer>> temp = (Vector<Vector<Integer>>) respondMatrix.elementAt(i).get(1);
            for (int j = 0; j < temp.size(); j++) {
                if (temp.elementAt(i).elementAt(2) == defaultDes) {
                    finished = false;
                    break;
                }
                decisions.add(temp.elementAt(i).elementAt(2));
            }
        
            if (finished) {
                //add the original input from the commander
                decisions.add(input.elementAt(0));
                Vector<Integer> v = new Vector<Integer>();
                //add the msgID on which respond
                v.add((Integer) respondMatrix.elementAt(i).get(0));
                v.add(majority(decisions));
                result.add(v);
                System.out.println("Respond matrix " + respondMatrix);
                System.out.println("Decision " + majority(decisions));
                System.out.println("Result " + ID + " " + result);
            }
        }
        return result;
    }
    
}