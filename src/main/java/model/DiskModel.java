package model;

import utils.Data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
    private Double K;

    /**
     * Угол наклона пластин
     */
    private Double alpha = 0.;

    /**
     * Входные параметры
     */
    private Data data;

    /**
     * Массив ячеек
     */
    private Cell[][] cells;

    /**
     * Значения на предыдущем шаге
     */
    private Cell[][] oldCells;

    private BufferedWriter logFile;

    public DiskModel(Data data) {
        this.data = data;
    }

    /**
     * Рассчитать исходные параметры ячеек
     */
    public void init(double d, double D, double K) {
        try {
            logFile = new BufferedWriter(new FileWriter("log.txt"));
        } catch(IOException e) {
            e.printStackTrace();
        }

        this.d = d;
        this.D = D;
        this.K = K;

        double rStep = data.get_DISK_R() / data.get_DISK_RINGS(); // шаг кольцевого деления
        double fiStep = (2 * Math.PI) / data.get_DISK_CELLS(); // шаг радиального деления

        cells = new Cell[data.get_DISK_RINGS()][];
        oldCells = new Cell[data.get_DISK_RINGS()][];

        for(int i = 0; i < data.get_DISK_RINGS(); i++) {
            cells[i] = new Cell[data.get_DISK_CELLS()];
            oldCells[i] = new Cell[data.get_DISK_CELLS()];

            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                cells[i][j] = new Cell();
                oldCells[i][j] = new Cell();

                cells[i][j].init(
                        rStep * (i + 1),
                        fiStep * j + fiStep / 2,
                        fiStep,
                        data.get_DISK_H()
                );

                double currS = (rStep * (i + 1)) * data.get_DISK_H() + (2 * data.get_DISK_H() * data.get_DISK_P() / K) / Math.PI;
                double prevS = (rStep * i) * data.get_DISK_H() + (2 * data.get_DISK_H() * data.get_DISK_P() / K) / Math.PI;
                double currV = i > 0 ?
                        currS * fiStep - prevS * fiStep
                        : currS * fiStep;

                cells[i][j].setVolume(currV);
                cells[i][j].setPressure(data.get_DISK_P());
            }
        }

        calculateNewDelta();
    }

    /**
     * Изменить угол наклона пластины и пересчитать параметры ячеек
     * (на этом этапе диффузия ещё не происходит)
     */
    public void rotateAndCalculate(double alpha) {
        this.alpha = alpha;
        debugInfo("before rotation", 1);

        calculateNewHeight();
        calculateNewDelta();
        calculateNewPressure();

        debugInfo("after rotation", 1);
    }


    /**
     * Начать процесс диффузии
     *
     * @param deltaTime величина шага по времени
     */
    public void beginDiffusion(double deltaTime) {
        beginDiffusion(deltaTime, 0);
    }

    /**
     * Начать процесс диффузии, с ограниченным количеством шагов
     *
     * @param deltaTime величина шага по времени
     * @param stepLimit количество шагов, принимает значения > 0 (при значениях меньше 0 игнорируется)
     */
    public void beginDiffusion(double deltaTime, int stepLimit) {
        double time = 0;
        for(int step = 0;  stepLimit > 0 ? step < stepLimit && !isEndOfDiffusion() : !isEndOfDiffusion(); step++) {
            setupOldCells();

            calculateNewVolume(deltaTime);
            calculateNewDelta();
            calculateNewPressure();

            time += deltaTime;

            debugInfo("Step " + (int) (time / deltaTime), 1);
        }
        System.out.println("Время выравнивания давления " + time + " сек.");
    }

    /**
     * Пересчитать "крайние" высоты ячеек после поворота
     */
    private void calculateNewHeight() {
        for(Cell[] row : cells) {
            for(Cell cell : row) {
                cell.setH(cell.getR() * Math.cos(cell.getFi()) * Math.sin(alpha) + data.get_DISK_H());
            }
        }
    }

    /**
     * Вычислить значения новых объемов
     *
     * @param deltaT шаг по времени (секунды)
     */
    private void calculateNewVolume(double deltaT) {
        setupOldCells();
        for(int i = 0; i < data.get_DISK_RINGS(); i++) {
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                double deltaV = 0;

                if(j == 0) { // первая ячейка кольца граничит с последней
                    deltaV += d * (oldCells[i][data.get_DISK_CELLS() - 1].getPressure() + oldCells[i][j + 1].getPressure() - 2 * oldCells[i][j].getPressure());
                } else if(j == data.get_DISK_CELLS() - 1) { // последняя ячейка кольца граничит с первой
                    deltaV += d * (oldCells[i][j - 1].getPressure() + oldCells[i][0].getPressure() - 2 * oldCells[i][j].getPressure());
                } else { // внутренние ячйеки кольца
                    deltaV += d * (oldCells[i][j - 1].getPressure() + oldCells[i][j + 1].getPressure() - 2 * oldCells[i][j].getPressure());
                }

                if(i == 0) { // если ячейка в пульпозном ядре
                    deltaV += D * (oldCells[i + 1][j].getPressure() - oldCells[i][j].getPressure());
                } else if(i == data.get_DISK_RINGS() - 1) { // если ячейка на крайнем кольце
                    deltaV += D * (oldCells[i - 1][j].getPressure() + data.get_OTHER_ATMP() - 2 * oldCells[i][j].getPressure());
                } else { // прочие ячейки
                    deltaV += D * (oldCells[i - 1][j].getPressure() + oldCells[i + 1][j].getPressure() - 2 * oldCells[i][j].getPressure());
                }

                cells[i][j].setVolume(oldCells[i][j].getVolume() + deltaV * deltaT);
            }
        }
    }

    /**
     * Вычисление новых величин прогиба стенок
     */
    private void calculateNewDelta() {
        setupOldCells();
        for(int j = 0; j < data.get_DISK_CELLS(); j++) {
            double prevVolSum = 0;
            for(int i = 0; i < data.get_DISK_RINGS(); i++) {
                prevVolSum += oldCells[i][j].getVolume();

                double S_comm = prevVolSum / cells[i][j].getFiStep();
                double S_cos = S_comm - (cells[i][j].getR() * Math.cos(alpha)) * ((cells[i][j].getH() + data.get_DISK_H()) / 2);
                double delta = S_cos * (Math.PI / (2 * cells[i][j].getH()));

                cells[i][j].setDelta(delta);
            }
        }
    }

    /**
     * Вычисление новых давлений
     */
    private void calculateNewPressure() {
        setupOldCells();
        for(int i = data.get_DISK_RINGS() - 1; i >= 0; i--) {
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                if(i == data.get_DISK_RINGS() - 1) {
                    cells[i][j].setPressure(K * cells[i][j].getDelta() + data.get_OTHER_ATMP());
                } else {
                    cells[i][j].setPressure(K * cells[i][j].getDelta() + cells[i + 1][j].getPressure());
                }
            }
        }
    }

    /**
     * Обновления параметров ячеек на предыдущем шаге
     */
    private void setupOldCells() {
        for(int i = 0; i < data.get_DISK_RINGS(); i++) {
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                cells[i][j].cloneTo(oldCells[i][j]);
            }
        }
    }

    /**
     * Закончился ли процесс диффузии
     *
     * @return true - если разность давлений в ячейках меньше epsilon, иначе false
     */
    private Boolean isEndOfDiffusion() {
        double P1 = cells[1][0].getPressure();
        double P2 = cells[1][data.get_DISK_CELLS() / 2].getPressure();
        double P3 = cells[data.get_DISK_RINGS() - 1][data.get_DISK_CELLS() / 4].getPressure();
        double P4 = cells[data.get_DISK_RINGS() - 1][3 * data.get_DISK_CELLS() / 4].getPressure();
        if(
                Math.abs(P1 - P2) < data.get_OTHER_EPS() && Math.abs(P1 - P3) < data.get_OTHER_EPS() && Math.abs(P1 - P4) < data.get_OTHER_EPS() &&
                        Math.abs(P2 - P3) < data.get_OTHER_EPS() && Math.abs(P2 - P4) < data.get_OTHER_EPS() &&
                        Math.abs(P3 - P4) < data.get_OTHER_EPS()) {
            return true;
        }
        return false;
    }

    private void debugInfo(String comment, int ringNumber) {
        try {
            String title = String.format("%s (ring number is %d)", comment, ringNumber);
            logFile.write(title);
            logFile.newLine();
            System.out.println(title);
            for(int j = 0; j < data.get_DISK_CELLS(); j++) {
                String row =
                        String.format(
                                "\tcell %s:\tP = %.7f\tV=%.7f\tdelta=%.7f\tH=%.7f",
                                j,
                                cells[ringNumber][j].getPressure(),
                                cells[ringNumber][j].getVolume(),
                                cells[ringNumber][j].getDelta(),
                                cells[ringNumber][j].getH()
                        );
                logFile.write(row);
                logFile.newLine();
                System.out.println(row);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
