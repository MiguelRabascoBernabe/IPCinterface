package app;

import upv.ipc.sportlib.SportActivityApp;

public class Context{
    SportActivityApp app;
    public Context(){
        app=SportActivityApp.getInstance();
    }
    public SportActivityApp getApp(){return app;}
}