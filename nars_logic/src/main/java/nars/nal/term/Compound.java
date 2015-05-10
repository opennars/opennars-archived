/*
 * CompoundTerm.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.term;

import nars.Global;
import nars.Symbols;
import nars.nal.NALOperator;
import nars.util.utf8.ByteBuf;
import nars.util.data.sexpression.IPair;
import nars.util.data.sexpression.Pair;

import java.util.*;

import static nars.nal.NALOperator.COMPOUND_TERM_CLOSER;
import static nars.nal.NALOperator.COMPOUND_TERM_OPENER;

/** a compound term */
public interface Compound extends Term, Iterable<Term>, IPair {




    /**
     * build a component list from terms
     *
     * @return the component list
     */
    public static Term[] termArray(final Term... t) {
        return t;
    }

    public static List<Term> termList(final Term... t) {
        return Arrays.asList((Term[]) t);
    }

    /**
     * single term version of makeCompoundName without iteration for efficiency
     */
    public static CharSequence makeCompoundName(final NALOperator op, final Term singleTerm) {
        int size = 2; // beginning and end parens
        String opString = op.toString();
        size += opString.length();
        final CharSequence tString = singleTerm.toString();
        size += tString.length();
        return new StringBuilder(size).append(COMPOUND_TERM_OPENER.ch).append(opString).append(Symbols.ARGUMENT_SEPARATOR).append(tString).append(COMPOUND_TERM_CLOSER.ch).toString();
    }
    public static byte[] makeCompound1Key(final NALOperator op, final Term singleTerm) {

        final byte[] opString = op.toBytes();

        final byte[] tString = singleTerm.name();

        return ByteBuf.create(1 + opString.length + 1 + tString.length + 1)
                .add((byte)COMPOUND_TERM_OPENER.ch)
                .add(opString)
                .add((byte) Symbols.ARGUMENT_SEPARATOR)
                .add(tString)
                .add((byte) COMPOUND_TERM_CLOSER.ch)
                .toBytes();
    }

    public static byte[] makeCompoundNKey(final NALOperator op, final Term... arg) {

        ByteBuf b = ByteBuf.create(64)
                .add((byte)COMPOUND_TERM_OPENER.ch)
                .add(op.toBytes());

        for  (final Term t : arg) {
            b.add((byte)Symbols.ARGUMENT_SEPARATOR).add(t.name());
        }

        return b.add((byte) COMPOUND_TERM_CLOSER.ch).toBytes();

    }

    /**
     * default method to make the oldName of a compound term from given fields
     *
     * @param op  the term operate
     * @param arg the list of term
     * @return the oldName of the term
     */
    public static CharSequence makeCompoundName(final NALOperator op, final Term... arg) {
        throw new RuntimeException("Not necessary, utf8 keys should be used instead");
//
//        int size = 1 + 1;
//
//        String opString = op.toString();
//        size += opString.length();
//        /*for (final Term t : arg)
//            size += 1 + t.name().length();*/
//
//
//        final StringBuilder n = new StringBuilder(size)
//                .append(COMPOUND_TERM_OPENER.ch).append(opString);
//
//        for (final Term t : arg) {
//            n.append(Symbols.ARGUMENT_SEPARATOR).append(t.toString());
//        }
//
//        n.append(COMPOUND_TERM_CLOSER.ch);
//
//        return n.toString();
    }

    /**
     * Deep clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(final Term[] original, final Term[] additional) {
        if (original == null) {
            return null;
        }

        int L = original.length + additional.length;
        if (L == 0)
            return original;

        //TODO apply preventUnnecessaryDeepCopy to more cases

        final Term[] arr = new Term[L];

        int j = 0;
        Term[] srcArray = original;
        for (int i = 0; i < L; i++) {
            if (i == original.length) {
                srcArray = additional;
                j = 0;
            }

            arr[i] = srcArray[j++];
        }

        return arr;

    }

    public static <T> void shuffle(T[] ar, Random rnd) {
        if (ar.length < 2) return;
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            T a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    /**
     * Abstract method to get the operate of the compound
     */
    @Override
    public NALOperator operator();


    Term getTerm(int subterm);

    /** returns the normalized form of this compound, or this compound itself if it doesn't need normalized */
    default public Compound normalized() {
        return this;
    }


    abstract public int hashCode();


    /** copy subterms so that reference check will be sufficient to determine equality
     * assumes that 'equivalent' has already been determined to be equal.
     * EXPERIMENTAL     */
    void share(Compound equivalent);


