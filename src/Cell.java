import utils.Data;

public class Cell {
    public Point TOP_LEFT;
    public Point TOP_RIGHT;
    public Point DOWN_LEFT;
    public Point DOWN_RIGHT;

    public Double LEFT_DELTA;
    public Double RIGHT_DELTA;

    private Integer number;

    public Cell(Data initData, int number) {
        this.number = number;

        /*this.TOP_LEFT = new Point(
                initData.get_2D_L() * ((double)-1/2 + (double)number/initData.get_2D_N()),
                initData.get_2D_H()/2
        );
        this.TOP_RIGHT = new Point(
                initData.get_2D_L() * ((double)-1/2 + (double)(number + 1)/initData.get_2D_N()),
                initData.get_2D_H()/2
        );
        this.DOWN_LEFT = new Point(
                initData.get_2D_L() * ((double)-1/2 + (double)number/initData.get_2D_N()),
                -initData.get_2D_H()/2
        );
        this.DOWN_RIGHT = new Point(
                initData.get_2D_L() * ((double)-1/2 + (double)(number + 1)/initData.get_2D_N()),
                -initData.get_2D_H()/2
        );
        this.LEFT_DELTA = 0.0;
        this.RIGHT_DELTA = 0.0;*/
    }

    public Integer get_2D_Number() {
        return number;
    }

    @Override
    public String toString() {
        return "Cell " + number + "\n" +
                "  TOP_LEFT=" + TOP_LEFT +
                "  TOP_RIGHT=" + TOP_RIGHT + "\n" +
                "  DOWN_LEFT=" + DOWN_LEFT +
                "  DOWN_RIGHT=" + DOWN_RIGHT;
    }
}
