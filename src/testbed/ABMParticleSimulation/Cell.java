package testbed.ABMParticleSimulation;


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testbed.utils.MyRectangle;
import testbed.utils.MyVec2;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rajesh Balagam
 */
public class Cell {

  private static final Logger LOGGER = LoggerFactory.getLogger(Cell.class);

  public int cellID;
  public Vec2 pos;
  public double orientation;

  public double reversalClock;
  boolean foundSlimeBins;
  double slimeAlignmentTimeCounter = 0;
  double slimeAlignmentAngle = 0f;
  boolean slimeAlignmentFlag;
  double slimeForceFraction = 1f;
  Vec2 slimeAlignmentDir;
  ArrayList<Integer> neighborCells;
  double neighborAvgDir;
  Vec2 neighborsCOM;
  Vec2 repulsionDir;
  Vec2 minNeighborDistVector;
  double AmotilityDir;
  boolean signalSensitive;
  double oldOrientation;

  public Cell(int ID, double x, double y, double orientation) {
    this.cellID = ID;
    this.pos = new Vec2();
    this.pos.x = (float) x;
    this.pos.y = (float) y;
    this.orientation = (float) orientation;
    this.oldOrientation = (float) orientation;

    foundSlimeBins = false;
    slimeAlignmentFlag = false;
    AmotilityDir = 0f;
    slimeAlignmentDir = new Vec2();
    neighborCells = new ArrayList<>();
    neighborAvgDir = 0.0;
    neighborsCOM = new Vec2();
    repulsionDir = new Vec2();
    minNeighborDistVector = new Vec2();
    signalSensitive = false;

    initializeReversalClock();
  }

  final void initializeReversalClock() {
    if (Parameters.asynchronousCellReversals) {
      reversalClock = Parameters.r.nextDouble() * Parameters.meanCellReversalTime;
    } else {
      reversalClock = 0;
    }
  }

  void updateReversalClock() {
    if (reversalClock >= Parameters.meanCellReversalTime) {
      resetReversalClock();
    }
    reversalClock += Parameters.timeStep;
  }

  void resetReversalClock() {
    reversalClock = 0;
    orientation += Math.PI;
    orientation = orientation % (2 * Math.PI);
  }

  public void updateCell(double dt) {

    if (Parameters.applyCellReversals) {
      updateReversalClock();
    }

    if (Parameters.neighborCellAlignmentFlag
        || Parameters.applyCellCellRepulsion
        || (Parameters.applySideContactSignaling && Parameters.applyCellReversals)) {
      storeCellNeighbors();
    }

    if (Parameters.applyCellReversals && Parameters.applySideContactSignaling) {
      applySideContactSignaling();
    }

    if (Parameters.slimeCreationFlag) {
      createSlime();
    }

    if (Parameters.slimeAlignmentFlag) {
      calculateSlimeDirection();
    }

    moveCell(dt);

    if (Parameters.applyCellGrowth) {
      applyCellDivision();
    }
  }

  void moveCell(double dt) {
    double cellNewDir;
//    Vec2 slimeDirection = new Vec2();
    Vec2 SmotilityDirection = new Vec2();

    cellNewDir = calculateAmotilityDirection();

    if (Parameters.neighborCellAlignmentFlag) {
      switch (Parameters.selectedCellAlignmentModel) {
        case NeighborAlignment:
          neighborAvgDir = calculateAvgNeighborDir();
          cellNewDir += neighborAvgDir * Parameters.neighborAlignmentFactor;
          break;
        case VicsekModel:
          neighborAvgDir = calculateAvgNeighborDirFromVicsekModel();
//          if (FastMath.abs(cellNewDir - neighborAvgDir) > FastMath.PI / 2) {
//            neighborAvgDir = neighborAvgDir - FastMath.PI;
//          }
          cellNewDir = neighborAvgDir;
          break;
        case NematicAlignment:
          neighborAvgDir = calculateAvgNeighborDirFromNematicAlignmentModel();
          cellNewDir = neighborAvgDir;
          break;
      }
    }

    if (Parameters.applyOrientationNoise) {
      cellNewDir += (Parameters.r.nextDouble() - 0.5f) * 2
          * Parameters.orientationRandomNoiseFactor / 2; // * Math.PI / 2;
    }

    if (Parameters.slimeCreationFlag && slimeAlignmentFlag) {
      cellNewDir += (slimeAlignmentAngle - orientation) * Parameters.slimeAlignmentFactor;
    }

    if (Parameters.neighborCellAlignmentFlag
        && (Parameters.selectedCellAlignmentModel == Parameters.CellAlignmentModel.VicsekModel
        || Parameters.selectedCellAlignmentModel == Parameters.CellAlignmentModel.NematicAlignment)) {
      // move cell in current direction
      pos.addLocal(MyVec2.unitVector((float) orientation).mul((float) (Parameters.cellSpeed * dt)));
      // Vicsek model - store current orienation and update orientation for next time step
      oldOrientation = orientation;
      orientation = (float) cellNewDir;
    } else {
      orientation = (float) cellNewDir;
      pos.addLocal(MyVec2.unitVector((float) orientation).mul((float) (Parameters.cellSpeed * dt)));
    }

    if (Parameters.applyCellCellRepulsion && checkNeighborCellRepulsion()) {
      Vec2 repulsionMoveDir = new Vec2();
      repulsionMoveDir.set(repulsionDir.mul((float) Parameters.repulsionFactor));
      if (repulsionDir.length() > 0 && Parameters.drawRepulsionDir) {
        Globals.dbgDraw.drawVector(pos, repulsionDir, 5, Color.RED);
      }
//      cellDir.normalize();
      pos.addLocal(repulsionMoveDir.mul((float) (Parameters.cellSpeed * dt)));
    }

    if (Parameters.applyPeriodicBoundary) {
      applyPeriodicBoundaries();
    }
//    LOGGER.info("Cell: " + cellID + " posx: " + pos.x + " posy: " + pos.y + " ori: " + orientation);
  }

