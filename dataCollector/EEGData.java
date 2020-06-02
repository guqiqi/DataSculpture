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
}
