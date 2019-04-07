package model;

import utils.Data;

public class DiskModel {
    /**
     * Коэффициент диффузии внутри колец
     */
    private Double d;

    /**
     * Коэффициент диффузии между кольцами
     */
    private Double D;

    /**
     * Коэффициент упругости кольцевых стенок
     */
    private Double C;

    /**
     * Угол наклона пластин
     */
    private Double alpha;

    /**
     * Входные параметры
     */
    private Data data;

    /**
     * Допустимая разность давлений в ячейках
     */
    private Double epsilon = 0.001;

    /**
     * Массив ячеек
     */
    private Cell[][] cells;

    public DiskModel(Data data) {
        this.data = data;
    }

    /**
     * Рассчитать исходные параметры ячеек
     */
    public void init(double d, double D, double C) {
        this.d = d;
        this.D = D;
        this.C = C;

        double rStep = data.get_DISK_R() / data.get_DISK_RINGS(); // шаг кольцевого деления
        double fiStep = (2 * Math.PI) / data.get_DISK_CELLS(); // шаг радиального деления

        cells = new Cell[data.get_DISK_RINGS()][];
        for(int i = 0; i < data.get_DISK_RINGS(); i++) {
            cells[i] = new Cell[data.get_DISK_CELLS()];
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                cells[i][j] = new Cell();
                cells[i][j].init(
                        rStep * (i + 1),
                        fiStep * j + fiStep / 2,
                        fiStep,
                        data.get_DISK_H(),
                        data.get_DISK_P() / C,
                        data.get_DISK_P()
                );
            }
        }
    }

    /**
     * Изменить угол наклона пластины и пересчитать параметры ячеек
     * (на этом этаме диффузия не происходит)
     */
    public void rotateAndCalculate(double alpha) {
        this.alpha = alpha;

        calculateNewDelta();
        calculateNewPressure();

        isEndOfDiffusion();
    }

    /**
     * Начать процесс диффузии
     */
    public void beginDiffusion(int deltaTime) {
        int time = 0;
        while(!isEndOfDiffusion()) {
            calculateNewVolume(deltaTime);
            calculateNewDelta();
            calculateNewPressure();
            time += deltaTime;

            printInfo();
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Время выравнивания давления " + time);
    }

    /**
     * Вычислить значения новых объемов
     * @param deltaT - шаг по времени (секунды)
     */
    private void calculateNewVolume(int deltaT) {
        for(int i = 0; i < data.get_DISK_RINGS(); i++) {
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                double deltaV = 0;

                if(j == 0) { // первая ячейка кольца граничит с последней
                    deltaV += d * (cells[i][data.get_DISK_CELLS() - 1].getPressure() + cells[i][j + 1].getPressure() - 2 * cells[i][j].getPressure());
                } else if (j == data.get_DISK_CELLS() - 1) { // последняя ячейка кольца граничит с первой
                    deltaV += d * (cells[i][j - 1].getPressure() + cells[i][0].getPressure() - 2 * cells[i][j].getPressure());
                } else { // внутренние ячйеки кольца
                    deltaV += d * (cells[i][j - 1].getPressure() + cells[i][j + 1].getPressure() - 2 * cells[i][j].getPressure());
                }

                if(i == 0) { // если ячейка в пульпозном ядре
                    deltaV += D * (cells[i + 1][j].getPressure() - cells[i][j].getPressure());
                } else if(i == data.get_DISK_RINGS() - 1) { // если ячейка на крайнем кольце
                    deltaV += D * (cells[i - 1][j].getPressure() - cells[i][j].getPressure());
                } else { // прочие ячейки
                    deltaV += D * (cells[i - 1][j].getPressure() + cells[i + 1][j].getPressure() - 2 * cells[i][j].getPressure());
                }

                cells[i][j].setVolume(cells[i][j].getVolume() + deltaV * deltaT);
            }
        }
    }

    /**
     * Вычисление новых величин прогиба стенок
     */
    private void calculateNewDelta() {
        for(Cell[] cellRing : cells) {
            for(Cell cell : cellRing) {
                cell.setDelta(
                        (Math.PI * (
                                cell.getVolume() / cell.getFiStep() -
                                        (
                                                cell.getR() * Math.cos(cell.getFi()) * Math.tan(alpha) + data.get_DISK_H()
                                        ) * Math.sqrt(Math.pow(cell.getR(), 2) - Math.pow(cell.getR() * Math.cos(cell.getFi()) * Math.tan(alpha), 2))
                        )) / (
                                2 * cell.getR() * Math.cos(cell.getFi()) * Math.tan(alpha) + data.get_DISK_H()
                        )
                );
            }
        }
    }

    /**
     * Вычисление новых давлений
     */
    private void calculateNewPressure() {
        for(int i = data.get_DISK_RINGS() - 1; i >= 0; i--) {
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                if(i == data.get_DISK_RINGS() - 1) {
                    cells[i][j].setPressure(C * cells[i][j].getDelta());
                } else {
                    cells[i][j].setPressure(C * cells[i][j].getDelta() + cells[i + 1][j].getPressure());
                }
            }
        }
    }

    /**
     * Закончился ли процесс диффузии
     * @return true - если разность давлений в ячейках меньше epsilon, иначе false
     */
    private Boolean isEndOfDiffusion() {
        double P1 = cells[1][0].getPressure();
        double P2 = cells[1][data.get_DISK_CELLS() / 2].getPressure();
        double P3 = cells[data.get_DISK_RINGS() - 1][data.get_DISK_CELLS() / 4].getPressure();
        double P4 = cells[data.get_DISK_RINGS() - 1][3 * data.get_DISK_CELLS() / 4].getPressure();
        //System.out.println("проверочные давления: " + P1 + " " + P2 + " " + P3 + " " + P4);
        if(
                Math.abs(P1 - P2) < epsilon && Math.abs(P1 - P3) < epsilon && Math.abs(P1 - P4) < epsilon &&
                Math.abs(P2 - P3) < epsilon && Math.abs(P2 - P4) < epsilon &&
                Math.abs(P3 - P4) < epsilon) {
            return true;
        }
        return false;
    }

    private void printInfo() {
        for(Cell[] ring : cells) {
            for(Cell cell : ring) {
                System.out.print(cell.getPressure() + " ");
            }
            System.out.println("");
        }
        System.out.println("\n");
    }
}
