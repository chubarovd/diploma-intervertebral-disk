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
        double alphaStepGrad = alphaLimitGrad / 60;
        double rpd = (Math.PI / 360);

        DynamicDiskModel model = new DynamicDiskModelImpl(data, calcMethod, rpd * alphaStepGrad, rpd * alphaLimitGrad);
//        DynamicDiskModel model = new DynamicDiskModelImpl(data, calcMethod, rpd * alphaStepGrad, rpd * alphaLimitGrad, 120000);
//        DynamicDiskModel model = new DynamicDiskModelImpl(data, calcMethod, rpd * alphaStepGrad, rpd * alphaLimitGrad, 10000);

        model.init();
        model.begin();
    }
}