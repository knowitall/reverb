package edu.washington.cs.knowitall.sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * <p>
 * This class represents a table mapping tuples of strings to integer values. It
 * is used by {@link LayeredTokenPattern} for matching patterns against
 * {@link LayeredSequence} objects.
 * </p>
 * <p>
 * The core of this class is a mapping from string tuples of length {@code n} to
 * integers {@code 0 <= i <} {@link Encoder#MAX_SIZE}. The mapping is defined by
 * a list of {@code n} sets of String symbols {@code S_1, ..., S_n}, and a
 * special symbol {@link Encoder#UNK}. The mapping assigns an integer value to
 * each tuple {@code (x_1, ..., x_n)}, where {@code x_i} is either in
 * {@code S_i} or is the symbol {@code UNK}. For example, if {@code n = 2} and
 * {@code S_1 = S_2 = {0,1}}, then a possible mapping would be
 * {@code (0,0) => 0, (0,1) => 1,
 * (0, UNK) => 2, (1,0) => 3, (1,1) => 4, (1,UNK) => 5, (UNK,0) => 6,
 * (UNK,1) => 7, (UNK,UNK) => 8}.
 * </p>
 * <p>
 * Given a String tuple {@code (x_1, ..., x_n)}, it is mapped to an integer
 * value as follows. First, it is mapped to an intermediate tuple
 * {@code (y_1, ..., y_n)}, where {@code y_i = x_i} if {@code x_i} is in
 * {@code S_i}, otherwise {@code y_i = UNK}. Then the value of
 * {@code (y_1, ..., y_n)} according to the mapping is returned. This procedure
 * is implemented in the method {@link Encoder#encode(String[])}, which
 * represents tuples as String arrays.
 * </p>
 * <p>
 * There is no guarantee on the actual integer values assigned to each tuple.
 * The mapping cannot be larger than 2^16. This means that the product
 * {@code (|S_1|+1) * (|S_2|+1) * ... * (|S_n| + 1)} must be less than or equal
 * to 2^16.
 * </p>
 * 
 * @author afader
 * 
 */
public class Encoder {

    /**
     * The maximum encoding size.
     */
    public static final int MAX_SIZE = 0xFFFF;

    /**
     * The "unknown" symbol.
     */
    public static final String UNK = "<UNK>";

    // The alphabets for each position in the tuple (including UNK)
    private List<Set<String>> alphabets;

    // The mapping from tuples (String arrays, wrapped in a class that makes
    // them hashable) to integers.
    private HashMap<StringArrayWrapper, Integer> encodingTable;

    /**
     * Constructs a new encoding table using the given symbol sets. These symbol
     * sets should not contain the unknown symbol {@link Encoder#UNK}.
     * 
     * @param symbols
     * @throws SequenceException
     *             if the symbol sets result in an encoding table larger than
     *             {@link Encoder#MAX_SIZE}.
     */
    public Encoder(List<Set<String>> symbols) throws SequenceException {

        alphabets = new ArrayList<Set<String>>(symbols.size());

        int encodingTableSize = 1;

        // Create the alphabets, checking to make sure that UNK does not appear
        // in the given symbols, and that the encoding table is not too large.
        for (int i = 0; i < symbols.size(); i++) {
            Set<String> symbolSet = symbols.get(i);
            Set<String> alphabet = new HashSet<String>(symbolSet.size() + 1);
            alphabet.add(UNK);
            for (String token : symbolSet) {
                if (!token.equals(UNK)) {
                    alphabet.add(token);
                } else {
                    String msg = String.format(
                            "Cannot create encoding table: symbol set %s contains "
                                    + "the \"unknown\" symbol %s", i, UNK);
                    throw new SequenceException(msg);
                }
            }
            alphabets.add(alphabet);
            encodingTableSize *= alphabet.size();
        }

        if (encodingTableSize > MAX_SIZE) {
            throw new SequenceException("Maximum size exceeded");
        }

        // Create the encoding table. Each tuple is represented as a wrapper
        // around String arrays, which allows them to be keys in a HashMap.
        encodingTable = new HashMap<StringArrayWrapper, Integer>(
                encodingTableSize);
        int i = 0;
        for (List<String> tupleAr : Sets.cartesianProduct(alphabets)) {
            StringArrayWrapper tuple = new StringArrayWrapper(
                    tupleAr.toArray(new String[0]));
            encodingTable.put(tuple, i);
            i++;
        }

    }

    /**
     * @return the tuple length of this encoding table
     */
    public int size() {
        return alphabets.size();
    }

    /**
     * @return the number of keys in this encoding table
     */
    public int tableSize() {
        return encodingTable.size();
    }

    /**
     * Encodes the given tuple (represented as a String array) to its integer
     * value, represented as a char.
     * 
     * @param tuple
     * @return the integer value of the array, represented as a char
     * @throws SequenceException
     *             if unable to encode the tuple
     */
    public char encode(String[] tuple) throws SequenceException {

        // The argument must have length == size()
        if (tuple.length != size()) {
            String msg = String.format(
                    "Invalid tuple size: expected %s, got %s", size(),
                    tuple.length);
            throw new SequenceException(msg);
        }

        // Check to make sure the given tuple doesn't contain the UNK value
        for (int i = 0; i < tuple.length; i++) {
            String val = tuple[i];
            if (val.equals(UNK)) {
                String tupleStr = "(" + Joiner.on(", ").join(tuple) + ")";
                String msg = String.format(
                        "Symbol at position %s in %s equals %s", i, tupleStr,
                        UNK);
                throw new SequenceException(msg);
            }
        }

        // Map any unknown values to UNK and proceed with encoding
        return encodeMapped(mapToUnkown(tuple));
    }

    /**
     * Encodes the given tuple as a char. This method assumes that the tuple has
     * already had the unknown symbols mapped to UNK.
     * 
     * @param tuple
     * @return the encoding
     * @throws SequenceException
     *             if unable to encode
     */
    private char encodeMapped(String[] tuple) throws SequenceException {

        StringArrayWrapper wrappedTup = new StringArrayWrapper(tuple);

        if (encodingTable.containsKey(wrappedTup)) {
            char[] enc = Character.toChars(encodingTable.get(wrappedTup));
            assert enc.length == 1;
            return enc[0];
        } else {
            // Something is wrong: even after replacing unknown symbols
            // with UNK, we still cannot find the correct encoding.
            String tupStr = "(" + Joiner.on(", ").join(tuple) + ")";
            System.out.println("---------------");
            throw new SequenceException("Could not get encoding for: " + tupStr);
        }

    }

    /**
     * Encodes a "class" of tuples that all have the symbol value in the given
     * layer index. Using the example from the class description, if the
     * encoding table contains the mappings {@code (0,0) => 0, (0,1) => 1,
     * (0,UNK) => 2, ...}, then calling this method with {@code layerIndex = 0}
     * and {@code value = 1} will return the encodings of {@code (1,0), (1,1),}
     * and {@code (1,UNK)} as an array.
     * 
     * @param index
     *            the position in the tuple (defined by the order of sets passed
     *            to the constructor)
     * @param value
     * @return the encoding as an array
     * @throws SequenceException
     *             if the index is out of bounds, or if any of the resulting
     *             tuples cannot be encoded
     */
    public char[] encodeClass(int index, String value) throws SequenceException {

        // Make sure that the given index is not too big/small
        if (index < 0 || index >= size()) {
            String msg = String.format(
                    "Cannot get encoding class with index = %s and value = '%s': "
                            + "index out of bounds", index, value);
            throw new SequenceException(msg);
        }

        // Find all tuples in the table that equal value at index, encode them
        // as a char, then add them to the list. Then convert it to an array
        // and return it.
        List<Character> result = new ArrayList<Character>();
        for (StringArrayWrapper tuple : encodingTable.keySet()) {
            String[] tupData = tuple.getData();
            if (tupData[index].equals(value)) {
                result.add(encodeMapped(tupData));
            }
        }
        char[] resultA = new char[result.size()];
        for (int i = 0; i < result.size(); i++) {
            resultA[i] = result.get(i);
        }
        return resultA;
    }

    /**
     * Maps the given tuple to an intermediate representation, where any symbols
     * that did not appear in the sets provided to the constructor to the
     * {@link Encoder#UNK} symbol.
     * 
     * @param tuple
     * @return the intermediate tuple
     * @throws SequenceException
     *             if any of the symbols equal {@link Encoder#UNK}
     */
    private String[] mapToUnkown(String[] tuple) throws SequenceException {

        String[] result = new String[tuple.length];
        for (int i = 0; i < tuple.length; i++) {

            Set<String> knowns = alphabets.get(i);

            // The tuple is malformed if it contains UNK
            if (tuple[i].equals(UNK)) {

            } else if (knowns.contains(tuple[i])) {
                result[i] = tuple[i];
            } else {
                result[i] = UNK;
            }
        }

        return result;
    }

}
