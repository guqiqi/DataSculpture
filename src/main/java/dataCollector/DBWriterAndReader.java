package dataCollector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBWriterAndReader {
    public static final String url = "jdbc:mysql://rm-bp1024wxq24xe9l28no.mysql.rds.aliyuncs.com:3306/data_sculpture";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "sculpture";
    public static final String password = "DataSculpture1";

    private static DBWriterAndReader dbWriterAndReader;
    private Connection conn = null;

    private DBWriterAndReader() {
        try {
            Class.forName(name);//指定连接类型
            conn = DriverManager.getConnection(url, user, password);//获取连接
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DBWriterAndReader getInstance() {
        if (dbWriterAndReader == null) {
            dbWriterAndReader = new DBWriterAndReader();
        }

        return dbWriterAndReader;
    }

    public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(int attention, int meditation, int mytheta, int mydelta, int mylow_alpha, int myhigh_alpha,
                       int mylow_beta, int myhigh_beta, int mylow_gamma, int myhigh_gamma) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into eeg_data(occur_time, attention, meditation, " +
                    "delta, theta, low_alpha, high_alpha, low_beta, high_beta, low_gamma, high_gamma)" +
                    " values(?,?,?,?,?,?,?,?,?,?,?)");
            System.out.println(System.currentTimeMillis());
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));

            ps.setInt(2, attention);
            ps.setInt(3, meditation);
            ps.setInt(4, mytheta);
            ps.setInt(5, mydelta);
            ps.setInt(6, mylow_alpha);
            ps.setInt(7, myhigh_alpha);
            ps.setInt(8, mylow_beta);
            ps.setInt(9, myhigh_beta);
            ps.setInt(10, mylow_gamma);
            ps.setInt(11, myhigh_gamma);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public EEGData readLastData(){
        EEGData eegData = new EEGData();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM eeg_data ORDER BY id DESC LIMIT 1");
            ResultSet rs=ps.executeQuery();
            while (rs.next()) {
                eegData = new EEGData(rs.getTimestamp(2), rs.getInt(3), rs.getInt(4),
                        rs.getInt(5), rs.getInt(6),rs.getInt(7), rs.getInt(8),rs.getInt(9), rs.getInt(10),
                        rs.getInt(11), rs.getInt(12));
                break;
            }


        }catch (SQLException e){
            e.printStackTrace();
        }

        return eegData;
    }

    public List<BraceletData> readLastBraceletData(){
        List<BraceletData> braceletDataList = new ArrayList<BraceletData>();
        BraceletData braceletData;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM record ORDER BY id DESC LIMIT 50");
            ResultSet rs=ps.executeQuery();
            while (rs.next()) {
                braceletData = new BraceletData(rs.getFloat("temp"), rs.getInt("br"), rs.getInt("bo"));
                braceletDataList.add(braceletData);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return braceletDataList;
    }
}
