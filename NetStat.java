package BGP_Simulation_temp;

import java.util.Vector;

public class NetStat
{
    protected int [] traitorVec;
    protected int msg;
    protected int nNodes;
    
    public NetStat(int[] tv, int msg_, int nNodes_) {
        traitorVec = tv;
        msg = msg_;
        nNodes = nNodes_;
    }
}
