package BGP_Simulator_v06_SignedMessages;

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

