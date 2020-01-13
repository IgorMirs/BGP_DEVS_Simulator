package BGP_Simulation_v05_NetworkTopology;

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
    public DCS_Router dcsRouter;
    public int ID;
    public NetStat netStat;
    
    public NodeCoupledModel(String name, int id, NetStat netStat_) {
        super(name);
        this.ID = id;
        netStat = netStat_;
        NodeInput nodeInput = new NodeInput();
        NodeNotRespond notRespond = new NodeNotRespond();
        fromCommander = new FromCommader("fromCommander", id, netStat_, nodeInput, notRespond);
        dcsRouter = new DCS_Router("DSC_Router", id, netStat_, nodeInput, notRespond);
        FromNodes[] arr = new FromNodes[netStat_.nTraitors];
        WYH_Router[] routers = new WYH_Router[netStat_.nTraitors];
        for (int i = 0; i < netStat_.nTraitors; i++) {
            arr[i] = new FromNodes("fromNodes" + i, id, netStat_, nodeInput, notRespond, i);
            routers[i] = new WYH_Router("WYH_Router " + i, id, netStat_, nodeInput, notRespond, i);
            add(arr[i]);
            add(routers[i]);
        }
        
        //getDecision = new GetDecision("getDecision", id, netStat_, nodeInput);

        add(fromCommander);
        add(dcsRouter);
        
        addInport(IN_COMMANDER);
        addOutport(OUT_COMMANDER);
        addInport(IN_NODES);
        addOutport(OUT_NODES);
        addInport(IN_DECISION);
        addOutport(OUT_DECISION);

        addCoupling(this, this.IN_COMMANDER, fromCommander, fromCommander.INPORT);
        addCoupling(this, this.IN_DECISION, dcsRouter, dcsRouter.INPORT);
        addCoupling(dcsRouter, dcsRouter.OUT_NODES, fromCommander, fromCommander.IN_DECISION);
        addCoupling(dcsRouter, dcsRouter.OUT_DECISION, this, this.OUT_DECISION);
        addCoupling(fromCommander, fromCommander.OUT_COMMANDER, this, this.OUT_COMMANDER);
        addCoupling(fromCommander, fromCommander.OUT_NODES, this, this.OUT_NODES);
        for (int i = 0; i < netStat_.nTraitors; i++) {
            addCoupling(this, this.IN_NODES, routers[i], routers[i].INPORT);
            addCoupling(arr[i], arr[i].OUT_DECISION, this, this.OUT_DECISION);
            addCoupling(routers[i], routers[i].OUT_NODES, this, this.OUT_NODES);
            addCoupling(arr[i], arr[i].OUT_NODES, this, this.OUT_NODES);
            addCoupling(dcsRouter, dcsRouter.OUT_NODES, arr[i], arr[i].IN_DECISION);
            addCoupling(routers[i], routers[i].OUT_FROMNODES, arr[i], arr[i].INPORT);
        }
    }

    public void layoutForSimView() {
        preferredSize = new Dimension(450, 450);
        int x = 10, y = 130;
        ((ViewableComponent) withName("fromCommander")).setPreferredLocation(new Point(0, 30));
        ((ViewableComponent) withName("DSC_Router")).setPreferredLocation(new Point(200, 30));
        for (int i = 0; i < netStat.nTraitors; i++) {
            if (x > 700) {
                y += 300;
                x = 10;
            } 
            ((ViewableComponent)withName(String.format("fromNodes%d", i))).setPreferredLocation(new Point(x, y));
            x += 100;
        }
    }
}
