package utils;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

public class Data {
    // region disk
    @Getter
    private final double _DISK_P;

    @Getter
    private final double _DISK_R;

    @Getter
    private final double _DISK_H;

    @Getter
    private final int _DISK_RINGS;

    @Getter
    private final int _DISK_CELLS;
    //endregion

    // region iqf
    @Getter
    private final int _IQF_N;
    // endregion

    // region gauss
    @Getter
    private final double _GAUSS_EPS;

    // endregion

    // region other
    @Getter
    private final double _OTHER_ATMP;

    @Getter
    private final double _OTHER_EPS;

    @Getter
    private final String _OTHER_LOG_FILE_NAME;

    @Getter
    private final String _OTHER_P_LOG_FILE_NAME;

    @Getter
    private final boolean _OTHER_DEBUG_MODE;

    @Getter
    private final boolean _OTHER_LOG_TO_FILE;
    // endregion

    private Properties source;

    public Data(String propsPath) {
        try {
            source = loadProperties(propsPath);
        } catch(IOException e) {
            e.printStackTrace();
            exit(0);
        }

        _DISK_P = Double.valueOf(source.getProperty("disk.p"));
        _DISK_R = Double.valueOf(source.getProperty("disk.r"));
        _DISK_H = Double.valueOf(source.getProperty("disk.h"));
        _DISK_RINGS = Integer.valueOf(source.getProperty("disk.rings"));
        _DISK_CELLS = Integer.valueOf(source.getProperty("disk.cells"));

        _IQF_N = Integer.valueOf(source.getProperty("iqf.n"));

        _GAUSS_EPS = Double.valueOf(source.getProperty("gauss.eps"));

        _OTHER_ATMP = Double.valueOf(source.getProperty("other.atmp"));
        _OTHER_EPS = Double.valueOf(source.getProperty("other.eps"));
        _OTHER_LOG_FILE_NAME = source.getProperty("other.log-file-name");
        _OTHER_P_LOG_FILE_NAME = source.getProperty("other.p-log-file-name");
        _OTHER_DEBUG_MODE = Boolean.valueOf(source.getProperty("other.debug-mode"));
        _OTHER_LOG_TO_FILE = Boolean.valueOf(source.getProperty("other.log-to-file"));
    }

    private static Properties loadProperties(String propsPath) throws IOException {
        Properties prop = new Properties();
        prop.load(Data.class.getClassLoader().getResourceAsStream(propsPath));

        return prop;
    }
}
