package edu.washington.cs.knowitall.normalization;

// package ac.biu.nlp.graph.untyped.preprocessing.rep;

import java.util.Scanner;

/**
 * Represents a Reverb predicate that contains (1) the original predicate from
 * the text (2) normalized version (3) POS sequence (modified by Michael
 * Schmitz)
 * 
 * @author Jonathan Berant
 * @date 28/8/11
 * 
 */

public class RelationString implements Comparable<RelationString> {

    private String m_pred; // original predicate extracted by reverb
    private String m_normPred;
    private String m_posPred;

    public RelationString(String m_pred, String m_normPred, String m_posPred) {
        this.m_pred = m_pred;
        this.m_normPred = m_normPred;
        this.m_posPred = m_posPred;
    }

    public void setRelationString(RelationString other) {
        this.m_pred = other.m_pred;
        this.m_normPred = other.m_normPred;
        this.m_posPred = other.m_posPred;
    }

    private void clearPredicate() {
        m_pred = "";
        m_normPred = "";
        m_posPred = "";
    }

    public String getPred() {
        return m_pred;
    }

    public void setPred(String m_pred) {
        this.m_pred = m_pred;
    }

    public String getNormPred() {
        return m_normPred;
    }

    public void setNormPred(String m_normPred) {
        this.m_normPred = m_normPred;
    }

    public String getPosPred() {
        return m_posPred;
    }

    public void setPosPred(String m_posPred) {
        this.m_posPred = m_posPred;
    }

    @Override
    public String toString() {
        return m_pred + "\t" + m_normPred + "\t" + m_posPred;
    }

    @Override
    /**
     * hascode is determined by the original predicate alone
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_pred == null) ? 0 : m_pred.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelationString other = (RelationString) obj;
        if (m_pred == null) {
            if (other.m_pred != null)
                return false;
        } else if (!m_pred.equals(other.m_pred))
            return false;
        return true;
    }

    @Override
    public int compareTo(RelationString o) {
        return m_pred.compareTo(o.m_pred);
    }

    /**
     * Normalizes the predicate - takes predicate normalized by TextRunner and
     * applies rule based normalization:
     * <ol>
     * <li>if predicate doesn't start with a letter or a abbreviated aux -
     * return empty predicate</li>
     * <li>transform to lower case</li>
     * <li>omit adverbs (<i>RB</i> pos-tag)</li>
     * <li>omit quasi-modals: "used to VB", "ought to VB", "be able to VB",
     * "have to VB"</li>
     * <li>omit modals: (<i>MD</i> pos-tag)</li>
     * <li>handle "have": change "have VBN/VB/VBD" to "VBD"</li>
     * <li>handle "be": reverse TextRunner normalization of "be VBN" to
     * unnormalized form, omit "be" in "be VBG" unless is "be going to VB",
     * which is treated like a quasi-modal</li>
     * <li>handle "do": omit "do" in "do VB" expressions</li>
     * <li>handle "have" again to solve some extreme cases where it needs to be
     * applied twice (<i>'d have had to have been doing")</i></li>
     * <li>if what's left is "be" or "do" or "be be" or "do do" - return empty
     * predicate since this predicate is not informative</li>
     * <li>if what's left doesn't start with a letter or a abbreviated aux -
     * return empty predicate since the extraction was bad</li>
     * <li>if what's contains "be be " or "do do " or "have have " - return
     * empty predicate since the extraction was bad</li>
     * </ol>
     * 
     * @return
     * @throws IllegalArgumentException
     */
    public void correctNormalization() {

        if (m_pred.length() == 0)
            throw new IllegalArgumentException(
                    "pos-tagged predicate has zero length");

        if (!(Character.isLetter(m_pred.charAt(0)) || startsWithShortenedAux(m_pred))) {
            clearPredicate();
        } else {

            m_normPred = m_normPred.toLowerCase();
            // remember the original form of the VBNs
            // Map<String,String> vbnToVbMap = createVbnMap();

            // the order matters
            setRelationString(omitAdverbs());
            setRelationString(handleQuasiModals());
            setRelationString(omitModals());
            setRelationString(handleHave());
            // setRelationString(handleBeDo(vbnToVbMap));
            // setRelationString(handleHave());
            postProcess();

            if (m_normPred.equals("be") || m_normPred.equals("do"))
                clearPredicate();
        }

    }

    private void postProcess() {

        // if the predicate is not informative get rid of it
        if (m_normPred.equals("be") || m_normPred.equals("do")
                || m_normPred.equals("be be") || m_normPred.equals("do do"))
            clearPredicate();
        // if the predicate appears syntactically wrong after normalization rid
        // of it
        else if (m_normPred.contains("have have ")
                || m_normPred.contains("do do ")
                || m_normPred.contains("be be "))
            clearPredicate();
        else if (!(Character.isLetter(m_normPred.charAt(0)) || startsWithShortenedAux(m_normPred)))
            clearPredicate();
    }