  double calculateAmotilityDirection() {
    double AmotilityDirection;
    switch (Parameters.selectedMovementModel) {
      case CellFixedVelocity:
        AmotilityDirection = orientation;
        break;
      case CellRandomWalk:
        AmotilityDirection = Parameters.r.nextDouble() * 2 * Math.PI;
        break;
      case CellFixedVelocityWithNoise:
        AmotilityDirection = orientation + (Parameters.r.nextDouble() - 0.5)
            * 2 * Math.PI / 2 * Parameters.orientationRandomNoiseFactor;
        break;
      case CellRandomWalkWithNeighborReinforcement:
        AmotilityDirection = orientation + (Parameters.r.nextDouble() - 0.5)
            * 2 * Math.PI / 2 * Parameters.orientationRandomNoiseFactor;
        AmotilityDirection *= neighborCells.size() * 0.1f;
        break;
      default:
        AmotilityDirection = orientation;
        break;
    }
    return AmotilityDirection;
  }

  void applySideContactSignaling() {
    signalSensitive = (reversalClock > Parameters.refractoryPeriod);
    if (signalSensitive) {
      double[] distances;
      boolean reversed = false;
      for (int ID : neighborCells) {
        Cell cell = Globals.simulation.cellArray.get(ID);
        if (FastMath.abs(FastMath.abs(cell.orientation - orientation) - FastMath.PI)
            < Parameters.alignOriThreshold && !reversed) {
          distances = calculateOverlap(cell.cellID);
          if (distances[0] <= Parameters.cellWidth
              && distances[1] <= Parameters.cellLength / 2
              && Parameters.r.nextDouble() < Parameters.sideSignalProb) {
            resetReversalClock();
            reversed = true;
          }
        }
      }
    }
  }

  double[] calculateOverlap(int otherCellID) {
    double[] distances = new double[2];
    double parallelDist, perpendDist;

    Cell otherCell = Globals.simulation.cellArray.get(otherCellID);
    Vec2 thisDir = MyVec2.unitVector((float) orientation);
    Vec2 otherDir = MyVec2.unitVector((float) otherCell.orientation);
    Vec2 cellToOtherCellVector = (otherCell.pos).sub(pos);
    Vec2 avgDir = thisDir.sub(otherDir);
    avgDir.normalize();

    parallelDist = Math.abs(Vec2.dot(cellToOtherCellVector, avgDir));
    perpendDist = (cellToOtherCellVector.sub(avgDir.mul((float) parallelDist))).length();

    distances[0] = parallelDist;
    distances[1] = perpendDist;
    return distances;
  }

  void applyPeriodicBoundaries() {
    if (pos.x > Parameters.worldWidth) {
      pos.x -= Parameters.worldWidth;
    }
    if (pos.x < 0) {
      pos.x += Parameters.worldWidth;
    }

    if (pos.y > Parameters.worldHeight) {
      pos.y -= Parameters.worldHeight;
    }
    if (pos.y < 0) {
      pos.y += Parameters.worldHeight;
    }
  }

  void storeCellNeighbors() {
    neighborCells.clear();
    neighborCells = getNeighborCells(2 * Parameters.neighborSearchRadius, 2 * Parameters.neighborSearchRadius);
    neighborAvgDir = 0.0;
    minNeighborDistVector = new Vec2(4 * (float) Parameters.cellLength, 4 * (float) Parameters.cellLength);
  }

