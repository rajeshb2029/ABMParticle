package testbed.ABMParticleSimulation;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.jbox2d.common.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * @author Rajesh Balagam
 */
public class Parameters {

  private static final Logger LOGGER = LoggerFactory.getLogger(Parameters.class);

  public enum CellMovementModels {
    CellFixedVelocity,
    CellRandomWalk,
    CellFixedVelocityWithNoise,
    CellRandomWalkWithNeighborReinforcement,
    CellRandomWalkWithGrowth
  }


  public enum InitializationMethods {
    InitializeAtRandom,
    InitializeAtCenter,
    InitializeAtBorder
  }


  public enum InitialOrientationModel {
    RandomOrientation,
    UniformOrientation,
    ParitialUniformOrienation
  }


  public enum DaughterCellDirectionModel {
    RandomDirection,
    UniformDirection,
    ParentCellDirection,
    ParentCellNegDirection,
    ParentCellDirectionWithProb
  }

  public enum CellRepulsionModel {
    SoftSphere,
    HardSphere,
    NeighborCOM,
    HardSphereMultiCollision
  }

  public enum CellAlignmentModel {
    NeighborAlignment,
    VicsekModel, // Vicsek et al (1995) PRL
    NematicAlignment // Chate H (2006) PRL
  }

  public static int worldWidth = 1000;
  public static int worldHeight = 1000;
  public static Random r;

  public static int numParticles = 1000;
  public static double cellWidth = 4;
  public static double cellLength = 10;

  public static double timeStep = 0.1;
  public static double finalTime = 2000;
//  public static long delay = 10;  
//  public static int threadDelay = 10;

  public static double cellSpeed = 4;
  public static double posRandomNoiseFactor = 0.0;
  public static boolean applyOrientationNoise = false;
  public static double orientationRandomNoiseFactor = 0.01;

  public static boolean applyCellReversals = false;
  public static boolean asynchronousCellReversals = false;
  public static double meanCellReversalTime = 8.0;

  public static boolean applySideContactSignaling = false;
  public static double refractoryPeriod = 3.0;
  public static double alignOriThreshold = MathUtils.PI / 12;
  public static double sideSignalProb = 0.1;
  public static boolean drawRefractoryCells = false;

  public static boolean neighborCellAlignmentFlag = false;
  public static double neighborAlignmentFactor = 0.1;
  public static double neighborSearchRadius = 2.0;
  public static boolean drawSearchAABB = false;

  public static boolean drawCellShapeFlag = false;
  public static boolean drawCellVectorFlag = false;
  public static boolean drawCellOutlineFlag = true;
  public static boolean drawCellNumFlag = false;
  public static boolean drawCellCenterFlag = false;

  public static boolean slimeCreationFlag = false;
  public static double slimeProduceRate = 20;
  public static double slimeDegradeRateConstant = 1.0;
  public static double slimeGridWidth = 5;

  public static boolean slimeAlignmentFlag = false;
  public static double slimeSearchRadius = 10;
  public static int slimeSearchNumBins = 5;
  public static double slimeAttractionTime = 1;
  public static double thresholdSlimeVolume = 0.1;
  public static double slimeAlignmentFactor = 0.05;

  public static boolean drawSlimeShapeFlag = false;
  public static boolean drawSlimeDirectionFlag = false;
  public static boolean drawSlimeOutlineFlag = false;
  public static boolean drawSlimeGrid = false;
  public static boolean drawSlimeGridNum = true;
  public static boolean drawSlimeAABBFlag = false;
  public static boolean drawSlimeAlignmentDirFlag = false;

  public static boolean applyCellGrowth = false;
  public static double cellGrowthRate = 0.01;

  public static boolean applyCellCellRepulsion = false;
  public static double repulsionFactor = 1.0;
  public static boolean drawRepulsionDir = false;

  public static double initialCellOrientation = 0; //(float) (Math.PI/4*1.1);  
  public static double initialRandomOrientationCellFraction = 0.1;