    /** compares only the contents of the subterms; assume that the other term is of the same operator type */
    abstract public int compareSubterms(final Compound otherCompoundOfEqualType);

    @Override
    abstract public boolean equals(final Object that);

    default public void recurseSubterms(final TermVisitor v, Term parent) {
        v.visit(this, parent);
        if (this instanceof Compound) {
            final int s = size();
            for (int i = 0; i < s; i++) {
                Term t = getTerm(i);
                t.recurseSubterms(v, this);
            }
        }
    }

    default public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
        if (hasVar()) {
            v.visit(this, parent);
            final int s = size();
            for (int i = 0; i < s; i++) {
                Term t = getTerm(i);
                t.recurseSubtermsContainingVariables(v, this);
            }
        }
    }

    /** extracts a subterm provided by the index tuple
     *  returns null if specified subterm does not exist
     * */
    default public <X extends Term> X subterm(int... index) {
        Term ptr = this;
        for (int i : index) {
            if (ptr instanceof Compound) {
                ptr = ((Compound)ptr).getTerm(i);;
            }
        }
        return (X) ptr;
    }

    public interface VariableTransform  {
        public Variable apply(Compound containingCompound, Variable v, int depth);
    }




/* UNTESTED
    public Compound clone(VariableTransform t) {
        if (!hasVar())
            throw new RuntimeException("this VariableTransform clone should not have been necessary");

        Compound result = cloneVariablesDeep();
        if (result == null)
            throw new RuntimeException("unable to clone: " + this);

        result.transformVariableTermsDeep(t);

        result.invalidate();

        return result;
    } */


    boolean subjectOrPredicateIsIndependentVar();

    /**
     * call after changing Term[] contents: recalculates variables and complexity
     * this is where the provided subterms for BaseCompound arrive.
     * it may not be necessary to include in Compound
     */
    default void init(Term[] content) {

    }


    /** called when a compound should invalidate any cached data because its
     * state has changed.  terms are generally immutable but not entirely
     * so this is necessary.
     */
    default public void invalidate() {

    }



    /**
     * Must be Term return type because the type of Term may change with different arguments
     */
    abstract public Term clone(final Term[] replaced);


    default public Compound cloneDeep() {
        return this;
    }




    default public int containedTemporalRelations() {
        return 0;
    }






    abstract public byte[] name();

 

    /* ----- utilities for other fields ----- */




    /**
     * Check if the order of the term matters
     * <p>
     * commutative CompoundTerms: Sets, Intersections Commutative Statements:
     * Similarity, Equivalence (except the one with a temporal order)
     * Commutative CompoundStatements: Disjunction, Conjunction (except the one
     * with a temporal order)
     *
     * @return The default value is false
     */
    default public boolean isCommutative() {
        return false;
    }


    /** number of immediate subterms; this is different from complexity because
     * complexity includes recursively summed sizes
     * @return
     */
    public int size();




    default public boolean isConstant() {
        return isNormalized();
    }


    /**
     * Gives a set of all (unique) contained term, recursively
     */
    default public Set<Term> getContainedTerms() {
        Set<Term> s = Global.newHashSet(getComplexity());
        final int z = size();
        for (int i = 0; i < z; i++) {
            Term t = getTerm(i);
            s.add(t);
            if (t instanceof Compound)
                s.addAll(((Compound) t).getContainedTerms());
        }
        return s;
    }




    /**
     * Check whether the compound contains all term of another term, or
     that term as a whole
     *
     * @param t The other term
     * @return Whether the term are all in the compound
     */
//    public boolean containsAllTermsOf_(final Term t) {
//        if (t instanceof CompoundTerm) {
//        //if (operate() == t.operate()) {
//            //TODO make unit test for containsAll
//            return Terms.containsAll(term, ((CompoundTerm) t).term );
//        } else {
//            return Terms.contains(term, t);
//        }
//    }






    /**
     * Recursively apply a substitute to the current CompoundTerm
     * May return null if the term can not be created
     *
     * @param subs
     */
    default public Term applySubstitute(final Map<Term, Term> subs) {
        if ((subs == null) || (subs.isEmpty())) {
            return this;
        }

        int sz = size();
        Term[] tt = new Term[sz];
        boolean modified = false;

        for (int i = 0; i < tt.length; i++) {
            Term t1 = tt[i] = getTerm(i);

            if (subs.containsKey(t1)) {
                Term t2 = subs.get(t1);
                while (subs.containsKey(t2)) {
                    t2 = subs.get(t2);
                }
                //prevents infinite recursion
                if (!t2.containsTerm(t1)) {
                    tt[i] = t2; //t2.clone();
                    modified = true;
                }
            } else if (t1 instanceof Compound) {
                Term ss = ((Compound) t1).applySubstitute(subs);
                if (ss != null) {
                    tt[i] = ss;
                    if (!tt[i].equals(getTerm(i)))
                        modified = true;
                }
            }
        }
        if (!modified)
            return this;

        if (this.isCommutative()) {
            Arrays.sort(tt);
        }

        return this.clone(tt);
    }


