import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClientLog {
    List<String[]> listOfArrOfPairs = new ArrayList<>();


    public void log(int productNum, int amount) {
        String[] arrOfPairs = new String[2];
        arrOfPairs[0] = Integer.toString(productNum);
        arrOfPairs[1] = Integer.toString(amount);
        listOfArrOfPairs.add(arrOfPairs);
    }


    public void exportAsCSV(File txtFile) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(txtFile), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, "\n")) {
            writer.writeAll(listOfArrOfPairs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
