package utils;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

public class Data {
    // disk
    @Getter
    private Double _DISK_P;

    @Getter
    private Double _DISK_R;

    @Getter
    private Double _DISK_H;

    @Getter
    private Integer _DISK_RINGS;

    @Getter
    private Integer _DISK_CELLS;

    // iqf
    @Getter
    private Integer _IQF_N;

    // gauss
    @Getter
    private Double _GAUSS_EPS;

    // other
    @Getter
    private Double _OTHER_ATMP;

    @Getter
    private Double _OTHER_EPS;

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