  public static String dataDir = "runData";
  public static String imageDir = "images";
  public static boolean saveImages = false;
  public static double snapShotInterval = 0.99;

  public static CellMovementModels selectedMovementModel = CellMovementModels.CellFixedVelocityWithNoise;
  public static InitializationMethods selectedInitializationMethod = InitializationMethods.InitializeAtRandom;
  public static InitialOrientationModel selectedInitialOrientationModel = InitialOrientationModel.RandomOrientation;

  public static DaughterCellDirectionModel selectedDaughterCellDirModel = DaughterCellDirectionModel.ParentCellDirection;
  public static double daughterCellUniformDir = 0;
  public static double daughterCellParentDirProb = 1;

  public static CellRepulsionModel selectedCellRepulsionModel = CellRepulsionModel.HardSphereMultiCollision;
  public static CellAlignmentModel selectedCellAlignmentModel = CellAlignmentModel.NeighborAlignment;

  public static boolean writeCellInformation = false;
  public static double calculationTime = 1.0;

  public static boolean applyPeriodicBoundary = true;

  public static void readParameters() {
    Configurations configs = new Configurations();

    try {
      String filename = "parameters.txt";
      LOGGER.info("Working Directory = " + System.getProperty("user.dir"));
      LOGGER.info("Reading parameter file: " + filename);
      Configuration config = configs.properties(new File(filename));

      worldWidth = config.getInt("worldWidth");
      worldHeight = config.getInt("worldHeight");
      numParticles = config.getInt("numParticles");
      cellWidth = config.getDouble("cellWidth");
      cellLength = config.getDouble("cellLength");

      timeStep = config.getDouble("timeStep");
      finalTime = config.getDouble("finalTime");

      cellSpeed = config.getDouble("cellSpeed");
      applyOrientationNoise = config.getBoolean("applyOrientationNoise");
      orientationRandomNoiseFactor = config.getDouble("orientationRandomNoiseFactor");

      applyCellReversals = config.getBoolean("applyCellReversals");
      asynchronousCellReversals = config.getBoolean("asynchronousCellReversals");
      meanCellReversalTime = config.getDouble("meanCellReversalTime");

      applySideContactSignaling = config.getBoolean("applySideContactSignaling");
      refractoryPeriod = config.getDouble("refractoryPeriod");
      alignOriThreshold = config.getDouble("alignOriThreshold");
      sideSignalProb = config.getDouble("sideSignalProb");

      neighborCellAlignmentFlag = config.getBoolean("neighborCellAlignmentFlag");
      neighborAlignmentFactor = config.getDouble("neighborAlignmentFactor");
      drawSearchAABB = config.getBoolean("drawSearchAABB");

      drawCellShapeFlag = config.getBoolean("drawCellShapeFlag");
      drawCellVectorFlag = config.getBoolean("drawCellVectorFlag");
      drawCellOutlineFlag = config.getBoolean("drawCellOutlineFlag");
      drawCellNumFlag = config.getBoolean("drawCellNumFlag");
      drawCellCenterFlag = config.getBoolean("drawCellCenterFlag");

      slimeCreationFlag = config.getBoolean("slimeCreationFlag");
      slimeProduceRate = config.getDouble("slimeProduceRate");
      slimeDegradeRateConstant = config.getDouble("slimeDegradeRateConstant");
      slimeGridWidth = config.getDouble("slimeGridWidth");

      slimeAlignmentFlag = config.getBoolean("slimeAlignmentFlag");
      slimeSearchRadius = config.getDouble("slimeSearchRadius");
      slimeSearchNumBins = config.getInt("slimeSearchNumBins");
      slimeAttractionTime = config.getDouble("slimeAttractionTime");
      thresholdSlimeVolume = config.getDouble("thresholdSlimeVolume");
      slimeAlignmentFactor = config.getDouble("slimeAlignmentFactor");

      drawSlimeShapeFlag = config.getBoolean("drawSlimeShapeFlag");
      drawSlimeDirectionFlag = config.getBoolean("drawSlimeDirectionFlag");
      drawSlimeOutlineFlag = config.getBoolean("drawSlimeOutlineFlag");
      drawSlimeGrid = config.getBoolean("drawSlimeGrid");
      drawSlimeGridNum = config.getBoolean("drawSlimeGridNum");
      drawSlimeAABBFlag = config.getBoolean("drawSlimeAABBFlag");
      drawSlimeAlignmentDirFlag = config.getBoolean("drawSlimeAlignmentDirFlag");

      applyCellGrowth = config.getBoolean("applyCellGrowth");
      cellGrowthRate = config.getDouble("cellGrowthRate");

      applyCellCellRepulsion = config.getBoolean("applyCellCellRepulsion");
      repulsionFactor = config.getDouble("repulsionFactor");
      drawRepulsionDir = config.getBoolean("drawRepulsionDir");

      initialCellOrientation = config.getDouble("initialCellOrientation");
      initialRandomOrientationCellFraction = config.getDouble("initialRandomOrientationCellFraction");

      dataDir = config.getString("dataDir");
      imageDir = config.getString("imageDir");
      saveImages = config.getBoolean("saveImages");
      snapShotInterval = config.getDouble("snapShotInterval");

      writeCellInformation = config.getBoolean("writeCellInformation");
      calculationTime = config.getDouble("calculationTime");

      String initializationMethod = config.getString("initializationMethod");
      switch (initializationMethod) {
        case "InitializeAtRandom":
          selectedInitializationMethod = InitializationMethods.InitializeAtRandom;
          break;
        case "InitializeAtCenter":
          selectedInitializationMethod = InitializationMethods.InitializeAtCenter;
          break;
        case "InitializeAtBorder":
          selectedInitializationMethod = InitializationMethods.InitializeAtBorder;
          break;
        default:
          LOGGER.error("Specified InitializationMethod not found");
          break;
      }

      String initialOrientation = config.getString("initialOrientation");
      switch (initialOrientation) {
        case "RandomOrientation":
          selectedInitialOrientationModel = InitialOrientationModel.RandomOrientation;
          break;
        case "UniformOrientation":
          selectedInitialOrientationModel = InitialOrientationModel.UniformOrientation;
          break;
        case "ParitialUniformOrienation":
          selectedInitialOrientationModel = InitialOrientationModel.ParitialUniformOrienation;
          break;
        default:
          LOGGER.error("Specified InitialOrientationModel not found");
          System.exit(1);
          break;
      }

      String cellMovementModel = config.getString("cellMovementModel");
      switch (cellMovementModel) {
        case "CellFixedVelocity":
          selectedMovementModel = CellMovementModels.CellFixedVelocity;
          break;
        case "CellRandomWalk":
          selectedMovementModel = CellMovementModels.CellRandomWalk;
          break;
        case "CellFixedVelocityWithNoise":
          selectedMovementModel = CellMovementModels.CellFixedVelocityWithNoise;
          break;
        case "CellRandomWalkWithReinforcement":
          selectedMovementModel = CellMovementModels.CellRandomWalkWithNeighborReinforcement;
          break;
        case "CellRandomWalkWithGrowth":
          selectedMovementModel = CellMovementModels.CellRandomWalkWithGrowth;
          break;
        default:
          LOGGER.error("Specified CellMovementModel not found");
          System.exit(1);
          break;
      }

      String daughterCellDir = config.getString("daughterCellDirectionModel");
      switch (daughterCellDir) {
        case "RandomDirection":
          selectedDaughterCellDirModel = DaughterCellDirectionModel.RandomDirection;
          break;
        case "UniformDirection":
          selectedDaughterCellDirModel = DaughterCellDirectionModel.UniformDirection;
          daughterCellUniformDir = config.getDouble("daughterCellUniformDir");
          break;
        case "ParentCellDirection":
          selectedDaughterCellDirModel = DaughterCellDirectionModel.ParentCellDirection;
          break;
        case "ParentCellNegDirection":
          selectedDaughterCellDirModel = DaughterCellDirectionModel.ParentCellNegDirection;
          break;
        case "ParentCellDirectionWithProb":
          selectedDaughterCellDirModel = DaughterCellDirectionModel.ParentCellDirectionWithProb;
          daughterCellParentDirProb = config.getDouble("daughterCellParentDirProb");
          break;
        default:
          LOGGER.error("Specified DaughterCellDirection not found");
          System.exit(1);
          break;
      }

      String cellAlignmentModel = config.getString("cellAlignmentModel");
      switch (cellAlignmentModel) {
        case "VicsekModel":
          selectedCellAlignmentModel = CellAlignmentModel.VicsekModel;
          break;
        case "NeighborAlignment":
          selectedCellAlignmentModel = CellAlignmentModel.NeighborAlignment;
          break;
        case "NematicAlignment":
          selectedCellAlignmentModel = CellAlignmentModel.NematicAlignment;
          break;
        default:
          LOGGER.error("Specified CellAlignmentModel not found");
          System.exit(1);
          break;
      }

      // Read Globals
      Globals.GUI = config.getBoolean("GUI");
      Globals.cameraPosX = config.getFloat("cameraPosX");
      Globals.cameraPosY = config.getFloat("cameraPosY");
      Globals.cameraScale = config.getDouble("cameraScale");
      Globals.imageWidth = config.getInt("imageWidth");
      Globals.imageHeight = config.getInt("imageHeight");
      Globals.scaleImages = config.getBoolean("scaleImages");

    } catch (ConfigurationException ex) {
      ex.printStackTrace(System.err);
      System.exit(1);
    }

    createResultDirectories();
    writeParametersToFile();
  }

