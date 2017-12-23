package com.example.naser.thehelper.historyRecyclerView;

/**
 * Created by Naser on 12/15/2017.
 */

public class HistoryObject {
    private String homeId , time;

    public HistoryObject(String homeId , String time){
        this.homeId=homeId;
        this.time = time ;
    }
    public String getHomeId(){return homeId;}
    public void setHomeId(String homeId){this.homeId=homeId;}

    public String getTime(){return time;}
    public void setTime(String time){this.time=time;}


}
