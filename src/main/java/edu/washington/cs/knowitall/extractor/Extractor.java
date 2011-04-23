package edu.washington.cs.knowitall.extractor;

import edu.washington.cs.knowitall.extractor.mapper.Mapper;
import edu.washington.cs.knowitall.extractor.mapper.MapperList;

/**
 * <p>
 * An abstract class that defines the basic behavior of an extractor. An 
 * {@code Extractor<S,T>} object extracts objects of type {@code T} from a 
 * source object of type {@code S}. Candidate extractions are first obtained by 
 * calling the {@link Extractor#extractCandidates(Object)} method, which returns 
 * an {@link Iterable} object over extractions of type {@code T}. These 
 * extractions are passed through a list of {@link Mapper}{@code <T>} objects, 
 * each of which can filter or modify the extractions.
 * </p>
 * <p> 
 * Other objects can use an {@code Extractor<S,T>} object by calling the 
 * {@link Extractor#extract(Object)}extract(S source) object, which returns an 
 * {@link Iterable} object of extractions after the {@link Mapper}s have
 * been applied. 
 * </p>
 * <p>
 * {@link Mapper} objects can be added to the list of {@link Mapper}s by calling 
 * the {@link Extractor#addMapper(Mapper)} method. This will add a mapper to 
 * the end of the list (i.e. it is the last one to be applied to the 
 * extractions).
 * </p>
 * <p>
 * Subclasses extending {@code Extractor<S,T>} must implement the abstract 
 * {@link Extractor#extractCandidates(Object)} method. 
 * </p>
 * <p>
 * As an example, this class can be used to implement a class for extracting 
 * String sentences from a String block of text. {@link Mapper} objects can be 
 * added to filter the sentences by length, or remove brackets from the 
 * sentences.
 * </p>
 * @author afader
 *
 * @param <S> the source type
 * @param <T> the target extraction type
 */
public abstract class Extractor<S, T> {

    private MapperList<T> mappers;

    /**
     * Constructs a new extractor with no mappers.
     */
    public Extractor() {
        mappers = new MapperList<T>();
    }

    /**
     * @return the list of mappers attached to this extractor.
     */
    public MapperList<T> getMappers() {
        return mappers;
    }

    /**
     * Adds a mapper to the end of the list of mappers. It will be the new 
     * final mapper object applied to the extractions, after the existing 
     * mappers have been applied.
     * @param mapper the mapper to add.
     */
    public void addMapper(Mapper<T> mapper) {
        mappers.addMapper(mapper);
    }

    /**
     * Extracts candidate extractions from the given source object. When the 
     * user calls {@link Extractor#extract(Object)}, the this method is
     * used to generate a set of candidate extractions, which are then passed 
     * through each mapper object attached to the extractor. 
     * @param source the source to extract from.
     * @return an iterable object over the candidate extractions.
     * @throws ExtractorException if unable to extract
     */
    protected abstract Iterable<T> extractCandidates(S source) 
    	throws ExtractorException;

    /**
     * @param source the source object to extract from.
     * @return an iterable object over the candidate extractions.
     * @throws ExtractorException if unable to extract
     */
    public Iterable<T> extract(S source) throws ExtractorException {
        Iterable<T> candidates = extractCandidates(source);
        return mappers.map(candidates);
    }

    /**
     * Composes a {@code R->S} extractor with a {@code S->T} extractor to create
     * a {@code R->T} extractor. 
     * @param <R>
     * @param <S>
     * @param <T>
     * @param rsExtractor
     * @param stExtractor
     * @return an extractor taking objects of type {@code R} and returning 
     * objects of type {@code T}
     */
    public static <R,S,T> Extractor<R,T> compose(Extractor<R,S> rsExtractor, 
    		Extractor<S,T> stExtractor) {
        return new ExtractorComposition<R,S,T>(rsExtractor, stExtractor);
    }
}
