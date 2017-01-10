package testbed.ABMParticleSimulation;

import net.sf.javaml.core.kdtree.KDTree;
import org.apache.commons.math3.util.FastMath;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testbed.utils.MyVec2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rajesh Balagam
 */
public class Simulation {

  private static final Logger LOGGER = LoggerFactory.getLogger(Simulation.class);

  public ConcurrentHashMap<Integer, Cell> cellArray;
  public KDTree kdTree;
  public ArrayList<Cell> newCellsList;
  public SlimeGrid slimeGrid;
  public ConcurrentHashMap<Long, Slime> slimeTrailArray;

  public boolean simPaused;
  public boolean simStopped;
  public boolean singleStep;
  public Thread simIterationThread;
  public boolean drawingFinished;

  private int N;
  private final int width;
  private final int height;
  private Random r;

  private int imageCount = 0;
  public double lastSnapShotTime;

  public Simulation(int width, int height) {
    this.width = width;
    this.height = height;

    slimeGrid = new SlimeGrid(Parameters.slimeGridWidth);
    slimeTrailArray = new ConcurrentHashMap<>();

    singleStep = false;
    drawingFinished = false;
    lastSnapShotTime = 0f;

    initializeSimulation(Parameters.numParticles);
  }

  final void initializeSimulation(int size) {
    Globals.currentTime = 0.0f;
    N = size;
    r = new Random();
    r.setSeed(System.currentTimeMillis());
    Parameters.r = r;
    cellArray = new ConcurrentHashMap<>();
    newCellsList = new ArrayList<>();
    kdTree = new KDTree(2);

    initializeCells();
    insertCellsToKDTree();
    slimeGrid.clearSlimeField();
    slimeTrailArray.clear();

    if (Parameters.writeCellInformation && Globals.simulation != null) {
      SimUtils.writeCellInformation();
    }
  }

  public void resetSystem() {
    initializeSimulation(Parameters.numParticles);
  }

  void initializeCells() {
    switch (Parameters.selectedInitializationMethod) {
      case InitializeAtRandom:
        initializeCellsRandom();
        break;
      case InitializeAtCenter:
        initializeCellsAtCenter("circle2");
        break;
      case InitializeAtBorder:
        initializeCellsAtBorder();
        break;
    }

//    clearCanvas();
//    drawCells();
//    drawSimInfo();
  }

  void insertCellsToKDTree() {
    double[] key;
    kdTree = new KDTree(2);
    Cell cell;
    for (int ID : cellArray.keySet()) {
      cell = cellArray.get(ID);
      key = new double[2];
      key[0] = cell.pos.x;
      key[1] = cell.pos.y;
      kdTree.insert(key, cell);
    }
  }

  void initializeCellsRandom() {
    cellArray.clear();
    for (int i = 0; i < N; i++) {
      cellArray.put(i, new Cell(i, r.nextDouble() * width, r.nextDouble() * height,
              Cell.getInitialCellDirection()));
    }
  }

  void initializeCellsAtCenter(String config) {
    double theta, dist;
    Vec2 pos;

    cellArray.clear();
    double initializeRadius = 100;

    switch (config) {
      case "square":
        for (int i = 0; i < N; i++) {
          cellArray.put(i, new Cell(i, width / 2 + (r.nextDouble() - 0.5) * initializeRadius,
                  height / 2.f + (r.nextDouble() - 0.5) * initializeRadius,
                  Cell.getInitialCellDirection()));
        }
        break;

      case "circle":
        for (int i = 0; i < N; i++) {
          theta = r.nextDouble() * 2 * Math.PI;
          dist = r.nextDouble() * initializeRadius;
          pos = new Vec2((float) (width / 2 + FastMath.cos(theta) * dist),
                  (float) (height / 2 + FastMath.sin(theta) * dist));
//          LOGGER.debug("pos: " + pos);
          cellArray.put(i, new Cell(i, pos.x, pos.y, Cell.getInitialCellDirection()));
        }
        break;

      case "circle2":
        Vec2 center = new Vec2(width / 2.f, height / 2.f);
        pos = new Vec2(0, 0);
        for (int i = 0; i < N; i++) {
          boolean insideCircle = false;
          while (!insideCircle) {
            pos = new Vec2((float) (width / 2 + (r.nextDouble() - 0.5) * 2 * initializeRadius),
                    (float) (height / 2 + (r.nextDouble() - 0.5) * 2 * initializeRadius));
            if (MyVec2.getEuclidDistance(pos, center) <= initializeRadius) {
              insideCircle = true;
            }
          }
//          LOGGER.debug("pos: " + pos);
          cellArray.put(i, new Cell(i, pos.x, pos.y, Cell.getInitialCellDirection()));
        }
        break;
    }
  }

