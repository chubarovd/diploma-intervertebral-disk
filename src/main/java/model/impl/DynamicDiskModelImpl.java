package model.impl;

import lombok.extern.slf4j.Slf4j;
import methods.CellCalcMethods;
import model.Cell;
import model.DynamicDiskModel;
import utils.Data;
import utils.Timer;
import utils.TimerLimitException;

import java.io.FileWriter;
import java.io.IOException;

import static java.lang.Math.PI;
import static methods.impl.CellCalcMethodsV2.anyCheckValue;

@Slf4j
public class DynamicDiskModelImpl implements DynamicDiskModel {

    private final CellCalcMethods cm;
    private final Data data;
    private final Cell[][] cells;
    private final Timer timer;
    private final double deltaAlpha;
    private final double limitAlpha;

    private boolean initialized = false;
    private double fiStep;
    private double rStep;
    private double alpha = 0;

    private FileWriter PRESSURES_LOG_FILE;
    private FileWriter DELTA_LOG_FILE;
    private FileWriter VOLUME_LOG_FILE;

    public DynamicDiskModelImpl(Data data, CellCalcMethods cellCalcMethods, double deltaAlphaPerSec, double limitAlpha) {
        this.data = data;
        this.cells = new Cell[this.data.getDISK_RINGS()][this.data.getDISK_CELLS()];
        this.cm = cellCalcMethods;
        this.deltaAlpha = deltaAlphaPerSec;
        this.limitAlpha = limitAlpha;
        this.timer = new Timer();

        this.fiStep = (2 * PI) / data.getDISK_CELLS();
        this.rStep = data.getDISK_R() / data.getDISK_RINGS();
    }