  static final void createResultDirectories() {
    File f = new File(Parameters.dataDir);
    if (!f.exists()) {
      LOGGER.info("Data directory doesn't exists");
      if (f.mkdir()) {
        LOGGER.info("New Data directory created");
      }
    } else {
      LOGGER.info("Previous data directory exists.");
      try {
        FileUtils.deleteDirectory(f);
      } catch (IOException ex) {
        LOGGER.error("Data directory delete unsuccessful", ex);
        ex.printStackTrace(System.err);
        System.exit(1);
      }
      LOGGER.info("Previous data directory exists. Delted.");
      if (f.mkdir()) {
        LOGGER.info("New Data directory created");
      }
    }

    f = new File(Parameters.imageDir);
    if (!f.exists()) {
      LOGGER.info("Image directory doesn't exists");
      if (f.mkdir()) {
        LOGGER.info("New Image directory created");
      }
    } else {
      LOGGER.info("Previous image directory exists.");
      try {
        FileUtils.deleteDirectory(f);
      } catch (IOException ex) {
        LOGGER.error("Image directory delete unsuccessful", ex);
        ex.printStackTrace(System.err);
        System.exit(1);
      }
      LOGGER.info("Previous image directory exists. Deleted.");
      if (f.mkdir()) {
        LOGGER.info("New Image directory created");
      }
    }
  }

  static final void writeParametersToFile() {
    String filename = "runParameters.txt";
    filename = Parameters.dataDir + "/" + filename;
    try {
      FileWriter fw = new FileWriter(filename);
      PrintWriter pw = new PrintWriter(fw);
      for (Field field : Parameters.class.getFields()) {
        pw.print(field.getName()); //get each variable name
        pw.print("=");
        pw.print(field.get(null)); //get variable value
        pw.println();
      }
      pw.close();
      fw.close();
      LOGGER.info("Simulation parameters written to ./" + filename);
    } catch (IOException | IllegalAccessException exception) {
      exception.printStackTrace(System.err);
      System.exit(1);
    }
  }

}
