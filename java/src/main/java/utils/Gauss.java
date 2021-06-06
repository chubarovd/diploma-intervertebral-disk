package utils;

public final class Gauss {
    private Gauss() {
    }

    public static Double[] compute(int n, double eps, Double[][] matrix, Double[] freeVector) {
        Double[] res = new Double[n];
        Double[][] M = matrix;
        Double[] b = freeVector;
        double max;
        int k = 0, index;

        while(k < n) {
            // Поиск строки с наибольшим ведущим элементом
            max = Math.abs(M[k][k]);
            index = k;
            for(int i = k + 1; i < n; i++) {
                if(Math.abs(M[i][k]) > max) {
                    max = Math.abs(M[i][k]);
                    index = i;
                }
            }
            if(max < eps) {
                System.out.println("Система не имеет решения, так как ее матрица содержит нулевой столбец (номер " + index + " )");
                return null;
            }
            // Перестановка строк
            double temp;
            for(int j = 0; j < n; j++) {
                temp = M[k][j];
                M[k][j] = M[index][j];
                M[index][j] = temp;
            }
            temp = b[k];
            b[k] = b[index];
            b[index] = temp;

            // Нормализация уравнений
            for(int i = k; i < n; i++) {
                temp = M[i][k];
                if(Math.abs(temp) < eps) continue; // пропустить для нулевого коэффициента
                for(int j = 0; j < n; j++) M[i][j] = M[i][j] / temp;
                b[i] = b[i] / temp;

                if(i == k) continue; // уравнение не вычитать само из себя
                for(int j = 0; j < n; j++) M[i][j] = M[i][j] - M[k][j];
                b[i] = b[i] - b[k];
            }
            k++;
        }

        // обратная подстановка
        for(k = n - 1; k >= 0; k--) {
            res[k] = b[k];
            for(int i = 0; i < k; i++)
                b[i] = b[i] - M[i][k] * res[k];
        }

        return res;
    }
}
