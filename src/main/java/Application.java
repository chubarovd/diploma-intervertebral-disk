import model.DiskModel;
import utils.Data;

public class Application {
    public static void main(String[] args) {
        Data data = new Data("data.properties");
        DiskModel model = new DiskModel(data);

        model.init(0.1, 0.05, 0.1);
        model.rotateAndCalculate((Math.PI / 360) * 10);
        model.beginDiffusion(10);
    }
}