  void initializeCellsAtBorder() {
    Vec2 pos = new Vec2(10, Parameters.worldHeight / 4);
    for (int i = 0; i < Parameters.numParticles; i++) {
      cellArray.put(i, new Cell(i, pos.x, pos.y, Cell.getInitialCellDirection()));
      pos.addLocal(0, 25);
    }
  }

  //  void clearBufferedCanvas() {
//    bufferedCanvas = new Canvas(width, height);
//    if (bufferedCanvas == null) {
//      LOGGER.error("dbImage is still null, ignoring render call");
//    }
//    gc = bufferedCanvas.getGraphicsContext2D();
//    gc.setFill(Color.BLACK);
//    gc.fillRect(0, 0, width, height);
//  }
//  void copyToBufferedCanvas() {
//    canvas.snapshot(null, image);
////    bufferedCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
//    drawingFinished = false;
//  }
  public void writeSnapshot() {
    boolean success;
    try {
//      String current = new java.io.File(".").getCanonicalPath();
      String fileName = Parameters.imageDir + "/image" + imageCount + ".png";
      File file = new File(fileName);
      if (Globals.scaleImages) {
        BufferedImage sImage = resizeImage(Globals.simImage, Globals.simImage.getType(),
                Globals.imageWidth, Globals.imageHeight);
        success = ImageIO.write(sImage, "png", file);
      } else {
        success = ImageIO.write(Globals.simImage, "png", file);
      }
      if (!success) {
        LOGGER.debug("Image writing failed");
      }
      imageCount++;
    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }

  private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
    BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
    g.dispose();

    return resizedImage;
  }

  final void updateCells() {
    newCellsList.clear();
    for (int ID : cellArray.keySet()) {
      cellArray.get(ID).updateCell(Parameters.timeStep);
    }

    for (Cell cellNew : newCellsList) {
      cellArray.put(cellNew.cellID, cellNew);
    }
  }

  void clearCanvas() {
//    Globals.dbgDraw.drawFilledRectangle(new Vec2(width/2, height/2), width, height, Color3f.BLUE);
//    Globals.dbgDraw.drawFilledRectangle(new Vec2(width / 2, height / 2), width, height, Color3f.BLACK);
    Globals.dbgDraw.getGraphics().setColor(Color.black);
    Globals.dbgDraw.getGraphics().fillRect(0, 0, Globals.GUIWIDTH, Globals.GUIHEIGHT);
  }

  final void drawCells() {
    Vec2 cellPos = new Vec2();
    for (int ID : cellArray.keySet()) {
//      LOGGER.debug(x + ", " + y);
      cellPos.set(cellArray.get(ID).pos);
      LOGGER.debug(cellPos.x + ", " + cellPos.y);
      Globals.dbgDraw.drawSolidCircle(cellPos, (float) Parameters.cellWidth / 2, null, Color3f.WHITE);
      if (Parameters.drawCellCenterFlag) {
        Globals.dbgDraw.drawPoint(cellPos, 2, Color3f.RED);
      }
//      Globals.dbgDraw.drawRectangle(cellPos, 2*Parameters.cellWidth, 2*Parameters.cellWidth, Color3f.BLUE);
      if (Parameters.drawCellNumFlag) {
        Globals.dbgDraw.drawString(cellPos.x - 8, cellPos.y,
                Integer.toString(ID), DebugDrawJ2D.getColor3f(Color.RED));
      }
    }
  }

//  final void drawSimInfo() {
//    Globals.dbgDraw.drawString(5, 1*Globals.stringSepY, "Framerate: "
//            + String.format("%.2f", Globals.frameRate),
//            DebugDrawJ2D.getColor3f(Color.RED));    
//    Globals.dbgDraw.drawString(5, 2*Globals.stringSepY, "Time: "
//            + String.format("%.2f", Globals.currentTime),
//            DebugDrawJ2D.getColor3f(Color.RED));    
//    Globals.dbgDraw.drawString(5, 3*Globals.stringSepY, "Total cells: " + 
//            cellArray.size(), DebugDrawJ2D.getColor3f(Color.RED));
//  }

