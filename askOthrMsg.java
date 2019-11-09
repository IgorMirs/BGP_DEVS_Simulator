package BGP_Simulation_temp;

import java.util.Vector;

import GenCol.entity;

public class askOthrMsg extends entity
{
//    protected int source;
    protected int dest;
//    protected int message;
//    protected int TTL;
//    protected int msgID;
//    protected Vector<Vector<Integer>> msgBag = new Vector<Vector<Integer>>();
    public askOthrMsg(String name, int dest_)
    {
        this.name=name;
        dest = dest_;
    }
}

