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

import com.google.common.collect.Iterables;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.NALOperator;
import nars.util.data.Utf8;
import nars.util.data.sorted.SortedList;
import nars.util.utf8.ByteBuf;
import nars.util.data.sexpression.IPair;
import nars.util.data.sexpression.Pair;

import java.util.*;

import static nars.nal.NALOperator.COMPOUND_TERM_CLOSER;
import static nars.nal.NALOperator.COMPOUND_TERM_OPENER;
import static nars.nal.term.Compound.contains;
import static nars.nal.term.Compound.containsAll;
import static nars.nal.term.Compound.toSortedSetArray;

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
     * Clone the component list
     *
     * @return The cloned component list
     */
    default public Term[] cloneTerms(final Term... additional) {
        return Compound.cloneTermsAppend(this, additional);
    }

    /** by default, creates a collection for the subterms.
     * however a subclass which stores terms as an array may provide
     * a reference to the copy, in other words, getting a
     * cloned copy is not guaranteed here.    */
    default public Term[] getTerms() {
        return Iterables.toArray(this, Term.class);
    }

    /** clones all non-constant sub-compound terms, excluding the variables themselves which are not cloned. they will be replaced in a subsequent transform step */
    default public Compound cloneVariablesDeep() {
        return (Compound) clone(cloneVariableTermsDeep());
    }

    default public Term[] cloneVariableTermsDeep() {
        Term[] l = new Term[size()];
        for (int i = 0; i < l.length; i++) {
            Term t = getTerm(i);

            if ((!(t instanceof Variable)) && (t.hasVar())) {
                t = t.cloneDeep();
            }

            //else it is an atomic term or a compoundterm with no variables, so use as-is:
            l[i] = t;
        }
        return l;
    }

    /**
     * Deep clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(final Compound original, final Term[] additional) {
        if (original == null) {
            return null;
        }

        int L = original.size() + additional.length;
        if (L == 0)
            return original.getTerms();

        //TODO apply preventUnnecessaryDeepCopy to more cases

        final Term[] arr = new Term[L];

        int j = 0;
        Term[] srcArray = original.getTerms();
        for (int i = 0; i < L; i++) {
            if (i == srcArray.length) {
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
    default public <T extends Term> T normalized() {
        return (T)this;
    }

    /** sets whether the term is normalized. if a term will always be normalized this function will not be called */
    default void setNormalized() {

    }


    abstract public int hashCode();


    /** copy subterms so that reference check will be sufficient to determine equality
     * assumes that 'equivalent' has already been determined to be equal.
     * EXPERIMENTAL     */
    void share(Compound equivalent);





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


    /** compares only the contents of the subterms; assume that the other term is of the same operator type */   default public int compareSubterms(final Compound otherCompoundOfEqualType) {
        DefaultCompound o = ((DefaultCompound) otherCompoundOfEqualType);
        int h = Integer.compare(hashCode(), o.hashCode());
        if (h == 0) {
            byte[] n1 = name();
            byte[] n2 = o.name();
            int c = Utf8.compare(n1, n2);
            if ((c == 0) && (n1!=n2)) {
                //equal string, ensure that the same byte[] instance is shared to accelerate equality comparison
                share(o);
            }
            return c;
        }
        return h;
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

    /**
     * true if equal operate and all terms contained
     */
    default public boolean containsAllTermsOf(final Term t) {
        if (Statement.Terms.equalType(this, t)) {
            return containsAll(this, ((BaseCompound) t).term);
        } else {
            return contains(this, t);
        }
    }


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


    default public Term first() {
        return getTerm(0);
    }



    default public void addTermsTo(final Collection<Term> c) {
        Iterables.addAll(c, this);
    }

    /**
     * creates a new ArrayList of the subterms
     */
    default public List<Term> asTermList() {
        List<Term> l = new ArrayList(size());
        addTermsTo(l);
        return l;
    }

    /**
     * Cloned array of Terms, except for one or more Terms.
     *
     * @param toRemove
     * @return the cloned array with the missing terms removed, OR null if no terms were actually removed when requireModification=true
     */
    default public Term[] cloneTermsExcept(final boolean requireModification, final Iterable<Term> toRemove) {
        //TODO if deep, this wastes created clones that are then removed.  correct this inefficiency?

        List<Term> l = asTermList();
        boolean removed = false;

        for (final Term t : toRemove) {
            if (l.remove(t))
                removed = true;
        }
        if ((!removed) && (requireModification))
            return null;

        return l.toArray(new Term[l.size()]);
    }

    default public Term[] cloneTermsExceptTerm(final boolean requireModification, Term termToRemove) {
        return cloneTermsExcept(requireModification, Collections.singleton(termToRemove));
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

    //TODO move this to a utility method
    public static <T> int indexOf(final T[] array, final T v) {
        int i = 0;
        for (final T e : array) {
            if (v.equals(e)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /** compres a set of terms (assumed to be unique) with another set to find if their
     * contents match. they can be in different order and still match.  this is useful for
     * comparing whether compound terms in which order doesn't matter (ex: conjunction)
     * are equivalent.
     */
    public static <T> boolean containsAll(final T[] container, final T[] content) {
        for (final T x : content) {
            if (!contains(container, x))
                return false;
        }
        return true;
    }
    public static <T> boolean containsAll(Compound container, final T[] content) {
        for (final T x : content)
            if (!contains((Iterable)container, x))
                return false;

        return true;
    }

    /** a contains any of b  NOT TESTED YET */
    public static boolean containsAny(final Term[] a, final Collection<Term> b) {
        for (final Term bx : b)
            if (contains(a, bx))
                return true;

        for (final Term ax : a)
            if (ax instanceof Compound)
                if (containsAny(((BaseCompound)ax).term, b)) //TODO write for other Compound types
                    return true;

        return false;
    }

    public static <T> boolean contains(final T[] container, final T v) {
        for (final T e : container)
            if (v.equals(e))
                return true;
        return false;
    }

    public static <T> boolean contains(Iterable<T> container, final T v) {
        for (final T e : container)
            if (v.equals(e))
                return true;
        return false;
    }

    public static boolean equals(final Term[] a, final Term[] b) {
        if (a.length!=b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i]))
                return false;
        }
        return true;
    }

    public static void verifyNonNull(Collection t) {
        for (Object o : t)
            if (o == null)
                throw new RuntimeException("Element null in: " + t);
    }

    public static void verifyNonNull(Term... t) {
        for (Object o : t)
            if (o == null)
                throw new RuntimeException("Element null in: " + Arrays.toString(t));
    }

    public static Term[] verifySortedAndUnique(final Term[] arg, boolean allowSingleton) {
        if (arg.length == 0) {
            throw new RuntimeException("Needs >0 components");
        }
        if (!allowSingleton && (arg.length == 1)) {
            throw new RuntimeException("Needs >1 components: " + Arrays.toString(arg));
        }
        Term[] s = toSortedSetArray(arg);
        if (arg.length!=s.length) {
            throw new RuntimeException("Contains duplicates: " + Arrays.toString(arg));
        }
        int j = 0;
        for (Term t : s) {
            if (!t.equals(arg[j++]))
                throw new RuntimeException("Un-ordered: " + Arrays.toString(arg) + " , correct order=" + Arrays.toString(s));
        }
        return s;
    }

    /**
     * comparison that will match constant terms, allowing variables to match regardless
     * ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
     */
    public static boolean containsVariablesAsWildcard(final Term[] term, final Term b) {
        Compound bCompound = (b instanceof Compound) ? ((Compound)b) : null;
        for (Term a : term) {
            if (a.equals(b)) return true;

            if ((a instanceof Compound) && (bCompound!=null))  {
                if (((BaseCompound)a).equalsVariablesAsWildcards(bCompound))
                    return true;
            }
        }
        return false;
    }


    /** true if any of the terms contains a variable */
    public static boolean containsVariables(Term... args) {
        for (Term t : args) {
            if (t.hasVar())
                return true;
        }
        return false;
    }

    public static boolean containsAll(Term[] sat, Term ta, Term[] sbt, Term tb) {
        //temporary set for fast containment check
        Set<Term> componentsA = Global.newHashSet(sat.length + 1);
        componentsA.add(ta);
        Collections.addAll(componentsA, sat);

        //test A contains B
        if (!componentsA.contains(tb))
            return false;
        for (Term bComponent : sbt)
            if (!componentsA.contains(bComponent))
                return false;

        return true;
    }


    public static Compound compoundOrNull(Term t) {
        if (t instanceof Compound) return (Compound)t;
        return null;
    }


    public static Term[] reverse(Term[] arg) {
        int l = arg.length;
        Term[] r = new Term[l];
        for (int i = 0; i < l; i++) {
            r[i] = arg[l - i - 1];
        }
        return r;
    }


    public static TreeSet<Term> toSortedSet(final Term... arg) {
        //use toSortedSetArray where possible
        TreeSet<Term> t = new TreeSet();
        Collections.addAll(t, arg);
        return t;
    }
    public static TreeSet<Term> toSortedSet(final Iterable<Term> arg) {
        //use toSortedSetArray where possible
        TreeSet<Term> t = new TreeSet();
        Iterables.addAll(t, arg);
        return t;
    }


    default public void transformVariableTermsDeep(VariableTransform variableTransform) {
        transformVariableTermsDeep(variableTransform, 0);
    }

    default public void transformVariableTermsDeep(VariableTransform variableTransform, int depth) {
        for (int i = 0; i < size(); i++) {
            Term t = getTerm(i);

            if (t.hasVar()) {
                if (t instanceof Compound) {
                    ((Compound)t).transformVariableTermsDeep(variableTransform);
                } else if (t instanceof Variable) {  /* it's a variable */
                    replaceTerm(i, variableTransform.apply(this, (Variable) t, depth + 1));
                }
            }
        }
    }

    /**
     * forced deep clone of terms
     */
    default public ArrayList<Term> cloneTermsListDeep() {
        ArrayList<Term> l = new ArrayList(size());
        for (final Term t : this)
            l.add(t.clone());
        return l;
    }

    /** dangerously replaces a subterm. returns the same instance being modified.
     * use with caution */
    Term replaceTerm(final int index, final Term t);


    /**
     * Try to replace a component in a compound at a given index by another one
     * If results in different subterms, the result is cloned into a new compound
     *
     * @param index The location of replacement
     * @param t     The new component
     * @return The new compound
     */
    default public Term setTermInClone(final int index, final Term t) {



        final boolean e = (t!=null) && Statement.Terms.equalType(this, t, true, true);

        //if the subterm is alredy equivalent, just return this instance because it will be equivalent
        if (t != null && (e) && (getTerm(index).equals(t)))
            return this;

        List<Term> list = asTermList();//Deep();

        list.remove(index);

        if (t != null) {
            if (!e) {
                list.add(index, t);
            } else {
                //final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                Compound tt = (Compound)t;
                for (int i = 0; i < tt.size(); i++) {
                    list.add(index + i, tt.getTerm(i));
                }
            }
        }

        return Memory.term(this, list);
    }


    final static Term[] EmptyTermArray=new Term[0];

    public static Term[] toSortedSetArray(final Term... arg) {
        switch (arg.length) {
            case 0: return EmptyTermArray;
            case 1: return new Term[] { arg[0] };
            case 2:
                Term a = arg[0];
                Term b = arg[1];
                int c = a.compareTo(b);

                if (Global.DEBUG) {
                    //verify consistency of compareTo() and equals()
                    boolean equal = a.equals(b);
                    if ((equal && (c!=0)) || (!equal && (c==0))) {
                        throw new RuntimeException("invalid order (" + c + "): " + a + " = " + b);
                    }
                }

                if (c < 0) return new Term[] { a, b };
                else if (c > 0) return new Term[] { b, a };
                else if (c == 0) return new Term[] { a }; //equal

        }

        //TODO fast sorted array for arg.length == 3

        //terms > 2:

        SortedList<Term> s = new SortedList(arg.length);
        s.setAllowDuplicate(false);

        Collections.addAll(s, arg);

        return s.toArray(new Term[s.size()] );

            /*
            TreeSet<Term> s = toSortedSet(arg);
            //toArray didnt seem to work, but it might. in the meantime:
            Term[] n = new Term[s.size()];
            int j = 0;
            for (Term x : s) {
                n[j++] = x;
            }
            return n;
            */
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
