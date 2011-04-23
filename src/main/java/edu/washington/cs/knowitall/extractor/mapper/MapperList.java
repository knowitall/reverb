package edu.washington.cs.knowitall.extractor.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of <code>Mapper<T></code> objects. Applies each mapper in the list to an input
 * stream of <code>T</code> objects in order.
 * @author afader
 *
 * @param <T>
 */
public class MapperList<T> extends Mapper<T> {

    private List<Mapper<T>> mappers;

    /**
     * Constructs an empty list of mappers.
     */
    public MapperList() {
        mappers = new ArrayList<Mapper<T>>();
        return;
    }

    /**
     * Constructs a new list of mappers from the given list.
     * @param mappers another mapper list.
     */
    public MapperList(List<Mapper<T>> mappers) {
        for (Mapper<T> mapper : mappers) {
            this.mappers.add(mapper);
        }
        return;
    }

    /**
     * @return the <code>Mapper<T></code> objects in this list.
     */
    public Iterable<Mapper<T>> getMappers() {
        return mappers;
    }

    /**
     * Adds a mapper to the end of the list. This mapper will be the last one
     * to be applied to the input stream of objects.
     * @param mapper
     */
    public void addMapper(Mapper<T> mapper) {
        mappers.add(mapper);
    }

    @Override
    /**
     * Applies each mapper in order to the input stream of objects.
     */
    protected Iterable<T> doMap(Iterable<T> objects) {
        for (Mapper<T> mapper : this.mappers) {
            objects = mapper.map(objects);
        }
        return objects;
    }

}
