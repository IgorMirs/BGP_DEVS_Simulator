package BGP_Simulation_v04_Internal_decisions;

import java.util.*;

import GenCol.entity;

public class WhatYouHaveMsg extends entity
{
    protected ArrayList<Object> msgToSend = new ArrayList<Object>();
    public WhatYouHaveMsg(String name, ArrayList<Object> msgToSend_)
    {
        this.name=name;
        msgToSend = msgToSend_;
    }
}