    private RelationString handleHave() {

        String[] normTokens = m_normPred.split("\\s+");
        String[] posTokens = m_posPred.split("\\s+");

        StringBuilder normBuilder = new StringBuilder();
        StringBuilder posBuilder = new StringBuilder();

        for (int i = 0; i < posTokens.length; ++i) {
            if (normTokens[i].equals("have")
                    && i < (posTokens.length - 1)
                    && (posTokens[i + 1].equals("VBN")
                            || posTokens[i + 1].equals("VB") || posTokens[i + 1]
                            .equals("VBD"))) {
                // skip the have and change it to past
                normBuilder.append(normTokens[i + 1] + " ");
                posBuilder.append("VBD ");
                i++;
            } else {
                normBuilder.append(normTokens[i] + " ");
                posBuilder.append(posTokens[i] + " ");
            }
        }
        return new RelationString(m_pred, normBuilder.toString().trim(),
                posBuilder.toString().trim());
    }

    private RelationString handleQuasiModals() {

        String[] normTokens = m_normPred.split("\\s+");
        String[] posTokens = m_posPred.split("\\s+");

        StringBuilder normBuilder = new StringBuilder();
        StringBuilder posBuilder = new StringBuilder();

        for (int i = 0; i < posTokens.length; ++i) {

            if (i < posTokens.length - 2) {
                if (normTokens[i].equals("ought")
                        && normTokens[i + 1].equals("to")
                        && posTokens[i + 2].equals("VB")) {
                    i++;
                } else if (normTokens[i].equals("have")
                        && normTokens[i + 1].equals("to")
                        && posTokens[i + 2].equals("VB")) {
                    i++;
                } else if (normTokens[i].equals("use")
                        && normTokens[i + 1].equals("to")
                        && posTokens[i + 2].equals("VB")
                        && m_pred.contains("used")) {
                    i++;
                } else if ((i < posTokens.length - 3)
                        && normTokens[i].equals("be")
                        && normTokens[i + 1].equals("able")
                        && normTokens[i + 2].equals("to")
                        && posTokens[i + 3].equals("VB")) {
                    i += 2;
                } else {
                    normBuilder.append(normTokens[i] + " ");
                    posBuilder.append(posTokens[i] + " ");
                }
            } else {
                normBuilder.append(normTokens[i] + " ");
                posBuilder.append(posTokens[i] + " ");
            }
        }

        return new RelationString(m_pred, normBuilder.toString().trim(),
                posBuilder.toString().trim());
    }

    private RelationString omitAdverbs() {

        String[] normTokens = m_normPred.split("\\s+");
        String[] posTokens = m_posPred.split("\\s+");
        if (normTokens.length != posTokens.length)
            throw new IllegalArgumentException(
                    "different number of tokens in: " + m_normPred + " and "
                            + m_posPred);

        StringBuilder normBuilder = new StringBuilder();
        StringBuilder posBuilder = new StringBuilder();

        for (int i = 0; i < posTokens.length; ++i) {
            if (!posTokens[i].equals("RB")) {
                normBuilder.append(normTokens[i] + " ");
                posBuilder.append(posTokens[i] + " ");
            }
        }
        return new RelationString(m_pred, normBuilder.toString().trim(),
                posBuilder.toString().trim());
    }

    private RelationString omitModals() {

        String[] normTokens = m_normPred.split("\\s+");
        String[] posTokens = m_posPred.split("\\s+");
        if (normTokens.length != posTokens.length)
            throw new IllegalArgumentException(
                    "different number of tokens in: " + m_normPred + " and "
                            + m_posPred);

        StringBuilder normBuilder = new StringBuilder();
        StringBuilder posBuilder = new StringBuilder();

        for (int i = 0; i < posTokens.length; ++i) {
            if (!posTokens[i].equals("MD")) {
                normBuilder.append(normTokens[i] + " ");
                posBuilder.append(posTokens[i] + " ");
            }
        }
        return new RelationString(m_pred, normBuilder.toString().trim(),
                posBuilder.toString().trim());
    }

    private boolean startsWithShortenedAux(String predicate) {

        if (predicate.startsWith("'d ") || predicate.startsWith("'D ")
                || predicate.startsWith("'ll ") || predicate.startsWith("'LL ")
                || predicate.startsWith("'m ") || predicate.startsWith("'M ")
                || predicate.startsWith("'re ") || predicate.startsWith("'RE ")
                || predicate.startsWith("'s ") || predicate.startsWith("'S ")
                || predicate.startsWith("'ve ") || predicate.startsWith("'VE "))
            return true;
        return false;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split("\t");
            System.out.println("input: " + line);

            RelationString relst = new RelationString(parts[0], parts[1],
                    parts[2]);
            relst.correctNormalization();
            System.out.println("output: " + relst.toString());
        }
    }
}
