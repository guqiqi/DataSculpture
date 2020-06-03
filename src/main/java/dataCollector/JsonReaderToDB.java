package dataCollector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Timestamp;

public class JsonReaderToDB {
    static String fileName = "like" ;
    static String thinkgearHost = "127.0.0.1";
    static int thinkgearPort = 13854;
    static String command = "{\"enableRawOutput\": false, \"format\": \"Json\"}\n";
    public static InputStream input;
    public static OutputStream output;
    public static BufferedReader reader;
    public static boolean flag = true;
    public static String timeStamp;
    public static String poorSignalLevel;
    public static int attention;
    public static int meditation;
    public static int delta;
    public static int theta;
    public static int lowAlpha;
    public static int highAlpha;
    public static int lowBeta;
    public static int highBeta;
    public static int lowGamma;
    public static int highGamma;
    public static String blinkStrength;
    public static DBWriterAndReader dbWriterAndReader;
    public static void main(String[] args) throws IOException, JSONException {
        dbWriterAndReader = DBWriterAndReader.getInstance();

        System.out.println("Connecting to host = " + thinkgearHost + ", port = " + thinkgearPort);
        Socket clientSocket = new Socket(thinkgearHost, thinkgearPort);
        input = clientSocket.getInputStream();
        output = clientSocket.getOutputStream();
        System.out.println("Sending command "+command);
        write(command);
        reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));

        while(true == flag) {
            clientEvent();
        }
        clientSocket.close();
    }
    public static void write(String data) {
        try {
            output.write(data.getBytes());
            output.flush();
        } catch (Exception e) { // null pointer or serial port dead
            e.printStackTrace();
        }}
    public static void clientEvent() throws NumberFormatException, IOException {
//		System.out.println(reader.readLine());
        // Sample JSON data:
        // {"eSense":{"attention":91,"meditation":41},"eegPower":{"delta":1105014,"theta":211310,"lowAlpha":7730,"highAlpha":68568,"lowBeta":12949,"highBeta":47455,"lowGamma":55770,"highGamma":28247},"poorSignalLevel":0}
        if (reader.ready()) {
            System.out.println("ready");
            String jsonText = reader.readLine();
            System.out.println(jsonText);
            java.util.Date date = new java.util.Date();
            timeStamp = ""+new Timestamp(date.getTime());
            try {
                String uniText = "";
                JSONObject json = new JSONObject(jsonText);
                if(json.has("blinkStrength")){
                    blinkStrength = json.getString("blinkStrength");
                }
                else{
                    poorSignalLevel = json.getString("poorSignalLevel");

                    JSONObject esense = json.getJSONObject("eSense");
                    if (esense != null) {
                        attention = esense.getInt("attention");
                        meditation = esense.getInt("meditation");
                    }

                    JSONObject eegPower = json.getJSONObject("eegPower");
                    if (eegPower != null) {
                        delta = eegPower.getInt("delta");
                        theta = eegPower.getInt("theta");
                        lowAlpha = eegPower.getInt("lowAlpha");
                        highAlpha = eegPower.getInt("highAlpha");
                        lowBeta = eegPower.getInt("lowBeta");
                        highBeta = eegPower.getInt("highBeta");
                        lowGamma = eegPower.getInt("lowGamma");
                        highGamma = eegPower.getInt("highGamma");
                    }
//                    uniText = attention+";"+meditation+";"+delta+";"+theta+";"+lowAlpha+";"+highAlpha+";"+lowBeta+";"+highBeta+";"+lowGamma+";"+highGamma+";NA";
                    writeDB(attention, meditation, theta, delta, lowAlpha, highAlpha,
                    lowBeta, highBeta, lowGamma, highGamma);
                }

            }
            catch (JSONException e) {
                System.out.println("There was an error parsing the JSONObject." + e);
            };
        }
    }
    public static void writeDB(int attention, int meditation, int mytheta, int mydelta, int mylow_alpha, int myhigh_alpha,
                                  int mylow_beta, int myhigh_beta, int mylow_gamma, int myhigh_gamma) {
        dbWriterAndReader.insert(attention, meditation, mytheta, mydelta, mylow_alpha, myhigh_alpha, mylow_beta, myhigh_beta, mylow_gamma, myhigh_gamma);
    }
}