    public DynamicDiskModelImpl(Data data, CellCalcMethods cellCalcMethods, double deltaAlphaPerSec, double limitAlpha, int timeLimit) {
        this.data = data;
        this.cm = cellCalcMethods;
        this.deltaAlpha = deltaAlphaPerSec;
        this.limitAlpha = limitAlpha;
        this.timer = new Timer(timeLimit);

        this.fiStep = (2 * PI) / data.getDISK_CELLS();
        this.rStep = data.getDISK_R() / data.getDISK_RINGS();

        this.cells = new Cell[this.data.getDISK_RINGS()][];
        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            this.cells[i] = new Cell[this.data.getDISK_CELLS()];
        }
    }

    @Override
    public void init() {
        try {
            PRESSURES_LOG_FILE = new FileWriter(data.getOTHER_PRESSURES_LOG_FILE_NAME());
            DELTA_LOG_FILE = new FileWriter(data.getOTHER_DELTA_LOG_FILE_NAME());
            VOLUME_LOG_FILE = new FileWriter(data.getOTHER_VOLUME_LOG_FILE_NAME());
        } catch(IOException ignore) {}

        double sum = 0;
        // init cells
        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                cells[i][j] = new Cell(
                        rStep * (i + 1),
                        fiStep * j,
                        fiStep,
                        (i == 0 ? null : cells[i - 1][j]),
                        data.getDISK_H(),
                        data.getDISK_P() / data.getDISK_K(),
                        data.getDISK_P(),
                        0 // начальный объем рассчитаем после
                );
                cells[i][j].setVolume(cm.initial(cells[i][j]));
                sum += cells[i][j].getVolume();
            }
        }

        //log.info("Volumes: ");
        //print("v");
        //System.out.println(sum);
        initialized = true;
    }

    @Override
    public void begin() throws Exception {
        if(!initialized) {
            log.error("Could not begin process. Model is not initialized.");
            return;
        }
        isBalanced();
        update();
        while(!isBalanced()) {
            try {
                update();
            } catch(TimerLimitException e) {
                log.warn("Time limit is reached. Process stopped.");
                break;
            }
        }
        PRESSURES_LOG_FILE.close();
        DELTA_LOG_FILE.close();
        VOLUME_LOG_FILE.close();
        //log.info("Result pressures: ");
        //print("p");
    }

    private void update() throws TimerLimitException {
        Cell c1 = cells[0][0];
        Cell c2 = cells[0][1];
        Cell c3 = cells[0][2];
        Cell c4 = cells[0][3];
        String logTemplate = "%.5f | %.5f | %.5f | %.5f";
        System.out.println(String.format(logTemplate, c1.getVolume(), c2.getVolume(), c3.getVolume(), c4.getVolume()));

        if(alpha < limitAlpha) {
            alpha += deltaAlpha;
        }
        timer.tick();

        updateHeights();
        updateDeltasAndPressures();
        updateVolumes();
    }

    // расчет delta и давлений при неизменном объеме
    private void updateDeltasAndPressures() {
        double newDeltas[][] = new double[data.getDISK_RINGS()][data.getDISK_CELLS()];

        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                newDeltas[i][j] = cm.calcDelta(cells[i][j], alpha);
            }
        }

        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                cells[i][j].setDelta(newDeltas[i][j]);
                cells[i][j].setPressure(data.getDISK_K() * cells[i][j].getDelta());
            }
        }
    }

    // пересчет давлений
    private void updateVolumes() {
        double[][] volumeDeltas = new double[data.getDISK_RINGS()][data.getDISK_CELLS()];
        double d = data.getDISK_SIDE_DIFFUSION();
        double D = data.getDISK_RING_DIFFUSION();
        int iMax = data.getDISK_RINGS() - 1;
        int jMax = data.getDISK_CELLS() - 1;

        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                if (i > 1) {
                    if(data.getDISK_RINGS() == 0) {
                        volumeDeltas[i][j] += D * (cells[i + 1][j].getPressure() - cells[i][j].getPressure());
                    } else if(i == iMax) {
                        volumeDeltas[i][j] += D * (cells[i - 1][j].getPressure() - cells[i][j].getPressure());
                    } else {
                        volumeDeltas[i][j] += D * (cells[i - 1][j].getPressure() + cells[i + 1][j].getPressure() - 2 * cells[i][j].getPressure());
                    }
                }

                if(j == 0) {
                    volumeDeltas[i][j] += d * (cells[i][jMax].getPressure() + cells[i][j + 1].getPressure() - 2 * cells[i][j].getPressure());
                } else if(j == jMax) {
                    volumeDeltas[i][j] += d * (cells[i][j - 1].getPressure() + cells[i][0].getPressure() - 2 * cells[i][j].getPressure());
                } else {
                    volumeDeltas[i][j] += d * (cells[i][j + 1].getPressure() + cells[i][j - 1].getPressure() - 2 * cells[i][j].getPressure());
                }
            }
        }

        String logTemplate = "%.5f | %.5f | %.5f | %.5f";
        //System.out.println(String.format(logTemplate, volumeDeltas[0][0], volumeDeltas[0][1], volumeDeltas[0][2], volumeDeltas[0][3]));

        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                cells[i][j].setVolume(cells[i][j].getVolume() + volumeDeltas[i][j]);
            }
        }
    }

    private void updateHeights() {
        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                cells[i][j].setH(cm.calcHeight(cells[i][j], alpha));
            }
        }
    }

    // признак равновесия давлений (т.е. окончания процесса)
    private boolean isBalanced() {
        Cell c1 = cells[0][0];
        Cell c2 = cells[0][1];
        Cell c3 = cells[0][2];
        Cell c4 = cells[0][3];

//        Cell c1 = cells[1][0];
//        Cell c2 = cells[1][data.getDISK_CELLS() / 2];
//        Cell c3 = cells[data.getDISK_RINGS() - 1][data.getDISK_CELLS() / 4];
//        Cell c4 = cells[data.getDISK_RINGS() - 1][3 * data.getDISK_CELLS() / 4];

        try {
            String logTemplate = " %.10f  |  %.10f  |  %.10f  |  %.10f \n";
            PRESSURES_LOG_FILE.write(String.format(logTemplate, c1.getPressure(), c2.getPressure(), c3.getPressure(), c4.getPressure()));
            DELTA_LOG_FILE.write(String.format(logTemplate, anyCheckValue(c1, alpha), anyCheckValue(c2, alpha), anyCheckValue(c3, alpha), anyCheckValue(c4, alpha)));
//            DELTA_LOG_FILE.write(String.format(logTemplate, c1.getDelta(), c2.getDelta(), c3.getDelta(), c4.getDelta()));
            VOLUME_LOG_FILE.write(String.format(logTemplate, c1.getVolume(), c2.getVolume(), c3.getVolume(), c4.getVolume()));
        } catch(IOException e) {
            e.printStackTrace();
        }

        return Math.abs(c1.getPressure() - c2.getPressure()) < data.getOTHER_EPS() &&
                Math.abs(c1.getPressure() - c3.getPressure()) < data.getOTHER_EPS() &&
                Math.abs(c1.getPressure() - c4.getPressure()) < data.getOTHER_EPS() &&
                Math.abs(c2.getPressure() - c3.getPressure()) < data.getOTHER_EPS() &&
                Math.abs(c2.getPressure() - c4.getPressure()) < data.getOTHER_EPS() &&
                Math.abs(c3.getPressure() - c4.getPressure()) < data.getOTHER_EPS();
    }

    private void print() {
        Cell c1 = cells[0][0];
        Cell c2 = cells[0][1];
        Cell c3 = cells[0][2];
        Cell c4 = cells[0][3];

//        Cell c1 = cells[1][0];
//        Cell c2 = cells[1][data.getDISK_CELLS() / 2];
//        Cell c3 = cells[data.getDISK_RINGS() - 1][data.getDISK_CELLS() / 4];
//        Cell c4 = cells[data.getDISK_RINGS() - 1][3 * data.getDISK_CELLS() / 4];

        String logTemplate = " %.10f  |  %.10f  |  %.10f  |  %.10f \n";
        String.format(logTemplate, anyCheckValue(c1, alpha), anyCheckValue(c2, alpha), anyCheckValue(c3, alpha), anyCheckValue(c4, alpha));
    }

    private void print(String quantity) {
        for(int i = 0; i < data.getDISK_RINGS(); i++) {
            for(int j = 0; j < data.getDISK_CELLS(); j++) {
                switch(quantity) {
                    case "p":
                        System.out.print(cells[i][j].getPressure() + " ");
                        break;
                    case "d":
                        System.out.print(cells[i][j].getDelta() + " ");
                        break;
                    case "v":
                        System.out.print(cells[i][j].getVolume() + " ");
                        break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
