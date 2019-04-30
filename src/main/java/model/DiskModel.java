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
                        data.get_DISK_H(),
                        data.get_DISK_P() / K,
                        data.get_DISK_P()
                );

                double currS = (rStep * (i + 1)) * data.get_DISK_H() + (2 * data.get_DISK_H() * data.get_DISK_P() / K) / Math.PI;
                double prevS = (rStep * i) * data.get_DISK_H() + (2 * data.get_DISK_H() * data.get_DISK_P() / K) / Math.PI;
                double currV = i > 0 ?
                        currS * fiStep - prevS * fiStep
                        : currS * fiStep;

                cells[i][j].setVolume(currV);
            }
        }
    }

    /**
     * Изменить угол наклона пластины и пересчитать параметры ячеек
     * (на этом этапе диффузия ещё не происходит)
     */
    public void rotateAndCalculate(double alpha) {
        calculateNewDelta();
        calculateNewPressure();

        this.alpha = alpha;
        debugInfo("before rotation", 1);

        calculateNewDelta();
        calculateNewPressure();

        debugInfo("after rotation", 1);
    }

    /**
     * Начать процесс диффузии
     */
    public void beginDiffusion(double deltaTime) {
        double time = 0;
        while(!isEndOfDiffusion()) {
            //while(time <= 10 * deltaTime) {
            setupOldCells();

            calculateNewVolume(deltaTime);
            calculateNewDelta();
            calculateNewPressure();

            time += deltaTime;

            debugInfo("Step " + (int) (time / deltaTime), 1);
            /*try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        System.out.println("Время выравнивания давления " + time + " сек.");
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
                    deltaV += D * (oldCells[i - 1][j].getPressure() - oldCells[i][j].getPressure());
                } else { // прочие ячейки
                    deltaV += D * (oldCells[i - 1][j].getPressure() + oldCells[i + 1][j].getPressure() - 2 * oldCells[i][j].getPressure());
                }

                cells[i][j].setVolume(oldCells[i][j].getVolume() + deltaV * deltaT);

                //System.out.printf("%.9f\n", deltaV);
            }
            //System.out.println("");
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

                cells[i][j].setDelta(
                        Math.PI * (prevVolSum / cells[i][j].getFiStep() -
                                (cells[i][j].getR() * Math.cos(cells[i][j].getFi()) * Math.sin(alpha))
                                        *
                                        (cells[i][j].getR() * Math.cos(cells[i][j].getFi()) * Math.cos(alpha) + data.get_DISK_H())
                        ) /
                                (2 * cells[i][j].getR() * Math.cos(cells[i][j].getFi()) * Math.sin(alpha) + data.get_DISK_H())
                );
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
                    cells[i][j].setPressure(K * cells[i][j].getDelta());
                } else {
                    cells[i][j].setPressure(K * cells[i][j].getDelta());
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
                                "  cell %s: P = %.7f  V=%.7f  delta=%.7f",
                                j,
                                cells[ringNumber][j].getPressure(),
                                cells[ringNumber][j].getVolume(),
                                cells[ringNumber][j].getDelta()
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
