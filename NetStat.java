package BGP_Simulation_v05_NetworkTopology;

import java.util.Vector;

public class NetStat
{
    protected int [] traitorVec;
    protected int msg;
    protected int nNodes;
    protected int nTraitors;
    protected int msgId = 0;  //each time the sender will put this id to his message
    public Vector<Integer> srcID = new Vector<Integer>(); //the vector with the IDs of sending nodes
    
    public double time = 0;
    
    public NetStat(int[] tv, int msg_, int nNodes_, int nTraitors_) {
        traitorVec = tv;
        msg = msg_;
        nNodes = nNodes_;
        nTraitors = nTraitors_;
    }
    
    /**
     * Return msg id and each time increase it
     * 
     */
    public int getMsgId() {
        return ++msgId;
    }
}
