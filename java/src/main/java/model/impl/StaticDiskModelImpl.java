package model.impl;

import model.Cell;
import model.StaticDiskModel;
import utils.Data;

import java.io.FileWriter;
import java.io.IOException;

public class StaticDiskModelImpl implements StaticDiskModel {
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

    private FileWriter logFile;
    private FileWriter pLogFile;

    public StaticDiskModelImpl(Data data) {
        this.data = data;
    }

    /**
     * Рассчитать исходные параметры ячеек
     */
    @Override
    public void init() {
        try {
            logFile = new FileWriter(data.getOTHER_LOG_FILE_NAME());
            pLogFile = new FileWriter(data.getOTHER_PRESSURES_LOG_FILE_NAME());
        } catch(IOException e) {
            e.printStackTrace();
        }

        this.d = data.getDISK_SIDE_DIFFUSION();
        this.D = data.getDISK_RING_DIFFUSION();
        this.K = data.getDISK_K();

        double rStep = data.getDISK_R() / data.getDISK_RINGS(); // шаг кольцевого деления
        double fiStep = (2 * Math.PI) / data.getDISK_CELLS(); // шаг радиального деления
        double delta = (data.getDISK_P() - data.getOTHER_ATMP()) / K; // величина исходного прогиба боковых стенок

        cells = new Cell[data.getDISK_RINGS()][];
        oldCells = new Cell[data.getDISK_RINGS()][];

        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            cells[i] = new Cell[data.getDISK_CELLS()];
            oldCells[i] = new Cell[data.getDISK_CELLS()];

            /*for(int j = 0; j < data.getDISK_CELLS(); j++) {
                );
                oldCells[i][j] = new Cell();

                double currS = (rStep * (i + 1)) * data.getDISK_H() + (2 * data.getDISK_H() * delta) / Math.PI;
                double prevS = (rStep * i) * data.getDISK_H() + (2 * data.getDISK_H() * delta) / Math.PI;
                double currV = i > 0 ? (currS * fiStep - prevS * fiStep) : (currS * fiStep);

                cells[i][j] = new Cell(
                        rStep * (i + 1),
                        fiStep * j + fiStep / 2,
                        fiStep,
                        data.getDISK_H()
                );

                cells[i][j].setDelta(delta);
                cells[i][j].setVolume(currV);
                cells[i][j].setPressure(data.getDISK_P());
            }*/
        }
    }

    /**
     * Изменить угол наклона пластины и пересчитать параметры ячеек
     * (на этом этапе диффузия ещё не происходит)
     */
    @Override
    public void rotateAndCalc(double alpha) {
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
    @Override
    public void beginDiffusion(double deltaTime) {
        beginDiffusion(deltaTime, 0);
    }

    /**
     * Начать процесс диффузии, с ограниченным количеством шагов
     *
     * @param deltaTime величина шага по времени
     * @param stepsLimit количество шагов, принимает значения > 0 (при значениях меньше 0 игнорируется)
     */
    @Override
    public void beginDiffusion(double deltaTime, int stepsLimit) {
        boolean logCheckedPressures = data.isOTHER_LOG_ALL();
        double time = 0;
        for(int step = 0; stepsLimit > 0 ? step < stepsLimit && !isEndOfDiffusion(logCheckedPressures) : !isEndOfDiffusion(logCheckedPressures); step++) {
            setupOldCells();

            calculateNewVolume(deltaTime);
            calculateNewDelta();
            calculateNewPressure();

            time += deltaTime;

            debugInfo("Step " + (int) (time / deltaTime), 1);
        }
        if(logCheckedPressures) {
            try {
                pLogFile.write(String.format("%d", (int) time));
            } catch(IOException e) {
                System.out.println("WARN: pressure logging failed");
            }
        }
        System.out.println("Время выравнивания давления " + ((int) (time / deltaTime)) + " сек.");
    }

    /**
     * Пересчитать "крайние" высоты ячеек после поворота
     */
    private void calculateNewHeight() {
        for(Cell[] row : cells) {
            for(Cell cell : row) {
                cell.setH(cell.getR() * Math.cos(cell.getFi()) * Math.sin(alpha) + data.getDISK_H());
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
        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                double deltaV = 0;

                if(j == 0) { // первая ячейка кольца граничит с последней
                    deltaV += d * (oldCells[i][data.getDISK_CELLS() - 1].getPressure() + oldCells[i][j + 1].getPressure() - 2 * oldCells[i][j].getPressure());
                } else if(j == data.getDISK_CELLS() - 1) { // последняя ячейка кольца граничит с первой
                    deltaV += d * (oldCells[i][j - 1].getPressure() + oldCells[i][0].getPressure() - 2 * oldCells[i][j].getPressure());
                } else { // внутренние ячйеки кольца
                    deltaV += d * (oldCells[i][j - 1].getPressure() + oldCells[i][j + 1].getPressure() - 2 * oldCells[i][j].getPressure());
                }

                if(i == 0) { // если ячейка в пульпозном ядре
                    deltaV += D * (oldCells[i + 1][j].getPressure() - oldCells[i][j].getPressure());
                } else if(i == data.getDISK_RINGS() - 1) { // если ячейка на крайнем кольце - считаем, что диффузии с внешними тканями не происходит
                    deltaV += D * (oldCells[i - 1][j].getPressure() - oldCells[i][j].getPressure());
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
        for(int j = 0; j < data.getDISK_CELLS(); j++) {
            double prevVolSum = 0;
            for(int i = 0; i < data.getDISK_RINGS(); i++) {
                prevVolSum += oldCells[i][j].getVolume();

                double S_comm = prevVolSum / cells[i][j].getFiStep();
                double S_cos = S_comm - (cells[i][j].getR() * Math.cos(cells[i][j].getFi()) * Math.cos(alpha)) * ((cells[i][j].getH() + data.getDISK_H()) / 2);
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
        for(int i = data.getDISK_RINGS() - 1; i >= 0; i--) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                if(i == data.getDISK_RINGS() - 1) {
                    cells[i][j].setPressure(K * cells[i][j].getDelta() + data.getOTHER_ATMP());
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
        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                //cells[i][j].cloneTo(oldCells[i][j]);
            }
        }
    }

    /**
     * Закончился ли процесс диффузии
     *
     * @return true - если разность давлений в ячейках меньше epsilon, иначе false
     */
    private Boolean isEndOfDiffusion(boolean logCheckedPressures) {
        double P1 = cells[1][0].getPressure();
        double P2 = cells[1][data.getDISK_CELLS() / 2].getPressure();
        double P3 = cells[data.getDISK_RINGS() - 1][data.getDISK_CELLS() / 4].getPressure();
        double P4 = cells[data.getDISK_RINGS() - 1][3 * data.getDISK_CELLS() / 4].getPressure();

        if(logCheckedPressures) {
            try {
                double CP1 = cells[1][0].getPressure();
                double CP2 = cells[2][data.getDISK_CELLS() / 3].getPressure();
                double CP3 = cells[4][2 * data.getDISK_CELLS() / 3].getPressure();
                double CP4 = 0.;

                pLogFile.write(String.format("%f:%f:%f:%f\n", CP1, CP2, CP3, CP4));
            } catch(IOException e) {
                System.out.println("WARN: pressure logging failed");
            }
        }

        return Math.abs(P1 - P2) < data.getOTHER_EPS() && Math.abs(P1 - P3) < data.getOTHER_EPS() && Math.abs(P1 - P4) < data.getOTHER_EPS() &&
                Math.abs(P2 - P3) < data.getOTHER_EPS() && Math.abs(P2 - P4) < data.getOTHER_EPS() &&
                Math.abs(P3 - P4) < data.getOTHER_EPS();
    }

    private void debugInfo(String comment, int ringNumber) {
        try {
            String title = String.format("%s (ring number is %d)\n", comment, ringNumber);
            if(data.isOTHER_LOG_TO_FILE()) {
                logFile.write(title);
            }
            if(data.isOTHER_DEBUG_MODE()) {
                System.out.print(title);
            }
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                String row =
                        String.format(
                                "\tcell %s:\tP = %.7f\tV=%.7f\tdelta=%.7f\tH=%.7f\n",
                                j,
                                cells[ringNumber][j].getPressure(),
                                cells[ringNumber][j].getVolume(),
                                cells[ringNumber][j].getDelta(),
                                cells[ringNumber][j].getH()
                        );
                if(data.isOTHER_LOG_TO_FILE()) {
                    logFile.write(row);
                }
                if(data.isOTHER_DEBUG_MODE()) {
                    System.out.print(row);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
