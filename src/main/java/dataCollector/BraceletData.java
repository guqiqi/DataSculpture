package dataCollector;

import java.sql.Timestamp;

public class BraceletData {
    int id;
    int tid;
    String uuid;
    float temp;
    int hr;
    int bo;
    Timestamp time;

    public BraceletData(){

    }

    public BraceletData(int id, int tid, String uuid, float temp, int hr, int bo, Timestamp time) {
        this.id = id;
        this.tid = tid;
        this.uuid = uuid;
        this.temp = temp;
        this.hr = hr;
        this.bo = bo;
        this.time = time;
    }

    public BraceletData(float temp, int hr, int bo) {
        this.temp = temp;
        this.hr = hr;
        this.bo = bo;
    }

    public float getTemp() {
        return temp;
    }

    public int getHr() {
        return hr;
    }

    public int getBo() {
        return bo;
    }

}
