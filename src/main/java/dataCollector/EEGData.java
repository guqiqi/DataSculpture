package dataCollector;

import java.sql.Timestamp;

public class EEGData {
    Timestamp occurTime;

    int attention;
    int meditation;
    int theta;
    int delta;
    int lowAlpha;
    int highAlpha;
    int lowBeta;
    int highBeta;
    int lowGamma;
    int highGamma;

    public EEGData(){

    }

    public EEGData(Timestamp occurTime, int attention, int meditation, int theta, int delta, int lowAlpha,
                   int highAlpha, int lowBeta, int highBeta, int lowGamma, int highGamma) {
        this.occurTime = occurTime;
        this.attention = attention;
        this.meditation = meditation;
        this.theta = theta;
        this.delta = delta;
        this.lowAlpha = lowAlpha;
        this.highAlpha = highAlpha;
        this.lowBeta = lowBeta;
        this.highBeta = highBeta;
        this.lowGamma = lowGamma;
        this.highGamma = highGamma;
    }

    public Timestamp getOccurTime () {
        return occurTime;
    }

    public int getAttention () {
        return attention;
    }

    public int getMeditation () {
        return meditation;
    }

    public int getTheta () {
        return theta;
    }

    public int getDelta () {
        return delta;
    }

    public int getLowAlpha () {
        return lowAlpha;
    }

    public int getHighAlpha () {
        return highAlpha;
    }

    public int getLowBeta () {
        return lowBeta;
    }

    public int getHighBeta () {
        return highBeta;
    }

    public int getLowGamma () {
        return lowGamma;
    }

    public int getHighGamma () {
        return highGamma;
    }
}
