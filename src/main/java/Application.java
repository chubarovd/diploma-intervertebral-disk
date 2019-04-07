import model.DiskModel;
import utils.Data;

public class Application {
    public static void main(String[] args) {
        Data data = new Data("data.properties");
        DiskModel model = new DiskModel(data);

        model.init(1, 1, 1);
        model.rotateAndCalculate(Math.PI / 18);
        model.beginDiffusion(1);
        //System.out.println(iqf.build(20).compute());
    }
}