  final void drawSimInfo() {
    Globals.dbgDraw.getGraphics().setColor(Color.RED);
    Globals.dbgDraw.getGraphics().drawString("Framerate: " + String.format("%.2f",
            Globals.frameRate), 5, 1 * Globals.stringSepY);
    Globals.dbgDraw.getGraphics().drawString("Time: " + String.format("%.2f",
            Globals.currentTime), 5, 2 * Globals.stringSepY);
    Globals.dbgDraw.getGraphics().drawString("Total cells: " + cellArray.size(),
            5, 3 * Globals.stringSepY);
  }

  final void drawCellVectors() {
    for (int ID : cellArray.keySet()) {
      Globals.dbgDraw.drawVector(cellArray.get(ID).pos,
              MyVec2.unitVector((float) cellArray.get(ID).orientation),
              (float) (1.0 * Parameters.cellLength), Color.YELLOW);
    }
  }

  final void drawCellOutlines() {
    Cell cell;
    for (int ID : cellArray.keySet()) {
      Vec2 target = MyVec2.unitVector((float) cellArray.get(ID).orientation);
      target.mulLocal((float) (Parameters.cellLength / 2));
      cell = cellArray.get(ID);
//      LOGGER.debug(cell.pos.x + ", " + cell.pos.y);
      Vec2 tail = cell.pos.add(target.negate());
      Vec2 head = cell.pos.add(target);
      Color color = Color.YELLOW;
      if (Parameters.applyCellReversals
              && Parameters.drawRefractoryCells
              && cell.reversalClock <= Parameters.refractoryPeriod) {
        color = Color.MAGENTA;
      }
      Globals.dbgDraw.drawSegment(tail, head, DebugDrawJ2D.getColor3f(color));

      if (Parameters.drawCellNumFlag) {
        Globals.dbgDraw.drawString(cell.pos.x - 8, cell.pos.y,
                Integer.toString(ID), DebugDrawJ2D.getColor3f(Color.RED));
      }
    }
  }

  void drawBorders() {
    Globals.dbgDraw.drawSegment(new Vec2(0, 0), new Vec2(width, 0), Color3f.RED);
    Globals.dbgDraw.drawSegment(new Vec2(0, 0), new Vec2(0, height), Color3f.RED);
    Globals.dbgDraw.drawSegment(new Vec2(width, 0), new Vec2(width, height), Color3f.RED);
    Globals.dbgDraw.drawSegment(new Vec2(0, height), new Vec2(width, height), Color3f.RED);
  }

  public void step() {
    Globals.currentTime += Parameters.timeStep;
    updateCells();
    insertCellsToKDTree();

    writeSimulationInfo();

    if (Parameters.slimeCreationFlag || slimeTrailArray.size() > 0) {
      slimeGrid.applySlimeAging();
    }
  }

  final void writeSimulationInfo() {
    if (Parameters.writeCellInformation) {
      if ((Globals.currentTime - SimUtils.lastCellInformationWriteTime) > Parameters.calculationTime) {
        SimUtils.writeCellInformation();
        SimUtils.lastCellInformationWriteTime = Globals.currentTime;
      }
    }
  }

  void drawSimulation() {
//    clearCanvas();

    drawBorders();

    if (Parameters.drawSlimeGrid) {
      slimeGrid.drawSlimeGrid();
    }
    if (Parameters.drawSlimeShapeFlag) {
      slimeGrid.drawSlime();
    }

    if (Parameters.drawSlimeDirectionFlag) {
      slimeGrid.drawSlimeDirection();
    }

    if (Parameters.drawSlimeOutlineFlag) {
      slimeGrid.drawSlimeOutline();
    }

    if (Parameters.drawCellShapeFlag) {
      drawCells();
    }

    if (Parameters.drawCellOutlineFlag) {
      drawCellOutlines();
    }

    if (Parameters.drawCellVectorFlag) {
      drawCellVectors();
    }

    drawSimInfo();

//    drawingFinished = true;
//    canvas.snapshot(null, image);    
//    if (Parameters.saveImages && (Parameters.currentTime - lastSnapShotTime)
//            >= Parameters.snapShotInterval) {
//      writeSnapshot();
//      lastSnapShotTime = Parameters.currentTime;
//    }
  }

}
