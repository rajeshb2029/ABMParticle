package testbed.ABMParticleSimulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * @author Rajesh Balagam
 */
public class SimulationMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulationMain.class);

  private final SimulationController controller;

  public SimulationMain() {
    controller = new SimulationController(Globals.GUIWIDTH, Globals.GUIHEIGHT);
    if (Globals.GUI) {
      SimulationGUI gui = new SimulationGUI(controller);
    }
  }

  public static void main(String[] args) {
    if (Globals.GUI) {
      try {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
        LOGGER.error("Could not set the look and feel to Nimbus");
      }
    }

    Parameters.readParameters();
    SimulationMain simulationMain = new SimulationMain();
    simulationMain.controller.start();
  }
}
