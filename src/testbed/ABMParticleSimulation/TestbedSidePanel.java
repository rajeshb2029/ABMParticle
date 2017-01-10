package testbed.ABMParticleSimulation;

import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Rajesh Balagam
 */
public class TestbedSidePanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestbedSidePanel.class);

  private final Dimension buttonDimension = new Dimension(80, 30);
  private final Dimension chkBoxDimension = new Dimension(140, 30);
  private JButton pauseBtn, stepBtn, resetBtn, resetZoomBtn, fitZoomBtn, quitBtn;
  private JCheckBox cellLayer, cellDirection, slimeLayer, slimeOutline, slimeGrid;
  private JCheckBox searchAABBNeighbor, cellOutline, cellNum, cellAlignment, cellReversals;
  private JCheckBox cellRepulsion, slimeCreation, slimeAlignment, slimeAlignDir;
  private JCheckBox cellGrowth, slimeSearchAABB, sideContactSignal, contactNormal;
  private JCheckBox cellOrientationNoise;
  private JButton screenShotBtn;
  private JPanel buttonGroup;
  private JPanel checkBoxGroup1, checkBoxGroup2, checkBoxGroup3;
  private JSeparator separator;

  private double cellOrientationNoiseFactor = 0;

  public TestbedSidePanel() {
    initUI();
  }

  final void initUI() {

    JPanel sidePanelStack = new JPanel();
//    sidePanelStack.setPreferredSize(new Dimension(150, Globals.GUIHEIGHT));
    sidePanelStack.setLayout(new BoxLayout(sidePanelStack, BoxLayout.Y_AXIS));

    addButtons();
    sidePanelStack.add(buttonGroup);

    separator = new JSeparator(SwingConstants.HORIZONTAL);
    sidePanelStack.add(separator);

    createCheckBoxes();
    addChkBoxActionListeners();

    addCheckBoxGroup1();
    sidePanelStack.add(checkBoxGroup1);

    separator = new JSeparator(SwingConstants.HORIZONTAL);
    sidePanelStack.add(separator);

    addCheckBoxGroup2();
    sidePanelStack.add(checkBoxGroup2);

    separator = new JSeparator(SwingConstants.HORIZONTAL);
    sidePanelStack.add(separator);

    addCheckBoxGroup3();
    sidePanelStack.add(checkBoxGroup3);

    add(sidePanelStack);
  }

  final void addButtons() {
    buttonGroup = new JPanel();
    buttonGroup.setLayout(new GridLayout(7, 1));

    pauseBtn = new JButton("Pause");
    pauseBtn.setPreferredSize(buttonDimension);
    buttonGroup.add(pauseBtn);
    pauseBtn.addActionListener((ActionEvent event) -> {
      Globals.simulation.simPaused = !Globals.simulation.simPaused;
      if (Globals.simulation.simPaused) {
        pauseBtn.setText("Resume");
        resetBtn.setEnabled(true);
        stepBtn.setEnabled(true);
      } else {
        pauseBtn.setText("Pause");
        resetBtn.setEnabled(false);
        stepBtn.setEnabled(false);
      }
    });

    stepBtn = new JButton("Step");
    stepBtn.setPreferredSize(buttonDimension);
    stepBtn.setEnabled(false);
    buttonGroup.add(stepBtn);
    stepBtn.addActionListener((ActionEvent event) -> {
      Globals.simulation.singleStep = true;
    });

    resetBtn = new JButton("Reset");
    resetBtn.setPreferredSize(buttonDimension);
    resetBtn.setEnabled(false);
    buttonGroup.add(resetBtn);
    resetBtn.addActionListener((ActionEvent event) -> {
      Globals.simulation.resetSystem();
      Globals.simulation.clearCanvas();
      Globals.simulation.drawSimulation();
    });

    resetZoomBtn = new JButton("Reset Zoom");
    resetZoomBtn.setPreferredSize(buttonDimension);
    buttonGroup.add(resetZoomBtn);
    resetZoomBtn.addActionListener((ActionEvent event) -> {
//      Globals.dbgDraw.setCamera(new Vec2(Parameters.worldWidth/2, Parameters.worldHeight/2), 1f);
      Globals.dbgDraw.setCamera(new Vec2(Globals.cameraPosX, Globals.cameraPosY), Globals.cameraScale);
    });

    fitZoomBtn = new JButton("Fit Zoom");
    fitZoomBtn.setPreferredSize(buttonDimension);
    buttonGroup.add(fitZoomBtn);
    fitZoomBtn.setEnabled(false);

    screenShotBtn = new JButton("Screenshot");
    screenShotBtn.setPreferredSize(buttonDimension);
    buttonGroup.add(screenShotBtn);
    screenShotBtn.addActionListener((ActionEvent event) -> {
      Globals.takeSnapShotBtn = true;
    });

    quitBtn = new JButton("Quit");
    quitBtn.setPreferredSize(buttonDimension);
    quitBtn.addActionListener((ActionEvent e) -> {
      System.exit(0);
    });
    buttonGroup.add(quitBtn);
  }

  final void createCheckBoxes() {
    cellLayer = new JCheckBox("Cell Shape");
    cellLayer.setPreferredSize(chkBoxDimension);
    cellLayer.setSelected(Parameters.drawCellShapeFlag);

    cellOutline = new JCheckBox("Cell Outline");
    cellOutline.setPreferredSize(chkBoxDimension);
    cellOutline.setSelected(Parameters.drawCellOutlineFlag);

    cellNum = new JCheckBox("Cell Num");
    cellNum.setPreferredSize(chkBoxDimension);
    cellNum.setSelected(Parameters.drawCellNumFlag);

    cellDirection = new JCheckBox("Cell Dir");
    cellDirection.setPreferredSize(chkBoxDimension);
    cellDirection.setSelected(Parameters.drawCellVectorFlag);

    searchAABBNeighbor = new JCheckBox("AABB Neighbor");
    searchAABBNeighbor.setPreferredSize(chkBoxDimension);
    searchAABBNeighbor.setSelected(Parameters.drawSearchAABB);

    cellOrientationNoise = new JCheckBox("Cell Ori. Noise");
    cellOrientationNoise.setPreferredSize(chkBoxDimension);
    cellOrientationNoise.setSelected(Parameters.orientationRandomNoiseFactor > 0);
    cellOrientationNoiseFactor = Parameters.orientationRandomNoiseFactor;

    cellReversals = new JCheckBox("Cell Reversals");
    cellReversals.setPreferredSize(chkBoxDimension);
    cellReversals.setSelected(Parameters.applyCellReversals);

    cellGrowth = new JCheckBox("Cell Growth");
    cellGrowth.setPreferredSize(chkBoxDimension);
    cellGrowth.setSelected(Parameters.applyCellGrowth);

    slimeCreation = new JCheckBox("Create slime");
    slimeCreation.setPreferredSize(chkBoxDimension);
    slimeCreation.setSelected(Parameters.slimeCreationFlag);

    slimeLayer = new JCheckBox("Slime Layer");
    slimeLayer.setPreferredSize(chkBoxDimension);
    slimeLayer.setSelected(Parameters.drawSlimeShapeFlag);

    slimeAlignment = new JCheckBox("Slime Alignment");
    slimeAlignment.setPreferredSize(chkBoxDimension);
    slimeAlignment.setSelected(Parameters.slimeAlignmentFlag);

    slimeAlignDir = new JCheckBox("Slime Align Dir");
    slimeAlignDir.setPreferredSize(chkBoxDimension);
    slimeAlignDir.setSelected(Parameters.drawSlimeAlignmentDirFlag);

    slimeOutline = new JCheckBox("Slime Outline");
    slimeOutline.setPreferredSize(chkBoxDimension);
    slimeOutline.setSelected(Parameters.drawSlimeOutlineFlag);

    slimeGrid = new JCheckBox("Slime Grid");
    slimeGrid.setPreferredSize(chkBoxDimension);
    slimeGrid.setSelected(Parameters.drawSlimeGrid);

    slimeSearchAABB = new JCheckBox("Slime AABB");
    slimeSearchAABB.setPreferredSize(chkBoxDimension);
    slimeSearchAABB.setSelected(Parameters.drawSlimeAABBFlag);

    cellAlignment = new JCheckBox("Cell Alignment");
    cellAlignment.setPreferredSize(chkBoxDimension);
    cellAlignment.setSelected(Parameters.neighborCellAlignmentFlag);

    cellRepulsion = new JCheckBox("Cell Repulsion");
    cellRepulsion.setPreferredSize(chkBoxDimension);
    cellRepulsion.setSelected(Parameters.applyCellCellRepulsion);

    contactNormal = new JCheckBox("Contact Normal");
    contactNormal.setPreferredSize(chkBoxDimension);
    contactNormal.setSelected(Parameters.drawRepulsionDir);

    sideContactSignal = new JCheckBox("Side Contact Signal");
    sideContactSignal.setPreferredSize(chkBoxDimension);
    sideContactSignal.setSelected(Parameters.applySideContactSignaling);

    adjustOtherComponentStatus();
  }

  final void addChkBoxActionListeners() {
    cellLayer.addActionListener((ActionEvent e) -> {
      Parameters.drawCellShapeFlag = cellLayer.isSelected();
      adjustOtherComponentStatus();
    });

    cellOutline.addActionListener((ActionEvent event) -> {
      Parameters.drawCellOutlineFlag = cellOutline.isSelected();
      adjustOtherComponentStatus();
    });

    cellNum.addActionListener((ActionEvent event) -> {
      Parameters.drawCellNumFlag = cellNum.isSelected();
      adjustOtherComponentStatus();
    });

    cellDirection.addActionListener((ActionEvent event) -> {
      Parameters.drawCellVectorFlag = cellDirection.isSelected();
      adjustOtherComponentStatus();
    });

    searchAABBNeighbor.addActionListener((ActionEvent event) -> {
      Parameters.drawSearchAABB = searchAABBNeighbor.isSelected();
      adjustOtherComponentStatus();
    });

    cellOrientationNoise.addActionListener((ActionEvent event) -> {
      if (cellOrientationNoise.isSelected()) {
        Parameters.orientationRandomNoiseFactor = cellOrientationNoiseFactor;
      } else {
        Parameters.orientationRandomNoiseFactor = 0;
      }
      adjustOtherComponentStatus();
    });

    cellReversals.addActionListener((ActionEvent event) -> {
      Parameters.applyCellReversals = cellReversals.isSelected();
      adjustOtherComponentStatus();
    });

    cellGrowth.addActionListener((ActionEvent event) -> {
      Parameters.applyCellGrowth = cellGrowth.isSelected();
      adjustOtherComponentStatus();
    });

    slimeCreation.addActionListener((ActionEvent event) -> {
      Parameters.slimeCreationFlag = slimeCreation.isSelected();
      adjustOtherComponentStatus();
    });

    slimeLayer.addActionListener((ActionEvent event) -> {
      Parameters.drawSlimeShapeFlag = slimeLayer.isSelected();
      adjustOtherComponentStatus();
    });

    slimeAlignment.addActionListener((ActionEvent event) -> {
      Parameters.slimeAlignmentFlag = slimeAlignment.isSelected();
      adjustOtherComponentStatus();
    });

    slimeAlignDir.addActionListener((ActionEvent event) -> {
      Parameters.drawSlimeAlignmentDirFlag = slimeAlignDir.isSelected();
      adjustOtherComponentStatus();
    });

    slimeOutline.addActionListener((ActionEvent event) -> {
      Parameters.drawSlimeOutlineFlag = slimeOutline.isSelected();
      adjustOtherComponentStatus();
    });

    slimeGrid.addActionListener((ActionEvent event) -> {
      Parameters.drawSlimeGrid = slimeGrid.isSelected();
      adjustOtherComponentStatus();
    });

    slimeSearchAABB.addActionListener((ActionEvent event) -> {
      Parameters.drawSlimeAABBFlag = slimeSearchAABB.isSelected();
      adjustOtherComponentStatus();
    });

    cellAlignment.addActionListener((ActionEvent event) -> {
      Parameters.neighborCellAlignmentFlag = cellAlignment.isSelected();
      adjustOtherComponentStatus();
    });

    cellRepulsion.addActionListener((ActionEvent event) -> {
      Parameters.applyCellCellRepulsion = cellRepulsion.isSelected();
      adjustOtherComponentStatus();
    });

    contactNormal.addActionListener((ActionEvent event) -> {
      Parameters.drawRepulsionDir = contactNormal.isSelected();
      adjustOtherComponentStatus();
    });

    sideContactSignal.addActionListener((ActionEvent event) -> {
      Parameters.applySideContactSignaling = sideContactSignal.isSelected();
      adjustOtherComponentStatus();
    });
  }

  final void addCheckBoxGroup1() {
    checkBoxGroup1 = new JPanel();
    checkBoxGroup1.setLayout(new GridLayout(5, 1));

    checkBoxGroup1.add(cellLayer);
    checkBoxGroup1.add(cellOutline);
    checkBoxGroup1.add(cellNum);
    checkBoxGroup1.add(cellDirection);
    checkBoxGroup1.add(searchAABBNeighbor);
  }

  final void addCheckBoxGroup2() {
    checkBoxGroup2 = new JPanel();
    checkBoxGroup2.setLayout(new GridLayout(7, 1));

    checkBoxGroup2.add(cellOrientationNoise);
    checkBoxGroup2.add(cellReversals);
    checkBoxGroup2.add(cellGrowth);
    checkBoxGroup2.add(cellAlignment);
    checkBoxGroup2.add(cellRepulsion);
    checkBoxGroup2.add(contactNormal);
    checkBoxGroup2.add(sideContactSignal);
  }

  final void addCheckBoxGroup3() {
    checkBoxGroup3 = new JPanel();
    checkBoxGroup3.setLayout(new GridLayout(7, 1));

    checkBoxGroup3.add(slimeCreation);
    checkBoxGroup3.add(slimeGrid);
    checkBoxGroup3.add(slimeLayer);
    checkBoxGroup3.add(slimeOutline);
    checkBoxGroup3.add(slimeAlignment);
    checkBoxGroup3.add(slimeAlignDir);
    checkBoxGroup3.add(slimeSearchAABB);
  }

  final void adjustOtherComponentStatus() {
    sideContactSignal.setEnabled(Parameters.applyCellReversals);

    slimeAlignment.setEnabled(Parameters.slimeCreationFlag);
    slimeAlignDir.setEnabled(Parameters.slimeCreationFlag && Parameters.slimeAlignmentFlag);
    slimeSearchAABB.setEnabled(Parameters.slimeCreationFlag && Parameters.slimeAlignmentFlag);
    slimeLayer.setEnabled(Parameters.slimeCreationFlag);
    slimeOutline.setEnabled(Parameters.slimeCreationFlag);
    slimeGrid.setEnabled(Parameters.slimeCreationFlag);

    contactNormal.setEnabled(Parameters.applyCellCellRepulsion);
  }
}
