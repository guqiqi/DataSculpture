package dataCollector;

public class Test {
    public static void main(String[] args) {
        DBWriterAndReader dbWriterAndReader = DBWriterAndReader.getInstance();

        dbWriterAndReader.insert(13, 24, 828561, 499878, 70992, 69242, 79538, 110103, 52597, 57468);
        dbWriterAndReader.insert(53, 57, 334895, 93353, 17819, 33215, 16593, 36286, 12217, 2018);

        EEGData eegData = dbWriterAndReader.readLastData();
        System.out.println(eegData.attention);
    }
}
