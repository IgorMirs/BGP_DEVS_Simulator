package BGP_Simulation_temp;

import java.util.Vector;

import GenCol.entity;

public class nodeMsg extends entity
{
   /* protected int source;
    protected int dest;
    protected int message;
    protected int TTL;
    protected int msgID;*/
    protected Vector<Vector<Integer>> msgBag = new Vector<Vector<Integer>>();
    public nodeMsg(String name, Vector<Vector<Integer>> msgBag_value)
    {
        this.name=name;
        this.msgBag = msgBag_value;
    }
}

