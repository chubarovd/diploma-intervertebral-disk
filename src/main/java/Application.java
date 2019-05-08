import model.DiskModel;
import utils.Data;

import java.awt.*;

public class Application {
    public static void main(String[] args) {
        Data data = new Data("data.properties");
        DiskModel model = new DiskModel(data);

        model.init(0.27, 0.1, 3.7);
        model.rotateAndCalculate((Math.PI / 360) * 10);
        //model.beginDiffusion(0.1, 3, false);
        model.beginDiffusion(0.01);
    }


}
