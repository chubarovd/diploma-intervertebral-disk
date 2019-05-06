package utils;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

public class Data {
    // region disk
    @Getter
    private double _DISK_P;

    @Getter
    private double _DISK_R;

    @Getter
    private double _DISK_H;

    @Getter
    private int _DISK_RINGS;

    @Getter
    private int _DISK_CELLS;
    //endregion

    // region iqf
    @Getter
    private int _IQF_N;
    // endregion

    // region gauss
    @Getter
    private double _GAUSS_EPS;

    // endregion

    // region other
    @Getter
    private double _OTHER_ATMP;

    @Getter
    private double _OTHER_EPS;
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
    }

    private static Properties loadProperties(String propsPath) throws IOException {
        Properties prop = new Properties();
        prop.load(Data.class.getClassLoader().getResourceAsStream(propsPath));

        return prop;
    }
}
