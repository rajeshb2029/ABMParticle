package testbed.ABMParticleSimulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Rajesh Balagam
 */
public class SimUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimUtils.class);

  public static double lastCellInformationWriteTime = 0.0;

  public static void writeCellInformation() {
    Cell cell;

    String filename = "./" + Parameters.dataDir + "/CellInformation.txt";
    File file = new File(filename);
    try {
      //if file doesn't exists create it
      if (!file.exists()) {
        file.createNewFile();
//        LOGGER.debug(file.getName());
      }

      //true = append file
      FileWriter fw = new FileWriter(filename, true);
      PrintWriter pw = new PrintWriter(fw);
      pw.print(String.format("%.2f", Globals.currentTime) + " ");
      for (int cellID : Globals.simulation.cellArray.keySet()) {
        cell = Globals.simulation.cellArray.get(cellID);
        pw.print(String.format("(%d,%.2f,%.2f,%.4f) ", cell.cellID, cell.pos.x,
                cell.pos.y, cell.orientation));
      }
      pw.println();

      pw.close();
      fw.close();

    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  } // end method writeCellInformation()

}
