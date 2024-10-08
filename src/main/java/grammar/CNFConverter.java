/*
 *  JFLAP - Formal Languages and Automata Package
 *
 *
 *  Susan H. Rodger
 *  Computer Science Department
 *  Duke University
 *  August 27, 2009

 *  Copyright (c) 2002-2009
 *  All rights reserved.

 *  JFLAP is open source software. Please see the LICENSE for terms.
 *
 */


package grammar;

import java.util.*;
import java.util.Map.Entry;

/**
 * Converts grammars to Chomsky normal form.
 *
 * @author Thomas Finley
 */

public class CNFConverter {
    /**
     * The comparator for productions.
     */
    private final ProductionComparator productionComparator;
    /**
     * The production directory.
     */
    private final ProductionDirectory productionDirectory;
    /**
     * The grammar we're converting.
     */
    private final Grammar grammar;
    /**
     * In the {@link #getLeft} function, a new production may be added. After
     * the function is called, this variable will hold if an addition was made.
     */
    private boolean leftAdded;
    /**
     * The number of variable things assigned already.
     */
    private int numVariables = 0;

    /**
     * Instantiates a new chomsky normal converter.
     *
     * @param grammar the grammar to convert
     */
    public CNFConverter(Grammar grammar) {
        this.grammar = grammar;
        productionComparator = new ProductionComparator(grammar);
        productionDirectory = new ProductionDirectory(grammar);
    }

    /**
     * Breaks a string of symbols into separate symbols.
     *
     * @param string the string of symbols
     * @return the array of separate symbols
     * @throws IllegalArgumentException if the string has anything that interferes with productions
     */
    public static String[] separateString(String string) {
        LinkedList<String> list = new LinkedList<>();
        for (int i = string.length() - 1; i >= 0; i--) {
            int start = i;
            if (string.charAt(i) != ')') {
                list.addFirst(string.substring(i, i + 1));
                continue;
            }
            while (i > 0 && string.charAt(i) != '(')
                i--;
            if (i > 0)
                i--;
            list.addFirst(string.substring(i, start + 1));
        }
        return list.toArray(new String[0]);
    }

    /**
     * Given an array of productions, this returns the proper replacements of
     * the "()" rules.
     *
     * @param p the array of productions
     * @return an equivalent set of productions
     * @throws UnsupportedOperationException if the number of variables needed exceeds 26
     */
    public static Production[] convert(Production[] p) {
        // Figure out what we need, and what's available.
        TreeSet<String> vars = new TreeSet<>(); // Set of available vars.
        for (char c = 'A'; c <= 'Z'; c++)
            vars.add("" + c);
        TreeSet<String> unresolved = new TreeSet<>(); // Set of vars needing conversion.
        for (int i = 0; i < p.length; i++) {
            String[] tokens = separateString(p[i].getRHS());
            for (int j = 0; j < tokens.length; j++)
                if (tokens[j].length() == 1)
                    vars.remove(tokens[j]);
                else
                    unresolved.add(tokens[j]);
            vars.remove(p[i].getLHS());
        }
        // Can it be done?
        int needed = unresolved.size() + 26 - vars.size();
        if (unresolved.size() > vars.size()) {
            // We canna do it, captain!
            throw new UnsupportedOperationException(
                    "26 variables available, but " + needed + " needed!");
        }
        // Build the replacement map.
        HashMap<String, String> replacements = new HashMap<>();
        Iterator<String> it = unresolved.iterator(), it2 = vars.iterator();
        while (it.hasNext())
            replacements.put(it.next(), it2.next());
        // Make the substitutions.
        Production[] pnew = new Production[p.length];
        for (int i = 0; i < p.length; i++) {
            String[] tokens = separateString(p[i].getRHS());
            String rhs = "";
            for (int j = 0; j < tokens.length; j++)
                if (tokens[j].length() == 1)
                    rhs += tokens[j];
                else
                    rhs += replacements.get(tokens[j]);
            String lhs = p[i].getLHS();
            if (lhs.length() != 1)
                lhs = replacements.get(lhs);
            pnew[i] = new Production(lhs, rhs);
        }
        return pnew;
    }

    /**
     * Shortens a production to two sides.
     */

    /**
     * Given a symbol string A, even a single symbol, this will return a LHS of
     * a productions where LHS->A is an okay production to map to A.
     *
     * @param string the symbol string
     * @return a valid left hand side for that string
     */
    private String getLeft(String right) {
        String left = productionDirectory.getLeft(right);
        leftAdded = false;
        if (left != null)
            return left;
        leftAdded = true;
        left = grammar.isTerminal(right) ? "B(" + right + ")" : "D("
                + (++numVariables) + ")";
        Production p = new Production(left, right);
        productionDirectory.add(p);
        return left;
    }

    /**
     * Returns the array of productions needed to replace a production.
     *
     * @param production the production to replace
     * @return the array of productions that will replace this production
     * @throws IllegalArgumentException if the production does not need to be replaced
     */
    public Production[] replacements(Production production) {
        String rhs = production.getRHS(), lhs = production.getLHS();
        if (rhs.length() == 1) {
            // Given that unit productions have been removed, this
            // must be a terminal production.
            throw new IllegalArgumentException(production
                    + " is a terminal production!");
        }
        String[] tokens = separateString(rhs);
        // Do we need to determinalize this?
        for (int i = 0; i < tokens.length; i++)
            if (grammar.isTerminal(tokens[i]))
                return determinalize(production);
        // No termianls to resolve...
        if (tokens.length <= 2)
            if (tokens.length == 2)
                throw new IllegalArgumentException(production
                        + " has two variables already!");
            else
                throw new IllegalArgumentException(production
                        + " is in bad form!");
        // Now we're all right.
        String remainder = rhs.substring(tokens[0].length());
        String left = getLeft(remainder);
        if (leftAdded)
            return new Production[]{new Production(lhs, tokens[0] + left),
                    new Production(left, remainder)};
        else
            return new Production[]{new Production(lhs, tokens[0] + left)};
    }

