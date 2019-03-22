public class Disk {
    private final Formulas formulas;

    private Double alpha;

    public Disk(Formulas formulas) {
        this.formulas = formulas;
        alpha = 0.;
    }

    public void rotate(double alpha) {
        formulas.setCurrentAlpha(alpha);
    }


}
