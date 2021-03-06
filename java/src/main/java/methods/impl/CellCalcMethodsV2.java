package methods.impl;

import lombok.RequiredArgsConstructor;
import methods.CellCalcMethods;
import model.Cell;

@RequiredArgsConstructor
public class CellCalcMethodsV2 implements CellCalcMethods {

    private final double originalDiskH;

    @Override
    public double initial(Cell cell) {
        return calcVolume(cell, 0);
    }

    @Override
    public double calcVolume(Cell cell, double alpha) {
        // если предыдущей ячейки нет - имеем дело с ячейкой центрального кольца
        if(cell.isCentral()) {
            // к объему трапеции добавим объем выпячивания
            return calcMainVolumeForCentralCell(cell, alpha) + protrusionVolume(cell, alpha);
        } else {
            // к объему трапеции добавим объем выпячивания и вычтем объем выпячивания предыдущей ячейки
            return calcMainVolumeForOthers(cell, alpha)
                    + protrusionVolume(cell, alpha)
                    - protrusionVolume(cell.getPrevious(), alpha);
        }
    }

    @Override
    public double calcDelta(Cell cell, double alpha) {
        double triangleSide = rProjection(cell, alpha);
        double baseSide = baseSide(triangleSide, triangleSide, cell.getFiStep());
        // считаем delta исходя из того, что стенки непроницаемы (т.е. объем не изменился)
        // если предыдущей ячейки нет - имеем дело с ячейкой центрального кольца
        if(cell.isCentral()) {
            // TODO тоже вычесть объем выпячивания, который дает пульпозное ядро
            return (cell.getVolume() - calcMainVolumeForCentralCell(cell, alpha)) * Math.PI / (2 * cell.getH() * baseSide);
        } else {
            return (cell.getVolume() - calcMainVolumeForOthers(cell, alpha) + protrusionVolume(cell.getPrevious(), alpha)) * Math.PI / (2 * cell.getH() * baseSide);
        }
    }

    @Override
    public double calcHeight(Cell cell, double alpha) {
        return originalDiskH + 2 * halfDeltaH(cell, alpha);
    }

    private double calcMainVolumeForCentralCell(Cell cell, double alpha) {
        double triangleSide = rProjection(cell, alpha);
        double triangleSquare = triangleSquare(triangleSide, triangleSide, cell.getFiStep());

        return triangleSquare * cell.getH();
    }

    private double calcMainVolumeForOthers(Cell cell, double alpha) {
        double previousTriangleSide = rProjection(cell.getPrevious(), alpha);
        double previousTriangleSquare = triangleSquare(previousTriangleSide, previousTriangleSide, cell.getPrevious().getFiStep());

        double triangleSide = rProjection(cell, alpha);
        double triangleSquare = triangleSquare(triangleSide, triangleSide, cell.getFiStep());

        double trapezeSquare = triangleSquare - previousTriangleSquare;

        return trapezeSquare * cell.getH();
    }

    public static double protrusionVolume(Cell cell, double alpha) {
        double triangleSide = rProjection(cell, alpha);
        double baseSide = baseSide(triangleSide, triangleSide, cell.getFiStep());

        return (2 * cell.getDelta() * cell.getH() * baseSide) / Math.PI;
    }

    public static double baseSide(double a, double b, double angle) {
        return Math.sqrt(a * a + b * b - 2 * a * b * Math.cos(angle));
    }

    private double triangleSquare(double a, double b, double angle) {
        return 0.5 * a * b * Math.sin(angle);
    }

    // со знаком
    public static double halfDeltaH(Cell cell, double alpha) {
        return (Math.sin(alpha) * cell.getR()) * Math.cos(cell.getFi());
    }

    public static double rProjection(Cell cell, double alpha) {
        double halfDeltaH = halfDeltaH(cell, alpha);
        return Math.sqrt(Math.pow(cell.getR(), 2) - Math.pow(halfDeltaH, 2));
    }
}
