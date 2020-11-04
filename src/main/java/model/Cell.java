package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cell {
    // region Постоянные параметры

    /**
     * Внешний радиус ячейки.
     */
    private final double r;

    /**
     * Угол между радиусом ячейки и положительным направлением ОХ
     * (имеется ввиду радиус, проведенный через центр ячейки).
     */
    private final double fi;

    /**
     * Шаг радиального деления.
     */
    private final double fiStep;

    /**
     * Граничная ячейка с предыдущего кольца
     * (null, если ячейка из центрального кольца).
     */
    private final Cell previous;

    // endregion

    // region Изменяемые параметры
    /**
     * Текущая внешняя высота ячейки.
     */
    private double h;

    /**
     * Текущая величина прогиба боковой стенки.
     */
    private double delta;

    /**
     * Текущее давление.
     */
    private double pressure;

    /**
     * Текущий объем ячейки.
     */
    private double volume;

    // endregion

    public boolean isCentral() {
        return previous == null;
    }
}
