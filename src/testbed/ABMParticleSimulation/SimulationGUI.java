package testbed.ABMParticleSimulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * @author Rajesh Balagam
 */
public class SimulationGUI extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulationGUI.class);

  private final int width = Globals.GUIWIDTH;
  private final int height = Globals.GUIHEIGHT;

  private SimulationController controller;
  private GraphicsPanel panel;
  private TestbedSidePanel sidePanel;

  public SimulationGUI(SimulationController controller) {
    this.controller = controller;
    if (Globals.GUI) {
      initUI();
    }
  }

  final void initUI() {
    panel = controller.panel;
    sidePanel = controller.sidePanel;
    controller.setFrameInsets(this.getInsets());

    setSize(width + 150, height);
    setLayout(new BorderLayout());
    add(panel, "Center");
    add(new JScrollPane(sidePanel), "East");
    pack();

    setTitle(this.getClass().getName());
    setResizable(false);
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
}
