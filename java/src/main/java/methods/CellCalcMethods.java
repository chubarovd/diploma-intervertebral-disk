package methods;

import model.Cell;

/**
 * Represents a cell volume calculation method.
 */
public interface CellCalcMethods {
    /**
     * Calculate initial cell volume.
     *
     * @param cell target cell
     */
    default double initial(Cell cell) {
        return 0;
    }

    /**
     * Calculate new cell volume.
     *
     * @param cell  target cell
     * @param alpha current alpha angle
     */
    double calcVolume(Cell cell, double alpha);

    /**
     * Calculate new cell delta.
     *
     * @param cell target cell
     * @param alpha current alpha angle
     */
    double calcDelta(Cell cell, double alpha);

    /**
     * Calculate new height.
     *
     * @param cell target cell
     * @param alpha current alpha angle
     */
    double calcHeight(Cell cell, double alpha);
}
