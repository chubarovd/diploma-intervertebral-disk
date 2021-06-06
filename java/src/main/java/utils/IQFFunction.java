package utils;

public interface IQFFunction {
    public Boolean useDefaultP = true;

    /**
     *  Весовая функция
     * @param x
     * @return p(x)
     */
    public Double p(double x);

    /**
     * Гладкая функция
     * @param x
     * @return f(x)
     */
    public Double f(double x);
}
