package utils;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

public class Data {
    // region disk
    @Getter
    private final double DISK_SIDE_DIFFUSION;

    @Getter
    private final double DISK_RING_DIFFUSION;

    @Getter
    private final double DISK_K;

    @Getter
    private final double DISK_P;

    @Getter
    private final double DISK_R;

    @Getter
    private final double DISK_H;

    @Getter
    private final int DISK_RINGS;

    @Getter
    private final int DISK_CELLS;
    //endregion

    // region iqf
    @Getter
    private final int IQF_N;
    // endregion

    // region gauss
    @Getter
    private final double GAUSS_EPS;

    // endregion

    // region other
    @Getter
    private final double OTHER_ATMP;

    @Getter
    private final double OTHER_EPS;

    @Getter
    private final String OTHER_LOG_FILE_NAME;

    @Getter
    private final String OTHER_PRESSURES_LOG_FILE_NAME;

    @Getter
    private final String OTHER_DELTA_LOG_FILE_NAME;

    @Getter
    private final String OTHER_VOLUME_LOG_FILE_NAME;

    @Getter
    private final boolean OTHER_DEBUG_MODE;

    @Getter
    private final boolean OTHER_LOG_TO_FILE;

    @Getter
    private final boolean OTHER_LOG_ALL;
    // endregion

    private Properties source;

    public Data(String propsPath) {
        try {
            source = loadProperties(propsPath);
        } catch(IOException e) {
            e.printStackTrace();
            exit(0);
        }

        DISK_SIDE_DIFFUSION = Double.valueOf(source.getProperty("disk.d"));
        DISK_RING_DIFFUSION = Double.valueOf(source.getProperty("disk.D"));
        DISK_K = Double.valueOf(source.getProperty("disk.K"));
        DISK_P = Double.valueOf(source.getProperty("disk.p"));
        DISK_R = Double.valueOf(source.getProperty("disk.r"));
        DISK_H = Double.valueOf(source.getProperty("disk.h"));
        DISK_RINGS = Integer.valueOf(source.getProperty("disk.rings"));
        DISK_CELLS = Integer.valueOf(source.getProperty("disk.cells"));

        IQF_N = Integer.valueOf(source.getProperty("iqf.n"));

        GAUSS_EPS = Double.valueOf(source.getProperty("gauss.eps"));

        OTHER_ATMP = Double.valueOf(source.getProperty("other.atmp"));
        OTHER_EPS = Double.valueOf(source.getProperty("other.eps"));
        OTHER_LOG_FILE_NAME = source.getProperty("other.log-file-name");
        OTHER_PRESSURES_LOG_FILE_NAME = source.getProperty("other.pressure-log-file-name");
        OTHER_DELTA_LOG_FILE_NAME = source.getProperty("other.delta-log-file-name");
        OTHER_VOLUME_LOG_FILE_NAME = source.getProperty("other.volume-log-file-name");
        OTHER_DEBUG_MODE = Boolean.valueOf(source.getProperty("other.debug-mode"));
        OTHER_LOG_TO_FILE = Boolean.valueOf(source.getProperty("other.log-to-file"));
        OTHER_LOG_ALL = Boolean.valueOf(source.getProperty("other.log-all"));
    }

    private static Properties loadProperties(String propsPath) throws IOException {
        Properties prop = new Properties();
        prop.load(Data.class.getClassLoader().getResourceAsStream(propsPath));

        return prop;
    }
}
