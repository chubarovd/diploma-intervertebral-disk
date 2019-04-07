package utils;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.exit;

@Getter
public class Data {
    // disk
    private Double _DISK_P;
    private Double _DISK_R;
    private Double _DISK_H;
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

        _DISK_P = Double.valueOf(source.getProperty("disk.p"));
        _DISK_R = Double.valueOf(source.getProperty("disk.r"));
        _DISK_H = Double.valueOf(source.getProperty("disk.h"));
        _DISK_RINGS = Integer.valueOf(source.getProperty("disk.rings"));
        _DISK_CELLS = Integer.valueOf(source.getProperty("disk.cells"));

        _IQF_N = Integer.valueOf(source.getProperty("iqf.n"));

        _GAUSS_EPS = Double.valueOf(source.getProperty("gauss.eps"));
    }

    private static Properties loadProperties(String propsPath) throws IOException {
        Properties prop = new Properties();
        prop.load(Data.class.getClassLoader().getResourceAsStream(propsPath));

        return prop;
    }

    // disk

    public Double get_DISK_P() {
        return _DISK_P;
    }

    public Double get_DISK_R() {
        return _DISK_R;
    }

    public Double get_DISK_H() {
        return _DISK_H;
    }

    public Integer get_DISK_RINGS() {
        return _DISK_RINGS;
    }

    public Integer get_DISK_CELLS() {
        return _DISK_CELLS;
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
