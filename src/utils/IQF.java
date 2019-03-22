package utils;

public class IQF {
    private final IQFFunction functions;

    private Integer n;
    private Double[] multipler;
    private Double[] moment;
    private Double[] point;

    public IQF(IQFFunction functions) {
        this.functions = functions;
    }

    public IQF build(double leftBound, double rightBound, int n, double gaussEps) {
        this.n = n;

        // fill the points grid
        point = new Double[n];
        double step = (rightBound - leftBound) / (n - 1);
        for(int i = 0; i < n; i++) {
            point[i] = leftBound + step * i;
        }

        moment = new Double[n];
        if(functions.useDefaultP) { // p(x)=1
            for(int i = 0; i < n; i++) {
                moment[i] = (1.0 / (i + 1)) * (Math.pow(rightBound, i + 1) - Math.pow(leftBound, i + 1));
            }
        } else {
            // ...
        }

        Double[][] coefficient = new Double[n][];
        for(int s = 0; s < n; s++) {
            coefficient[s] = new Double[n];
            for(int i = 0; i < n; i++) {
                coefficient[s][i] = Math.pow(point[i], s);
            }
        }
        multipler = Gauss.compute(n, gaussEps, coefficient, moment);

        return this;
    }

    public Double compute() {
        double result = 0;
        for(int i = 0; i < n; i++) {
            //System.out.printf("f(" + point[i]+ ") = %.34f\n", functions.f(point[i]));
            //System.out.printf("\t\tA = %.34f\n", multipler[i]);
            result += multipler[i] * functions.f(point[i]);
        }

        return result;
    }
}
