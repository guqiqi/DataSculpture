import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Timestamp;

public class JsonReaderToFile {
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
	public static String attention;
	public static String meditation;
	public static String delta;
	public static String theta;
	public static String lowAlpha;
	public static String highAlpha;
	public static String lowBeta;
	public static String highBeta;
	public static String lowGamma;
	public static String highGamma;
	public static String blinkStrength;
	public static void main(String[] args) throws IOException, JSONException {
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
		}
	}
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
						attention = esense.getString("attention");
						meditation = esense.getString("meditation"); 
					}

					JSONObject eegPower = json.getJSONObject("eegPower");
					if (eegPower != null) {
						delta = eegPower.getString("delta");
						theta = eegPower.getString("theta"); 
						lowAlpha = eegPower.getString("lowAlpha");
						highAlpha = eegPower.getString("highAlpha");  
						lowBeta = eegPower.getString("lowBeta");
						highBeta = eegPower.getString("highBeta");
						lowGamma = eegPower.getString("lowGamma");
						highGamma = eegPower.getString("highGamma");
					}
					uniText = attention+";"+meditation+";"+delta+";"+theta+";"+lowAlpha+";"+highAlpha+";"+lowBeta+";"+highBeta+";"+lowGamma+";"+highGamma+";NA";
					writeFile(uniText);
				}

			}
			catch (JSONException e) {
				System.out.println("There was an error parsing the JSONObject." + e);
			};
		}
	}
	public static void writeFile(String text){
		try{
			File file = new File(".\\dataCollector\\dataFile\\"+fileName + ".txt");
			FileWriter fileWriter = new FileWriter(file,true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(text+"\r\n");
            bufferedWriter.close();
		}
		catch (Exception hata){
			hata.printStackTrace();
		}
	}
}
