package BGP_Simulation_v04_Internal_decisions;

import java.awt.Dimension;
import java.awt.Point;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class NodeCoupledModel extends ViewableDigraph {
    static final public String IN_COMMANDER = "in_com";
    static final public String OUT_COMMANDER = "out_com";

    static final public String IN_NODES = "in_nod";
    static final public String OUT_NODES = "out_nod";
    
    static final public String IN_DECISION = "in_decs";
    static final public String OUT_DECISION = "out_decs";
    
    public FromCommader fromCommander;
    public FromNodes fromNodes;
//    public GetDecision getDecision;
    public int ID;
    
    public NodeCoupledModel(String name, int id, NetStat netStat_) {
        super(name);
        this.ID = id;
        NodeInput nodeInput = new NodeInput();
        NodeNotRespond notRespond = new NodeNotRespond();
        fromCommander = new FromCommader("fromCommander", id, netStat_, nodeInput, notRespond);
        FromNodes[] arr = new FromNodes[netStat_.nTraitors]; 
        for (int i = 0; i < netStat_.nTraitors; i++) {
            arr[i] = new FromNodes("fromNodes" + i, id, netStat_, nodeInput, notRespond, i);
            add(arr[i]);
        }
        
        //getDecision = new GetDecision("getDecision", id, netStat_, nodeInput);

        add(fromCommander);
      //  add(getDecision);
        
        addInport(IN_COMMANDER);
        addOutport(OUT_COMMANDER);
        addInport(IN_NODES);
        addOutport(OUT_NODES);
        addInport(IN_DECISION);
        addOutport(OUT_DECISION);

        addCoupling(this, this.IN_COMMANDER, fromCommander, fromCommander.INPORT);
        addCoupling(this, this.IN_DECISION, fromCommander, fromCommander.IN_DECISION);
        addCoupling(fromCommander, fromCommander.OUT_COMMANDER, this, this.OUT_COMMANDER);
        addCoupling(fromCommander, fromCommander.OUT_NODES, this, this.OUT_NODES);
        for (int i = 0; i < netStat_.nTraitors; i++) {
            addCoupling(this, this.IN_NODES, arr[i], arr[i].INPORT);
            addCoupling(arr[i], arr[i].OUT_DECISION, this, this.OUT_DECISION);
            addCoupling(arr[i], arr[i].OUT_NODES, this, this.OUT_NODES);
            addCoupling(this, this.IN_DECISION, arr[i], arr[i].IN_DECISION);
        }
    }

    public void layoutForSimView() {
        preferredSize = new Dimension(370, 300);
        ((ViewableComponent) withName("fromCommander")).setPreferredLocation(new Point(0, 30));
     //   ((ViewableComponent) withName("arr")).setPreferredLocation(new Point(0, 210));
    }
}
