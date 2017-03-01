package com.epicestgaming.gpsp;

import javax.jws.*;
import javax.jws.soap.*;
import javax.jws.soap.SOAPBinding.*;

@WebService

@SOAPBinding(style = Style.RPC)

public interface GPSPServerBean {

    @WebMethod
    int id();

    @WebMethod
    String getTime(int id);

    @WebMethod
    String currentVersion(int id);

}
