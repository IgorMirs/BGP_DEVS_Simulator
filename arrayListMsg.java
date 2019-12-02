package BGP_Simulation_v03_Internal_decisions;

import java.util.*;

import GenCol.entity;

public class arrayListMsg extends entity
{
    protected Vector<ArrayList<Object>> msgToSend = new Vector<ArrayList<Object>>();
    public arrayListMsg(String name, Vector<ArrayList<Object>> msgToSend_)
    {
        this.name=name;
        msgToSend = msgToSend_;
    }
}

