package edu.washington.cs.knowitall.extractor.mapper;

/**
 * A class for taking a stream of <code>T</code> objects and modifying it
 * somehow (e.g. by filtering or modifying some objects). A <code>Mapper</code>
 * object has two states: enabled and disabled. If the <code>Mapper</code> is
 * disabled, it will return the stream of objects unmodified.
 *
 * Subclasses extending <code>Mapper</code> should implement the
 * <code>doMap(Iterable<T> objects)</code> method.
 *
 * @author afader
 *
 * @param <T>
 */
public abstract class Mapper<T> {

    private boolean enabled = true;

    /**
     * @param objects
     *            a stream of objects
     * @return a modified stream of objects
     */
    protected abstract Iterable<T> doMap(Iterable<T> objects);

    /**
     * @param objects
     *            a stream of objects
     * @return a modified stream of objects if the mapper is enabled, the
     *         unmodified input stream otherwise.
     */
    public Iterable<T> map(Iterable<T> objects) {
        if (isEnabled()) {
            return doMap(objects);
        } else {
            return objects;
        }
    }

    /**
     * @return <code>true</code> if this mapper is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Disables this mapper.
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Enables this mapper.
     */
    public void enable() {
        enabled = true;
    }

}
