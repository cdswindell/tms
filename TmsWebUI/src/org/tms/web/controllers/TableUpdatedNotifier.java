package org.tms.web.controllers;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/tableUpdated")
public class TableUpdatedNotifier
{	 
    @OnMessage(encoders = {JSONEncoder.class})
    public String onMessage(String msg) 
    {
        return msg;
    }
}