  void applyNeighborCellAlignmentOld() {
    double avgDirection = 0;
    double diffTheta = 0;
    double cellOrientation;
    int count = 0;
    for (int ID : neighborCells) {
      Cell cell = Globals.simulation.cellArray.get(ID);
      if (cell.cellID != cellID) {
        cellOrientation = cell.orientation % Math.PI;
        avgDirection += Math.sin(cellOrientation) / Math.cos(cellOrientation);
        count++;
      }
    }
    avgDirection /= count;
    avgDirection = Math.atan(avgDirection); // add PI/2 to convert asin() range to [0,PI] form [-PI/2,PI/2]
    if (avgDirection < 0) {
      avgDirection += Math.PI;
    }

    diffTheta = (orientation - avgDirection);
    if (diffTheta > Math.PI) {
      diffTheta -= Math.PI;
    } else if (diffTheta < -Math.PI) {
      diffTheta += Math.PI;
    }
    orientation -= Math.sin(2 * diffTheta);
  }

  double calculateAvgNeighborDir() {
    double avgDirection = 0;
    int count = 0;
    for (int ID : neighborCells) {
      Cell cell = Globals.simulation.cellArray.get(ID);
      if (cell.cellID != cellID) {
        avgDirection += Math.sin(2 * (cell.orientation - orientation));
        count++;
      }
    }
    if (count > 0) {
      avgDirection /= count;
    }
    return avgDirection;
  }

  double calculateAvgNeighborDirFromVicsekModel() {
    double avgNeighborDirection;
    if (neighborCells.size() < 1) {
      throw new IllegalStateException("Number of neighbors zero");
    }
    if (neighborCells.size() > 1) {
      double avgSin = 0;
      double avgCos = 0;
      for (int ID : neighborCells) {
        Cell cell = Globals.simulation.cellArray.get(ID);
        avgSin += FastMath.sin(cell.oldOrientation);
        avgCos += FastMath.cos(cell.oldOrientation);
      }
      avgSin /= neighborCells.size();
      avgCos /= neighborCells.size();
//      avgNeighborDirection = FastMath.atan(avgSin / avgCos);
      avgNeighborDirection = FastMath.atan2(avgSin, avgCos); // between [-PI, PI]
    } else {
      avgNeighborDirection = oldOrientation;
    }
    return avgNeighborDirection;
  }

  double calculateAvgNeighborDirFromNematicAlignmentModel() {
    double avgNeighborDirection;
    if (neighborCells.size() < 1) {
      throw new IllegalStateException("Number of neighbors zero");
    }
    if (neighborCells.size() > 1) {
      double avgSinSqr = 0;
      double avgCosSqr = 0;
      double avgSinCos = 0;
      double sinVal, cosVal;
      for (int ID : neighborCells) {
        Cell cell = Globals.simulation.cellArray.get(ID);
        sinVal = FastMath.sin(cell.oldOrientation);
        cosVal = FastMath.cos(cell.oldOrientation);
        avgSinSqr += sinVal * sinVal;
        avgCosSqr += cosVal * cosVal;
        avgSinCos += sinVal * cosVal;
      }
      avgSinSqr /= neighborCells.size();
      avgCosSqr /= neighborCells.size();
      avgSinCos /= neighborCells.size();

      // create local tensorial traceless order parameter
      RealMatrix localOrderParameter = new Array2DRowRealMatrix(2, 2);
      localOrderParameter.setEntry(0, 0, avgCosSqr - 0.5);
      localOrderParameter.setEntry(0, 1, avgSinCos);
      localOrderParameter.setEntry(1, 0, avgSinCos);
      localOrderParameter.setEntry(1, 1, avgSinSqr - 0.5);

      // Eigenvectors of matrix
      EigenDecomposition eigDecomp = new EigenDecomposition(localOrderParameter);
      RealMatrix eigVectors = eigDecomp.getV();
      double[] firstEigenVector = eigVectors.getColumn(0);

      avgNeighborDirection = FastMath.atan2(firstEigenVector[1], firstEigenVector[0]); // between [-PI, PI]
    } else {
      avgNeighborDirection = oldOrientation;
    }
    return avgNeighborDirection;
  }

  double getDaughterCellDirection() {
    double daughterCellDir;
    switch (Parameters.selectedDaughterCellDirModel) {
      case RandomDirection:
        daughterCellDir = getInitialCellDirection();
        break;
      case UniformDirection:
        daughterCellDir = Parameters.daughterCellUniformDir;
        break;
      case ParentCellDirection:
        daughterCellDir = orientation;
        break;
      case ParentCellNegDirection:
        daughterCellDir = orientation + Math.PI;
        break;
      case ParentCellDirectionWithProb:
        if (Parameters.r.nextDouble() < Parameters.daughterCellParentDirProb) {
          daughterCellDir = orientation;
        } else {
          daughterCellDir = orientation + Math.PI;
        }
        break;
      default:
        daughterCellDir = orientation;
        break;
    }
    return daughterCellDir;
  }

