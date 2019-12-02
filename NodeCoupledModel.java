package BGP_Simulation_v03_Internal_decisions;

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
    public GetDecision getDecision;
    
    public NodeCoupledModel(String name, int id, NetStat netStat_) {
        super(name);
        
        NodeInput nodeInput = new NodeInput();
        NodeNotRespond notRespond = new NodeNotRespond();
        fromCommander = new FromCommader("fromCommander", id, netStat_, nodeInput, notRespond);
        fromNodes = new FromNodes("fromNodes", id, netStat_, nodeInput, notRespond);
        //getDecision = new GetDecision("getDecision", id, netStat_, nodeInput);

        add(fromCommander);
        add(fromNodes);
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
        addCoupling(this, this.IN_NODES, fromNodes, fromNodes.INPORT);
        addCoupling(fromNodes, fromNodes.OUT_DECISION, this, this.OUT_DECISION);
        addCoupling(fromNodes, fromNodes.OUT_NODES, this, this.OUT_NODES);
        addCoupling(this, this.IN_DECISION, fromNodes, fromNodes.IN_DECISION);
       // addCoupling(this, this.IN_DECISION, getDecision, getDecision.INPORT);
    }

    public void layoutForSimView() {
        preferredSize = new Dimension(370, 300);
        ((ViewableComponent) withName("fromCommander")).setPreferredLocation(new Point(0, 30));
        ((ViewableComponent) withName("fromNodes")).setPreferredLocation(new Point(0, 210));
    }
}
