package model;

import lombok.Data;

@Data
public class Cell {
    /**
     * Внешний радиус ячейки
     */
    private Double r;

    /**
     * Угол между радиусом ячейки и положительным направлением ОХ
     * (имеется ввиду радиус, проведенный через центр ячейки)
     */
    private Double fi;

    /**
     * Шаг ралиального деления
     */
    private Double fiStep;

    /**
     * Текущая высота ячейки
     */
    private Double h;

    /**
     * Текущая величина прогиба боковой стенки
     */
    private Double delta;

    /**
     * Текущее давление
     */
    private Double pressure;

    /**
     * Площадь сечения
     * (от центра самого диска до внешней стенки этой ячейки)
     */
    private Double square;

    /**
     * Текущий объем ячейки
     */
    private Double volume;

    /**
     * Начальная инициализация ячейки
     */
    public void init(double r, double fi, double fiStep, double h, double delta, double pressure) {
        this.r = r;
        this.fi = fi;
        this.fiStep = fiStep;
        this.h = h;
        this.delta = delta;
        this.pressure = pressure;

        this.square = r * h + (2 * h * delta) / Math.PI;
        this.volume = square * fiStep;
    }
}