  void applyCellDivision() {
    if (Parameters.r.nextDouble() < (Parameters.cellGrowthRate * Parameters.timeStep)) {
      double x = pos.x + (Parameters.r.nextDouble() - 0.5) * 2 * Parameters.cellLength;
      double y = pos.y + (Parameters.r.nextDouble() - 0.5) * 2 * Parameters.cellLength;

      Cell cell = new Cell(Globals.simulation.cellArray.size()
          + Globals.simulation.newCellsList.size() + 1, (float) x, (float) y, getDaughterCellDirection());
      Globals.simulation.newCellsList.add(cell);
    }
  }

  public static double getInitialCellDirection() {
    double orientation;
    switch (Parameters.selectedInitialOrientationModel) {
      case RandomOrientation:
        orientation = Parameters.r.nextDouble() * 2 * FastMath.PI;
        break;

      case UniformOrientation:
        orientation = Parameters.initialCellOrientation;
        break;

      case ParitialUniformOrienation:
        if (Parameters.r.nextDouble() < Parameters.initialRandomOrientationCellFraction) {
          orientation = Parameters.r.nextDouble() * 2 * FastMath.PI;
        } else {
          orientation = Parameters.initialCellOrientation;
        }
        break;

      default:
        orientation = 0;
    }

    return orientation;
  }

  ArrayList<Integer> getNeighborCells(double searchWidth, double searchHeight) {

    double[] searchLowPoint = new double[2];
    double[] searchHighPoint = new double[2];
    searchLowPoint[0] = this.pos.x - searchWidth / 2.f;
    searchLowPoint[1] = this.pos.y - searchHeight / 2.f;
    searchHighPoint[0] = this.pos.x + searchWidth / 2.f;
    searchHighPoint[1] = this.pos.y + searchHeight / 2.f;

    if (Parameters.drawSearchAABB) {
      Globals.dbgDraw.drawRectangle(this.pos, (float) searchWidth, (float) searchHeight, Color3f.RED);
      Globals.dbgDraw.drawSegment(new Vec2(pos.x - (float) searchWidth / 2, pos.y),
          new Vec2(pos.x + (float) searchWidth / 2, pos.y), Color3f.RED);
      Globals.dbgDraw.drawSegment(new Vec2(pos.x, pos.y - (float) searchHeight / 2),
          new Vec2(pos.x, pos.y + (float) searchHeight / 2), Color3f.RED);
    }

    Object[] neighborObjects = Globals.simulation.kdTree.range(searchLowPoint, searchHighPoint);

    ArrayList<Integer> neighbors = new ArrayList<>();
    for (int i = 0; i < neighborObjects.length; i++) {
      neighbors.add(((Cell) neighborObjects[i]).cellID);
    }

    if (Parameters.applyPeriodicBoundary) {
      searchNeighborsAtPeriodicBounday(searchWidth, searchHeight, neighbors);
    }

//    LOGGER.debug("Num of neighborCells: " + neighbors.length);
    if (Parameters.drawSearchAABB) {
      Globals.dbgDraw.drawString(pos, Integer.toString(neighbors.size()), Color3f.RED);
    }
    Set<Integer> uniqueCellIDs = new HashSet<>(neighbors);
    neighbors.clear();
    neighbors.addAll(uniqueCellIDs);

    return neighbors;
  }