    /**
     * Returns if a production is in the proper Chomsky normal form.
     *
     * @param production the production to test
     * @return if the production maps to either two variables, or a single
     * terminal
     */
    public boolean isChomsky(Production production) {
        String[] tokens = separateString(production.getRHS());
        switch (tokens.length) {
            case 1:
                return grammar.isTerminal(tokens[0]);
            case 2:
                return !(grammar.isTerminal(tokens[0]) || grammar
                        .isTerminal(tokens[1]));
            default:
                return false;
        }
    }

    /**
     * De-terminalizes a production.
     *
     * @param production a production to "determinalize"
     * @return the determinalized production
     */
    public Production[] determinalize(Production production) {
        String[] tokens = separateString(production.getRHS());
        List<Production> list = new ArrayList<>();
        String rhs = "";
        for (int i = 0; i < tokens.length; i++) {
            if (grammar.isTerminal(tokens[i])) {
                String newR = getLeft(tokens[i]);
                if (leftAdded)
                    list.add(new Production(newR, tokens[i]));
                rhs += newR;
            } else
                rhs += tokens[i];
        }
        list.add(0, new Production(production.getLHS(), rhs));
        return list.toArray(new Production[0]);
    }

    /**
     * This is not a directory of productions in the grammar as such, but rather
     * more of a catalog of those strings that certain left hand sides mapped to
     * at one point. The idea is to avoid, as much as is sensibly possible,
     * redundency in variable assignments.
     */
    private class ProductionDirectory {
        /**
         * The productions.
         */
        private final List<Production> productions = new ArrayList<>();
        /**
         * The map of LHS to multiple RHS stored as sets.
         */
        private final Map<String, Set<String>> lhsToRhs = new TreeMap<>();
        /**
         * The map of RHS to a LHS, given that the RHS of the production is
         * unique to the LHS of the production (that is, that LHS maps only to
         * this RHS).
         */
        private final Map<String, String> rhsToLhs = new HashMap<>();

        /**
         * Instantiates a production directory.
         */
        public ProductionDirectory(Grammar grammar) {
            Production[] p = grammar.getProductions();
            // Create the map of LHSes to RHSes.
            for (int i = 0; i < p.length; i++) {
                String lhs = p[i].getLHS(), rhs = p[i].getRHS();
                if (rhs.indexOf('(') != -1)
                    throw new IllegalArgumentException(
                            "Grammar has the ( character, which is reserved.");
                if (rhs.indexOf(')') != -1)
                    throw new IllegalArgumentException(
                            "Grammar has the ) character, which is reserved.");
                // Add it to the list of productions.
                productions.add(p[i]);
                // Check the right hand side map.
                Set<String> rhses = lhsToRhs.get(lhs);
                if (rhses == null) {
                    rhses = new HashSet<>();
                    lhsToRhs.put(lhs, rhses);
                }
                rhses.add(rhs);
            }
            // Creates the map of RHSes to LHSes.
            Iterator<Entry<String, Set<String>>> it = lhsToRhs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Set<String>> entry = it.next();
                Set<String> rhses = entry.getValue();
                Iterator<String> it2 = rhses.iterator();
                String rhs = it2.next();
                if (it2.hasNext())
                    continue;
                rhsToLhs.put(rhs, entry.getKey());
            }
        }

        /**
         * After the first initialization, this can be used to add productions.
         * The left hand side must not have been in any production added before,
         * or in the initializations.
         *
         * @param production the production to add
         */
        public void add(Production production) {
            String lhs = production.getLHS(), rhs = production.getRHS();
            // Does the RHS already have a unique mapping?
            if (rhsToLhs.containsKey(rhs))
                throw new IllegalArgumentException(rhs
                        + " already represented by " + rhsToLhs.get(rhs));
            // Add the production.
            Set<String> rhses = lhsToRhs.get(lhs);
            if (rhses == null) {
                rhses = new HashSet<String>();
                lhsToRhs.put(lhs, rhses);
            }
            rhses.add(rhs);
            rhsToLhs.put(rhs, lhs);
        }

        /**
         * Returns an array of RHSes for a given LHS.
         *
         * @param lhs the LHS to check for
         * @return an array of RHSes, or an empty array if this LHS does not
         * appear in the grammar
         */
        public String[] getRight(String lhs) {
            Set<String> rhses = lhsToRhs.get(lhs);
            return rhses.toArray(new String[0]);
        }

        /**
         * Returns a LHS for a given RHS, if that RHS is unique for a LHS.
         *
         * @param lhs the RHS to check for a unique representation of
         * @return a RHS that maps to this LHS uniquely, i.e., for LHS->RHS,
         * there is no other rule predicated on LHS
         */
        public String getLeft(String rhs) {
            return rhsToLhs.get(rhs);
        }
    }
}
