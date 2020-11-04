import methods.CellCalcMethods;
import methods.impl.CellCalcMethodsV2;
import model.DynamicDiskModel;
import model.impl.DynamicDiskModelImpl;
import utils.Data;

public class Application {
    public static void main(String[] args) throws Exception {
        Data data = new Data("data.properties");
        CellCalcMethods calcMethod = new CellCalcMethodsV2(data.getDISK_H());

        double alphaLimitGrad = 10;
        double alphaStepGrad = 10;//0.1;

        DynamicDiskModel model = new DynamicDiskModelImpl(
                data,
                calcMethod,
                (Math.PI / 360) * alphaStepGrad,
                (Math.PI / 360) * alphaLimitGrad);
//        DynamicDiskModel model = new DynamicDiskModelImpl(data, calcMethod, Math.PI / 360, Math.PI / 36, 1, 10);
//        DynamicDiskModel model = new DynamicDiskModelImpl(data, calcMethod, Math.PI / 36, Math.PI / 36, 1, 20);
        model.init();
        model.begin();
    }
}