  void searchNeighborsAtPeriodicBounday(double searchWidth, double searchHeight,
                                        ArrayList<Integer> neighbors) {
    boolean leftFlag, rightFlag, bottomFlag, topFlag;
    leftFlag = this.pos.x < Parameters.neighborSearchRadius;
    rightFlag = (Parameters.worldWidth - this.pos.x) < Parameters.neighborSearchRadius;
    bottomFlag = this.pos.y < Parameters.neighborSearchRadius;
    topFlag = (Parameters.worldHeight - this.pos.y) < Parameters.neighborSearchRadius;
    int sumFlags = booleanToInt(leftFlag) + booleanToInt(rightFlag)
        + booleanToInt(topFlag) + booleanToInt(bottomFlag);
    if (sumFlags > 2) {
      throw new IllegalStateException("Bounday conditions problem");
    }
    if (sumFlags > 0) {
      Vec2 otherSearchPos;
      if (sumFlags == 2) {
//        if (leftFlag && bottomFlag) {
        otherSearchPos = new Vec2(Parameters.worldWidth, Parameters.worldHeight);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
//        } else if (rightFlag && topFlag) {
        otherSearchPos = new Vec2(0, 0);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
//        } else if (rightFlag && bottomFlag) {
        otherSearchPos = new Vec2(0, Parameters.worldHeight);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
//        } else if (leftFlag && topFlag) {
        otherSearchPos = new Vec2(Parameters.worldWidth, 0);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
//        }

      } else if (leftFlag) {
        otherSearchPos = new Vec2(Parameters.worldWidth, this.pos.y);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
      } else if (rightFlag) {
        otherSearchPos = new Vec2(0, this.pos.y);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
      } else if (topFlag) {
        otherSearchPos = new Vec2(this.pos.x, 0);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
      } else if (bottomFlag) {
        otherSearchPos = new Vec2(this.pos.x, Parameters.worldHeight);
        searchAndAddPeriodicBoundaryNeighbors(otherSearchPos, searchWidth, searchHeight, neighbors);
      }
    }
  }

  void searchAndAddPeriodicBoundaryNeighbors(Vec2 searchPos, double searchWidth,
                                             double searchHeight, ArrayList<Integer> neighbors) {
    Object[] neighborObjectsAtOtherBoundary;
    double[] searchLowPoint = new double[2];
    double[] searchHighPoint = new double[2];
    searchLowPoint[0] = searchPos.x - searchWidth / 2.f;
    searchLowPoint[1] = searchPos.y - searchHeight / 2.f;
    searchHighPoint[0] = searchPos.x + searchWidth / 2.f;
    searchHighPoint[1] = searchPos.y + searchHeight / 2.f;

    if (Parameters.drawSearchAABB) {
      Globals.dbgDraw.drawRectangle(searchPos, (float) searchWidth, (float) searchHeight,
          DebugDrawJ2D.getColor3f(Color.YELLOW));
      Globals.dbgDraw.drawSegment(new Vec2(searchPos.x - (float) searchWidth / 2, searchPos.y),
          new Vec2(searchPos.x + (float) searchWidth / 2, searchPos.y), DebugDrawJ2D.getColor3f(Color.YELLOW));
      Globals.dbgDraw.drawSegment(new Vec2(searchPos.x, searchPos.y - (float) searchHeight / 2),
          new Vec2(searchPos.x, searchPos.y + (float) searchHeight / 2), DebugDrawJ2D.getColor3f(Color.YELLOW));
    }

    neighborObjectsAtOtherBoundary = Globals.simulation.kdTree.range(searchLowPoint, searchHighPoint);

    Cell cell;
    for (Object neighborObject : neighborObjectsAtOtherBoundary) {
      cell = (Cell) neighborObject;
      double dx = FastMath.abs(cell.pos.x - pos.x);
      double dy = FastMath.abs(cell.pos.y - pos.y);
      if (dx > Parameters.neighborSearchRadius) {
        dx = Parameters.worldWidth - dx;
      }
      if (dy > Parameters.neighborSearchRadius) {
        dy = Parameters.worldHeight - dy;
      }
      if (FastMath.sqrt(dx * dx + dy * dy) < Parameters.neighborSearchRadius) {
        neighbors.add(cell.cellID);
      }
    }
  }

  Vec2 getPeriodicPoint(Vec2 tPos) {
    Vec2 periodicPos = new Vec2();
    periodicPos.set(tPos);
    periodicPos.x = (periodicPos.x + Parameters.worldWidth) % Parameters.worldWidth;
    periodicPos.y = (periodicPos.y + Parameters.worldHeight) % Parameters.worldHeight;
    return periodicPos;
  }

  int booleanToInt(boolean b) {
    return b ? 1 : 0;
  }

  void calculateNeighborMassCenter() {
    int count = 0;
    neighborsCOM = new Vec2();
    Vec2 thisToCellVector;
    double dist;
    for (int ID : neighborCells) {
      Cell cell = Globals.simulation.cellArray.get(ID);
      if (cell.cellID != cellID) {
        thisToCellVector = pos.sub(cell.pos);
        neighborsCOM.addLocal(thisToCellVector);
        dist = thisToCellVector.length();
        if (dist < Parameters.cellWidth) {
          if (dist < minNeighborDistVector.length()) {
            minNeighborDistVector.set(thisToCellVector);
          }
        }
        count++;
      }
    }
    if (count > 0) {
      neighborsCOM.mulLocal(1.0f / count);
//      neighborsCOM.addLocal(pos);
    }
  }