//    /** caches a static copy of commonly uesd index variables of each variable type */
//    public static final int maxCachedVariableIndex = 32;
//    public static final Variable[][] varCache = (Variable[][]) Array.newInstance(Variable.class, 3, maxCachedVariableIndex);
//    
//    public static Variable getIndexVariable(final char type, final int i) {
//        int typeI;
//        switch (type) {
//            case '#': typeI = 0; break;
//            case '$': typeI = 1; break;
//            case '?': typeI = 2; break;
//            default: throw new RuntimeException("Invalid variable type: " + type + ", index " + i);
//        }
//        
//        if (i < maxCachedVariableIndex) {
//            Variable existing = varCache[typeI][i];
//            if (existing == null)
//                existing = varCache[typeI][i] = new Variable(type + String.valueOf(i));
//            return existing;
//        }
//        else
//            return new Variable(type + String.valueOf(i));
//    }


//    /**
//     * Recursively rename the variables in the compound
//     *
//     * @param map The substitution established so far
//     * @return an array of terms, normalized; may return the original Term[] array if nothing changed,
//     * otherwise a clone of the array will be returned
//     */
//    public static Term[] normalizeVariableNames(String prefix, final Term[] s, final HashMap<Variable, Variable> map) {
//        
//        boolean renamed = false;
//        Term[] t = s.clone();
//        char c = 'a';
//        for (int i = 0; i < t.length; i++) {
//            final Term term = t[i];
//            
//
//            if (term instanceof Variable) {
//
//                Variable termV = (Variable)term;                
//                Variable var;
//
//                var = map.get(termV);
//                if (var == null) {
//                    //var = getIndexVariable(termV.getType(), map.size() + 1);
//                    var = new Variable(termV.getType() + /*prefix + */String.valueOf(map.size() + 1));
//                }
//                
//                if (!termV.equals(var)) {
//                    t[i] = var;
//                    renamed = true;
//                }
//
//                map.put(termV, var);
//
//            } else if (term instanceof CompoundTerm) {
//                CompoundTerm ct = (CompoundTerm)term;
//                if (ct.containVar()) {
//                    Term[] d = normalizeVariableNames(prefix + Character.toString(c),  ct.term, map);
//                    if (d!=ct.term) {                        
//                        t[i] = ct.clone(d, true);
//                        renamed = true;
//                    }
//                }
//            }        
//            c++;
//        }
//            
//        if (renamed) {            
//            return t;
//        }
//        else 
//            return s;
//    }

    /**
     * returns result of applySubstitute, if and only if it's a CompoundTerm.
     * otherwise it is null
     */
    default public Compound applySubstituteToCompound(Map<Term, Term> substitute) {
        Term t = applySubstitute(substitute);
        if (t instanceof Compound)
            return ((Compound) t);
        return null;
    }


//    /** recursively sets all subterms normalization */
//    protected void setNormalizedSubTerms(boolean b) {
//        setNormalized(b);
//
//        //recursively set subterms as normalized
//        for (final Term t : term)
//            if (t instanceof CompoundTerm)
//                ((CompoundTerm)t).setNormalizedSubTerms(b);
//    }


    default public Object first() {
        return getTerm(0);
    }



    /**
     * cdr or 'rest' function for s-expression interface when arity > 1
     * TODO incomplete, for interfacing with scheme interpreter
     */
    @Override
    default public Object rest() {
        int sz = size();
        switch (sz) {
            case 2:
                return getTerm(1);
            case 3:
                return new Pair(getTerm(1), getTerm(2));
            case 4:
                return new Pair(getTerm(1), new Pair(getTerm(2), getTerm(3)));
            case 0:
            case 1:
            default:
                throw new RuntimeException("Pair fault");
        }

//        //this may need tested better:
//        Pair p = null;
//        for (int i = term.length - 2; i >= 0; i--) {
//            if (p == null)
//                p = new Pair(term[i], term[i + 1]);
//            else
//                p = new Pair(term[i], p);
//        }
//        return p;
    }

    @Override
    default public Object setFirst(Object first) {
        throw new RuntimeException(this + " not modifiable");
    }

    @Override
    default public Object setRest(Object rest) {
        throw new RuntimeException(this + " not modifiable");
    }

    public static class InvalidTermConstruction extends RuntimeException {
        public InvalidTermConstruction(String reason) {
            super(reason);
        }
    }




