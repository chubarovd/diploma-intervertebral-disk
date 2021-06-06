package model;

/**
 * Represents static intervertebral disk model.
 */
public interface StaticDiskModel {
    /**
     * Init model.
     */
    void init();

    /**
     * Perform rotation and calculate new cells' values.
     */
    void rotateAndCalc(double alpha);

    /**
     * Begin diffusion until equilibrium is established.
     */
    void beginDiffusion(double deltaTime);

    /**
     * Begin diffusion until steps limit is reached.
     */
    void beginDiffusion(double deltaTime, int stepsLimit);
}