  boolean checkNeighborCellRepulsion() {
    boolean isRepelled = false;
    repulsionDir = new Vec2();
    if (neighborCells.size() > 1) {
      isRepelled = true;
      switch (Parameters.selectedCellRepulsionModel) {
        case NeighborCOM:
          calculateNeighborMassCenter();
//          repulsionDir = pos.sub(neighborsCOM);
//          repulsionDir.set(neighborsCOM);
          if (minNeighborDistVector.length() < Parameters.cellWidth) {
            repulsionDir.set(minNeighborDistVector);
            repulsionDir.normalize();
          }
          break;
        case HardSphere:
          double maxOverlap = 0f;
          for (int ID : neighborCells) {
            Cell otherCell = Globals.simulation.cellArray.get(ID);
            double overlap = otherCell.pos.sub(pos).length();
            if (overlap < Parameters.cellWidth) {
              maxOverlap = overlap;
              repulsionDir.set(pos.sub(otherCell.pos));
            }
          }
          repulsionDir.normalize();
          break;
        case SoftSphere:
          break;
        case HardSphereMultiCollision:
          repulsionDir.set(0, 0);
          double overlap;
          Vec2 contactNormal = new Vec2();
          for (int ID : neighborCells) {
            Cell otherCell = Globals.simulation.cellArray.get(ID);
            if (cellID != otherCell.cellID) {
              overlap = Parameters.cellWidth - pos.sub(otherCell.pos).length();
              if (overlap > 0) {
                contactNormal.set(pos.sub(otherCell.pos));
                contactNormal.normalize();
                repulsionDir.addLocal(contactNormal.mul((float) overlap));
              }
            }
          }
          break;
      }
    }
    return isRepelled;
  }

  void createSlime() {
    Vec2 slimePos = new Vec2();
    slimePos.set(pos.x, pos.y);
    Vec2 gridPos = Globals.simulation.slimeGrid.getGridPos(slimePos);
    Globals.simulation.slimeGrid.depositSlime(gridPos, orientation);
  } // end method createSlime

