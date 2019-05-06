import model.DiskModel;
import utils.Data;

import java.io.FileNotFoundException;

public class Application {
    public static void main(String[] args) throws FileNotFoundException {
        /*int val = 3; //11
        int i = 2;

        System.out.println(((int)Math.pow(2, i - 1)) ^ val);*/

        Data data = new Data("data.properties");
        DiskModel model = new DiskModel(data);

        model.init(0.4, 0.1, 0.51);
        model.rotateAndCalculate((Math.PI / 360) * 10);
        model.beginDiffusion(0.1, 3);
    }
}
