package model;

import lombok.Data;

@Data
public class Cell {
    /**
     * Внешний радиус ячейки
     */
    private double r;

    /**
     * Угол между радиусом ячейки и положительным направлением ОХ
     * (имеется ввиду радиус, проведенный через центр ячейки)
     */
    private double fi;

    /**
     * Шаг ралиального деления
     */
    private double fiStep;

    /**
     * Текущая высота ячейки
     */
    private double h;

    /**
     * Текущая величина прогиба боковой стенки
     */
    private double delta;

    /**
     * Текущее давление
     */
    private double pressure;

    /**
     * Текущий объем ячейки
     */
    private double volume;

    /**
     * Начальная инициализация ячейки
     */
    public void init(double r, double fi, double fiStep, double h) {
        this.r = r;
        this.fi = fi;
        this.fiStep = fiStep;
        this.h = h;
    }

    /**
     * Клонирует поля из this в поля из target
     * @param target - куда копировать
     */
    public void cloneTo(Cell target) {
        target.r = this.r;
        target.fi = this.fi;
        target.fiStep = this.fiStep;
        target.h = this.h;
        target.delta = this.delta;
        target.pressure = this.pressure;
        target.volume = this.volume;
    }
}