  /**
   * slime alignment based on circular search region in front of the cell
   */
  void calculateSlimeDirection() {
    Vec2 cellTipPosS, slimeGridPosS, slimePos;
    Vec2 headVector, slimeVector;
    double cellBodyAngle;
    double slimeAngle = 0;
    long uniqueIDS;
    Slime s;
//    double maxSlimeVolumePerBox = Parameters.slimeProduceRate/Parameters.slimeDegradeRateConstant;

    foundSlimeBins = false;
    slimeAlignmentAngle = 0f;

    cellBodyAngle = orientation;
    cellTipPosS = new Vec2(pos.x, pos.y);
    cellTipPosS.addLocal((float) (FastMath.cos(orientation) * Parameters.slimeGridWidth),
        (float) (FastMath.sin(orientation) * Parameters.slimeGridWidth));

    // find the grid positions corresponding to the tip positions
    slimeGridPosS = Globals.simulation.slimeGrid.getGridPos(cellTipPosS);
//    LOGGER.debug("cell: " + cellID + cellTipPosS + ":" + slimeGridPosS);

    // find all slime grid positions within a semicircle with radius equal to piliLength
    // in front of the cell
    // Define a square region with whose sides are of size sqrt(2)*2*piliLength
    int numSlimeSearchGrid = (int) ((Math.sqrt(2) * Parameters.slimeSearchRadius) / Parameters.slimeGridWidth) + 1;
    int left = (int) (slimeGridPosS.x - numSlimeSearchGrid);
    int right = (int) (slimeGridPosS.x + numSlimeSearchGrid);
    int bottom = (int) (slimeGridPosS.y - numSlimeSearchGrid);
    int top = (int) (slimeGridPosS.y + numSlimeSearchGrid);

    // correct for boundary conditions
    left = left < 0 ? 0 : left;
    bottom = bottom < 0 ? 0 : bottom;
    right = right > Globals.simulation.slimeGrid.gridNumX ? Globals.simulation.slimeGrid.gridNumX : right;
    top = top > Globals.simulation.slimeGrid.gridNumY ? Globals.simulation.slimeGrid.gridNumY : top;

    // Create a semi-circle infront of cell head and divide into bins
    // Find the volume of slime in each bin covered by semi-circle area
    headVector = MyVec2.unitVector((float) orientation);
    int bin;
    double[] slimeSearchAngleBins = new double[Parameters.slimeSearchNumBins];
    for (int i = left; i <= right; i++) {
      for (int j = bottom; j <= top; j++) {
        slimePos = Globals.simulation.slimeGrid.getPositionFromGridNum(new Vec2(i, j));
        uniqueIDS = Globals.simulation.slimeGrid.getGridUniqueID(new Vec2(i, j));
        s = Globals.simulation.slimeTrailArray.get(uniqueIDS);
        if (s != null) {
          if (MyVec2.getEuclidDistance(cellTipPosS, slimePos) <= Parameters.slimeSearchRadius) {
            slimeVector = MyVec2.unitVector(cellTipPosS, slimePos);
            double angle = MyVec2.getAngle(headVector, slimeVector); // angle between heading vector and a vector from cell Tip to slime position
            if (angle < Math.PI / 2) {
//              gc.setStroke(Color.RED);
//              gc.strokeRect(slimePos.x - Parameters.slimeGridWidth / 2,
//                      slimePos.y - Parameters.slimeGridWidth / 2,
//                      Parameters.slimeGridWidth, Parameters.slimeGridWidth);
              if (MyVec2.cross(headVector, slimeVector) > 0) {
                angle += Math.PI / 2;
              } else {
                angle = Math.PI / 2 - angle;
              }
              bin = (int) (angle / (Math.PI / Parameters.slimeSearchNumBins));
              slimeSearchAngleBins[bin] += s.volume;
            }
          }
        }
      }
    }

    // Find the bin with maximum slime
    double maxSlime = 0;
    int maxBinPos = -1;
    for (int i = 0; i < Parameters.slimeSearchNumBins; i++) {
      if (slimeSearchAngleBins[i] > maxSlime) {
        maxSlime = slimeSearchAngleBins[i];
        maxBinPos = i;
      }
    }

    /* draw search area and the sectors */
    if (Parameters.drawSlimeAABBFlag) {
      MyRectangle slimeSearchRectangle = new MyRectangle(cellTipPosS,
          FastMath.sqrt(2) * 1.5 * Parameters.slimeSearchRadius,
          FastMath.sqrt(2) * 1.5 * Parameters.slimeSearchRadius, cellBodyAngle);
//      slimeSearchRectangle.drawRectangle();      
      Globals.dbgDraw.drawSolidCircle(new Vec2((float) (cellTipPosS.x - Parameters.slimeSearchRadius),
              (float) (cellTipPosS.y - Parameters.slimeSearchRadius)),
          2 * (float) Parameters.slimeSearchRadius, null, Color3f.GREEN);
      for (int i = 0; i < Parameters.slimeSearchNumBins + 1; i++) {
        Vec2 pos2 = MyVec2.pointAtDistance(cellTipPosS,
            (float) ((cellBodyAngle - Math.PI / 2) + Math.PI / Parameters.slimeSearchNumBins * i),
            (float) Parameters.slimeSearchRadius);
        Globals.dbgDraw.drawSegment(cellTipPosS, pos2, Color3f.GREEN);
      }
      if (maxBinPos >= 0) {
        for (int i = 0; i < Parameters.slimeSearchNumBins; i++) {
          Vec2 pos2 = MyVec2.pointAtDistance(cellTipPosS,
              (float) ((cellBodyAngle - Math.PI / 2) + Math.PI / Parameters.slimeSearchNumBins * (i + 0.5)),
              (float) (Parameters.slimeSearchRadius * slimeSearchAngleBins[i] / slimeSearchAngleBins[maxBinPos]));
          Globals.dbgDraw.drawSegment(cellTipPosS, pos2, Color3f.RED);
        }
      }
    }

    // If multiple high slime volume bins are found, choose one of them randomly
    // else choose the maximum slime volume bin as slime direction
    if (maxBinPos >= 0) {

      foundSlimeBins = true;
      ArrayList<Integer> equalVolumeBins = new ArrayList<>();
      for (int i = 0; i < Parameters.slimeSearchNumBins; i++) {
        if (slimeSearchAngleBins[i] / slimeSearchAngleBins[maxBinPos] >= 0.8) {
          equalVolumeBins.add(i);
        }
      }

      // calculate deviation of bin angles from cell body (center bin has least deviation)
      double maxAngle = 250; // random large value
//      double[] binAngles = {72, 36, 0, 36, 72};
      double[] binAngles = new double[Parameters.slimeSearchNumBins];
      if (Parameters.slimeSearchNumBins % 2 == 1) {
        for (int i = 0, j = Parameters.slimeSearchNumBins - 1; i <= (int) (Parameters.slimeSearchNumBins / 2); i++, j--) {
          binAngles[i] = (Math.PI / Parameters.slimeSearchNumBins) * ((int) (Parameters.slimeSearchNumBins / 2) - i);
          binAngles[j] = (Math.PI / Parameters.slimeSearchNumBins) * (j - (int) (Parameters.slimeSearchNumBins / 2));
        }
      } else {
        for (int i = 0, j = Parameters.slimeSearchNumBins - 1; i < (int) (Parameters.slimeSearchNumBins / 2); i++, j--) {
          binAngles[i] = (Math.PI / Parameters.slimeSearchNumBins)
              * ((int) (Parameters.slimeSearchNumBins / 2) - i - 0.5);
          binAngles[j] = (Math.PI / Parameters.slimeSearchNumBins)
              * (j - (int) (Parameters.slimeSearchNumBins / 2) - 0.5);
        }
      }

      // choose slime bin from maximum slime volume bins based on 
      // least deviation from current cell travel direction
      if (equalVolumeBins.size() > 1) {
//        int pos = Parameters.r.nextInt(equalVolumeBins.size());
//        maxBinPos = equalVolumeBins.get(pos);
        maxAngle = 250;
        // count from left or right end of bins with equal probability
        if (Parameters.r.nextDouble() < 0.5) {
          for (int i = 0; i < equalVolumeBins.size(); i++) {
            if (binAngles[equalVolumeBins.get(i)] < maxAngle) {
              maxBinPos = equalVolumeBins.get(i);
            }
          }
        } else {
          for (int i = equalVolumeBins.size() - 1; i >= 0; i--) {
            if (binAngles[equalVolumeBins.get(i)] < maxAngle) {
              maxBinPos = equalVolumeBins.get(i);
            }
          }
        }
      }

      if (maxBinPos >= 0) {
        slimeAngle = cellBodyAngle + (Math.PI / Parameters.slimeSearchNumBins) * (maxBinPos + 0.5f) - Math.PI / 2;
//        LOGGER.debug("cellID: " + cellID + " max slime bin angle: " + 
//                (slimeAngle-cellBodyAngle+MathUtils.HALF_PI)*MathUtils.RAD2DEG);
//        slimeAngle = (bodyAngle - MathUtils.HALF_PI) + (MathUtils.PI / numBins) * (maxPos + 0.5f);            
        slimeAlignmentTimeCounter = 0f;
        slimeAlignmentFlag = true;
      }
    }

    // update slime alignment counter
    slimeAlignmentTimeCounter += Parameters.timeStep;
//    if (cellID == 1) {
//        LOGGER.debug("cellID: "+cellID+
//            " slime seek time counter: "+timeCounter);
//    }

    if (slimeAlignmentFlag) {
      if (slimeAlignmentTimeCounter > Parameters.slimeAttractionTime) {
        slimeAlignmentFlag = false;
        slimeAlignmentAngle = 0f;
//        timeCounter = 0f;
//        LOGGER.debug("slimeAttractionFlag un-set for cell: "+cell);
      } else if (maxBinPos >= 0 && slimeSearchAngleBins[maxBinPos] > 0.001f) {

        // reduce slime attraction force only if slime volume in current block
        // is less than half of the original deposit volume
        slimeForceFraction = 1f;
        int numGridInSlimeSearchArea = (int) (Globals.simulation.slimeGrid.gridAreaDensity
            * Math.PI * Parameters.slimeSearchRadius * Parameters.slimeSearchRadius / 2);
        if (slimeSearchAngleBins[maxBinPos] < Parameters.thresholdSlimeVolume
            * (numGridInSlimeSearchArea / Parameters.slimeSearchNumBins)) {
          slimeForceFraction = (slimeSearchAngleBins[maxBinPos]
              / (Parameters.thresholdSlimeVolume * (numGridInSlimeSearchArea
              / Parameters.slimeSearchNumBins)));
        }
//        LOGGER.debug("cellID:  " + cellID + " slimeForceFraction: " + slimeForceFraction);

        slimeAlignmentAngle = slimeAngle;
//        LOGGER.debug("slimeAlignmentAngle: " + slimeAlignmentAngle);
        slimeAlignmentDir = (MyVec2.unitVector((float) slimeAlignmentAngle)).
            mulLocal((float) (Parameters.slimeAlignmentFactor * slimeForceFraction));
      } else {
        // if slime blocks are not found, direction is maintained as per the last
        // slime block direction
        slimeAlignmentDir = (MyVec2.unitVector((float) slimeAlignmentAngle)).
            mulLocal((float) (Parameters.slimeAlignmentFactor * slimeForceFraction));
      } // end of if-else block for if slimeBodies found
//      LOGGER.debug("cellID: " + cellID + " slimeAlignmentSegmentForce: " + 
//              slimeAlignmentSegmentForce.length()); // end slimeAlignmentTimeCounter
    }

    if (Parameters.drawSlimeAlignmentDirFlag && slimeAlignmentDir.length() > 0) {
      Globals.dbgDraw.drawVector(pos, slimeAlignmentDir, 5, Color.YELLOW);
    }

  } // end method getSlimeDirection()
}
