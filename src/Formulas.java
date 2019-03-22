import utils.Data;
import utils.IQF;
import utils.IQFFunction;

public class Formulas implements IQFFunction {
    private Double currentR;
    private Double currentAlpha;
    private Double currentZ;

    private final Data data;
    private final IQF iqf;

    public Formulas(Data data) {
        this.data = data;
        iqf = new IQF(this);

        currentR = data.get_DISK_R();
        currentAlpha = 0.;
        currentZ = data.get_DISK_H() / 2;
    }

    public Double cellVol(double r_1, double r_2, double fi_1, double fi_2) {
        return
                (2. / 3.) * Math.tan(currentAlpha) * (Math.pow(r_2, 3) - Math.pow(r_1, 3)) * (Math.sin(fi_2) - Math.sin(fi_1))
                +
                (data.get_DISK_H() / 2.) * (Math.pow(r_2, 2) - Math.pow(r_1, 2)) * (fi_2 - fi_1);
    }

    public Double boundVol(double fi_1, double fi_2) {
        return iqf.build(fi_1, fi_2, data.get_IQF_N(), data.get_GAUSS_EPS()).compute();
    }

    public Double delta(double fi) {
        return -data.get_DISK_P1() * Math.cos(fi) * Math.sin (currentAlpha) * (currentR / data.get_DISK_R());
    }

    @Override
    public Double p(double x) {
        return 1.;
    }

    @Override
    public Double f(double x) {
        double fi = x / data.get_DISK_R();
        //System.out.printf("  delta(" + fi + ") = %.10f\n", delta(fi));
        /*return
                (
                        data.get_DISK_DELTA() + delta(x)
                ) * (
                        Math.sin(
                                Math.PI / 2 *
                                        currentZ / (currentR * Math.cos(x) * Math.tan(currentAlpha) + data.get_DISK_H() / 2.)
                        ) -
                        Math.sin(
                                Math.PI / 2 *
                                        (-currentZ) / (currentR * Math.cos(x) * Math.tan(currentAlpha) + data.get_DISK_H() / 2.)
                        )
                ) * (
                        2. * (currentR * Math.cos(x) * Math.tan(currentAlpha) + data.get_DISK_H() / 2.) / Math.PI
                );*/
        /*return
                (2. / 3.) * Math.cos(x) * Math.tan(currentAlpha) * (Math.pow(cosinusoida(x, currentZ), 3) - Math.pow(currentR, 3))
                +
                (1. / 2.) * data.get_DISK_H() * (Math.pow(cosinusoida(x, currentZ), 2) - Math.pow(currentR, 2));*/
        return
                //currentR +
                        2. * (data.get_DISK_DELTA() + (currentR * data.get_DISK_P1() * Math.sin(currentAlpha) * Math.cos(fi)) / data.get_DISK_R())
                                * (2 * currentR * Math.cos(fi) * Math.tan(currentAlpha) + data.get_DISK_H()) / Math.PI;
    }

    public Double getCurrentR() {
        return currentR;
    }

    public void setCurrentR(Double currentR) {
        this.currentR = currentR;
    }

    public Double getCurrentAlpha() {
        return currentAlpha;
    }

    public void setCurrentAlpha(Double currentAlpha) {
        this.currentAlpha = currentAlpha;
    }

    public Double getCurrentZ() {
        return currentZ;
    }

    public void setCurrentZ(Double currentZ) {
        this.currentZ = currentZ;
    }
}
