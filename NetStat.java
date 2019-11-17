package BGP_Simulation_git;

import java.util.Vector;

public class NetStat
{
    protected int [] traitorVec;
    protected int msg;
    protected int nNodes;
    protected int nTraitors;
    
    public double time = 0;
    
    public NetStat(int[] tv, int msg_, int nNodes_, int nTraitors_) {
        traitorVec = tv;
        msg = msg_;
        nNodes = nNodes_;
        nTraitors = nTraitors_;
    }
}