//    @Deprecated public static class UnableToCloneException extends RuntimeException {
//
//        public UnableToCloneException(String message) {
//            super(message);
//        }
//
//        @Override
//        public synchronized Throwable fillInStackTrace() {
//            /*if (Parameters.DEBUG) {
//                return super.fillInStackTrace();
//            } else {*/
//                //avoid recording stack trace for efficiency reasons
//                return this;
//            //}
//        }
//
//
//    }


}


//    /** performs a deep comparison of the term structure which should have the same result as normal equals(), but slower */
//    @Deprecated public boolean equalsByTerm(final Object that) {
//        if (!(that instanceof CompoundTerm)) return false;
//
//        final CompoundTerm t = (CompoundTerm)that;
//
//        if (operate() != t.operate())
//            return false;
//
//        if (getComplexity()!= t.getComplexity())
//            return false;
//
//        if (getTemporalOrder()!=t.getTemporalOrder())
//            return false;
//
//        if (!equals2(t))
//            return false;
//
//        if (term.length!=t.term.length)
//            return false;
//
//        for (int i = 0; i < term.length; i++) {
//            if (!term[i].equals(t.term[i]))
//                return false;
//        }
//
//        return true;
//    }
//
//
//
//
//    /** additional equality checks, in subclasses, only called by equalsByTerm */
//    @Deprecated public boolean equals2(final CompoundTerm other) {
//        return true;
//    }

//    /** may be overridden in subclass to include other details */
//    protected int calcHash() {
//        //return Objects.hash(operate(), Arrays.hashCode(term), getTemporalOrder());
//        return name().hashCode();
//    }

//
//    /**
//     * Orders among terms: variable < atomic < compound
//     *
//     * @param that The Term to be compared with the current Term
//\     * @return The order of the two terms
//     */
//    @Override
//    public int compareTo(final AbstractTerm that) {
//        if (this == that) return 0;
//
//        if (that instanceof CompoundTerm) {
//            final CompoundTerm t = (CompoundTerm) that;
//            if (size() == t.size()) {
//                int opDiff = this.operate().ordinal() - t.operate().ordinal(); //should be faster faster than Enum.compareTo
//                if (opDiff != 0) {
//                    return opDiff;
//                }
//
//                int tDiff = this.getTemporalOrder() - t.getTemporalOrder(); //should be faster faster than Enum.compareTo
//                if (tDiff != 0) {
//                    return tDiff;
//                }
//
//                for (int i = 0; i < term.length; i++) {
//                    final int diff = term[i].compareTo(t.term[i]);
//                    if (diff != 0) {
//                        return diff;
//                    }
//                }
//
//                return 0;
//            } else {
//                return size() - t.size();
//            }
//        } else {
//            return 1;
//        }
//    }



    /*
    @Override
    public boolean equals(final Object that) {
        return (that instanceof Term) && (compareTo((Term) that) == 0);
    }
    */


//
//
//
//
//    /**
//     * Orders among terms: variable < atomic < compound
//     *
//     * @param that The Term to be compared with the current Term
//\     * @return The order of the two terms
//     */
//    @Override
//    public int compareTo(final Term that) {
//        /*if (!(that instanceof CompoundTerm)) {
//            return getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
//        }
//        */
//        return -name.compareTo(that.name());
//            /*
//            if (size() == t.size()) {
//                int opDiff = this.operate().ordinal() - t.operate().ordinal(); //should be faster faster than Enum.compareTo
//                if (opDiff != 0) {
//                    return opDiff;
//                }
//
//                for (int i = 0; i < term.length; i++) {
//                    final int diff = term[i].compareTo(t.term[i]);
//                    if (diff != 0) {
//                        return diff;
//                    }
//                }
//
//                return 0;
//            } else {
//                return size() - t.size();
//            }
//        } else {
//            return 1;
//            */
//    }
