package BGP_Simulation_v05_NetworkTopology;

import java.awt.Dimension;
import java.awt.Point;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class CommanderCoupledModel extends ViewableDigraph {
    static final public String OUT_COMMANDER = "out_com";

    static final public String IN_NODES = "in_nod";
    static final public String OUT_NODES = "out_nod";
    
    static final public String IN_DECISION = "in_decs";
    static final public String OUT_DECISION = "out_decs";
    
    public Observer observer;
    public Commander_Router cr;
    
    public CommanderCoupledModel(String name, int id, int msg_, NetStat netStat_, NodeCoupledModel[] network_) {
        super(name);
        //create the observer model
        observer = new Observer("Observer", id, msg_, netStat_, network_);
        //create the router model
        cr = new Commander_Router("Router", netStat_);

        add(observer);
        add(cr);
        
        addOutport(OUT_COMMANDER);
        addInport(IN_NODES);
        addOutport(OUT_NODES);
        addInport(IN_DECISION);
        addOutport(OUT_DECISION);
        
        addCoupling(observer, observer.OUT_PORT, this, this.OUT_COMMANDER);
        addCoupling(this, this.IN_NODES, cr, cr.IN_NODES);
        addCoupling(this, this.IN_DECISION, cr, cr.IN_DECISION);
        addCoupling(cr, cr.OUT_NODES, this, this.OUT_NODES);
        addCoupling(cr, cr.OUT_DECISION, this, this.OUT_DECISION);
    }

    public void layoutForSimView() {
        preferredSize = new Dimension(300, 200);
        ((ViewableComponent) withName("Observer")).setPreferredLocation(new Point(0, 10));
        ((ViewableComponent) withName("Router")).setPreferredLocation(new Point(0, 150));
    }
}
