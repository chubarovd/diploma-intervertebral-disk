import utils.Data;
import utils.IQF;

public class Application {
    public static void main(String[] args) {
        Data data = new Data("data.properties");
        Formulas formulas = new Formulas(data);
        Disk disk = new Disk(formulas);

        //System.out.println(iqf.build(20).compute());
    }
}
