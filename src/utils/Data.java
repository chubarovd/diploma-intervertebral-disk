package utils;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

public class Data {
    // disk
    private Double _DISK_DELTA;
    private Double _DISK_R;
    private Double _DISK_H;
    private Double _DISK_P1;
    private Double _DISK_P2;
    private Integer _DISK_RINGS;
    private Integer _DISK_CELLS;

    // iqf
    private Integer _IQF_N;

    // gauss
    private Double _GAUSS_EPS;

    private Properties source;

    public Data(String propsPath) {
        try {
            source = loadProperties(propsPath);
        } catch(IOException e) {
            e.printStackTrace();
            exit(0);
        }

        _DISK_DELTA = Double.valueOf(source.getProperty("disk.delta"));
        _DISK_R = Double.valueOf(source.getProperty("disk.r"));
        _DISK_P1 = Double.valueOf(source.getProperty("disk.p1"));
        _DISK_P2 = Double.valueOf(source.getProperty("disk.p2"));
        _DISK_H = Double.valueOf(source.getProperty("disk.h"));

        _IQF_N = Integer.valueOf(source.getProperty("iqf.n"));

        _GAUSS_EPS = Double.valueOf(source.getProperty("gauss.eps"));
    }

    private static Properties loadProperties(String propsPath) throws IOException {
        Properties prop = new Properties();
        prop.load(Data.class.getClassLoader().getResourceAsStream(propsPath));

        return prop;
    }
    // disk

    public Double get_DISK_DELTA() {
        return _DISK_DELTA;
    }

    public Double get_DISK_R() {
        return _DISK_R;
    }

    public Double get_DISK_P1() {
        return _DISK_P1;
    }

    public Double get_DISK_P2() {
        return _DISK_P2;
    }

    public Double get_DISK_H() {
        return _DISK_H;
    }

    // iqf

    public Integer get_IQF_N() {
        return _IQF_N;
    }

    // gauss

    public Double get_GAUSS_EPS() {
        return _GAUSS_EPS;
    }
}
