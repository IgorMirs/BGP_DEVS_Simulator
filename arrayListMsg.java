package BGP_Simulation_v05_NetworkTopology;

import java.util.*;

import GenCol.entity;

public class arrayListMsg extends entity
{
    protected ArrayList<Object> msgToSend = new ArrayList<Object>();
    public arrayListMsg(String name, ArrayList<Object> msgToSend_)
    {
        this.name=name;
        msgToSend = msgToSend_;
    }
}

