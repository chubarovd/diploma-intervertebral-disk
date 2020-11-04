package model;

/**
 * Represents dynamic intervertebral disk model.
 */
public interface DynamicDiskModel {
    /**
     * Init model.
     */
    void init() throws Exception;

    /**
     * Begin rotation and calculation.
     */
    void begin() throws Exception;
}
