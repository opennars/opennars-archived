package nars.nal.term;

import com.google.common.collect.Iterators;
import nars.Global;
import nars.Memory;
import nars.nal.nal7.TemporalRules;
import nars.util.data.Utf8;

import java.util.*;

/**
 * Default implementation for Compound subclasses.
 * It stores subterms in a fixed size array for maximum efficiency
 * in accessing them.  Any modifications to the content
 * of this array needs to invoke invalidate() and it must not cause a key
 * hashcode, equality, or order comparison change while it is present in a
 * bag or other indexing structure, otherwise it will cause a fault.
 *
 */
abstract public class BaseCompound implements Compound {

    /**
     * list of (direct) term
     * TODO make final again
     */
    public final Term[] term;

    /**
     * syntactic complexity of the compound, the sum of those of its term
     * plus 1
     * TODO make final again
     */
    transient public short complexity;


    /**
     * Whether contains a variable
     */
    transient private boolean hasVariables, hasVarQueries, hasVarIndeps, hasVarDeps;

    transient private int containedTemporalRelations = -1;
    private boolean normalized;



    /**
     * subclasses should be sure to call init() in their constructors; it is not done here
     * to allow subclass constructors to set data before calling init()
     */
    public BaseCompound(final Term... components) {
        super();

        this.complexity = -1;
        this.term = components;


    }

    @Override
    public Term getTerm(int subterm) {
        return term[subterm];
    }

    @Override
    public Compound normalized() {
        return cloneNormalized();
    }

    @Override
    public int compareTo(final Term that) {
        if (that==this) return 0;

        // variables have earlier sorting order than non-variables
        if (!(that instanceof Compound)) return 1;

        final Compound c = (Compound)that;


        int opdiff = getClass().getName().compareTo(c.getClass().getName());
        if (opdiff == 0) {
            //return compareSubterms(c);

            int sd = compareSubterms(c);
            if (sd == 0) {
                share(c);
            }
            return sd;
        }
        return opdiff;
    }


    @Override public void share(Compound equivalent) {

            if (!hasVar()) {
                //System.arraycopy(term, 0, equivalent.term, 0, term.length);
            }

    }

    /**
     * Normalizes if contain variables which need to be finalized for use in a Sentence
     * May return null if the resulting compound term is invalid
     */
    public <T extends Compound> T cloneNormalized() {
        if (!hasVar()) return (T) this;
        if (isNormalized()) return (T) this;


        VariableNormalization vn = new VariableNormalization(this);
        BaseCompound result = vn.getResult();
        if (result == null) return null;

        if (vn.hasRenamed()) {
            result.invalidate();
        }

        result.setNormalized(); //dont set subterms normalized, in case they are used as pieces for something else they may not actually be normalized unto themselves (ex: <#3 --> x> is not normalized if it were its own term)


//        if (!valid(result)) {
////                UnableToCloneException ntc = new UnableToCloneException("Invalid term discovered after normalization: " + result + " ; prior to normalization: " + this);
////                ntc.printStackTrace();
////                throw ntc;
//            return null;
//        }


        return (T)result;

    }

    @Override public boolean subjectOrPredicateIsIndependentVar() {
        return false;
    }


    @Override
    public void init(Term[] term) {

        this.complexity = 1;
        this.hasVariables = this.hasVarDeps = this.hasVarIndeps = this.hasVarQueries = false;
        for (final Term t : term) {
            this.complexity += t.getComplexity();
            hasVariables |= t.hasVar();
            hasVarDeps |= t.hasVarDep();
            hasVarIndeps |= t.hasVarIndep();
            hasVarQueries |= t.hasVarQuery();
        }

        invalidate();
    }

    public Compound cloneDeep() {
        Term c = clone(cloneTermsDeep());
        if (c == null) return null;

//        if (c.operator() != operator()) {
//            throw new RuntimeException("cloneDeep resulted in different class: " + c + '(' + c.getClass() + ") from " + this + " (" + getClass() + ')');
//        }


        return ((Compound) c);
    }

    @Override
    public int containedTemporalRelations() {
        if (containedTemporalRelations == -1) {

            /*if ((this instanceof Equivalence) || (this instanceof Implication))*/
            {
                int temporalOrder = this.getTemporalOrder();
                switch (temporalOrder) {
                    case TemporalRules.ORDER_FORWARD:
                    case TemporalRules.ORDER_CONCURRENT:
                    case TemporalRules.ORDER_BACKWARD:
                        containedTemporalRelations = 1;
                        break;
                    default:
                        containedTemporalRelations = 0;
                        break;
                }
            }

            for (final Term t : term)
                containedTemporalRelations += t.containedTemporalRelations();
        }
        return this.containedTemporalRelations;
    }


    /**
     * default method to make the oldName of the current term from existing
     * fields.  needs overridden in certain subclasses
     *
     * @return the oldName of the term
     */
    @Deprecated protected CharSequence makeName() {
        return Compound.makeCompoundName(operator(), term);
    }

    protected byte[] makeKey() {
        return Compound.makeCompoundNKey(operator(), term);
    }

    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */
    @Override
    public short getComplexity() {
        return complexity;
    }

    /**
     * get the number of term
     *
     * @return the size of the component list
     */
    final public int size() {
        return term.length;
    }


    public boolean isCommutative() {
        return false;
    }

    @Override
    public boolean hasVar() {
        return hasVariables;
    }

    @Override
    public boolean hasVarDep() {
        return hasVarDeps;
    }

    /* ----- variable-related utilities ----- */

    @Override
    public boolean hasVarIndep() {
        return hasVarIndeps;
    }

    @Override
    public boolean hasVarQuery() {
        return hasVarQueries;
    }

    /**
     * NOT TESTED YET
     */
    public boolean containsAnyTermsOf(final Collection<Term> c) {
        return Statement.Terms.containsAny(term, c);
    }

    final public void addTermsTo(final Collection<Term> c) {
        Collections.addAll(c, term);
    }

    @Override
    public boolean isNormalized() {
        return normalized;
    }



    protected void setNormalized() {
        this.normalized = true;
    }

    /**
     * compare subterms where any variables matched are not compared
     */
    public boolean equalsVariablesAsWildcards(final Compound c) {
        if (!Statement.Terms.equalType(this, c)) return false;
        if (size() != c.size()) return false;
        for (int i = 0; i < size(); i++) {
            Term a = term[i];
            Term b = c.getTerm(i);
            if ((a instanceof Variable) /*&& (a.hasVarDep())*/ ||
                    ((b instanceof Variable) /*&& (b.hasVarDep())*/))
                continue;
            if (!a.equals(b)) return false;
        }
        return true;
    }

    public Term[] cloneTermsReplacing(final Term from, final Term to) {
        Term[] y = new Term[term.length];
        int i = 0;
        for (Term x : term) {
            if (x.equals(from))
                x = to;
            y[i++] = x;
        }
        return y;
    }

    @Override
    public Iterator<Term> iterator() {
        return Iterators.forArray(term);
    }

    @Override
    public String toString() {
        return Utf8.fromUtf8(name());
    }

    abstract public byte[] nameCached();

    /**
     * Clone the component list
     *
     * @return The cloned component list
     */
    public Term[] cloneTerms(final Term... additional) {
        return Compound.cloneTermsAppend(term, additional);
    }


    /**
     * Cloned array of Terms, except for one or more Terms.
     *
     * @param toRemove
     * @return the cloned array with the missing terms removed, OR null if no terms were actually removed when requireModification=true
     */
    public Term[] cloneTermsExcept(final boolean requireModification, final Term... toRemove) {
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

    /**
     * creates a new ArrayList for terms
     */
    public List<Term> asTermList() {
        List<Term> l = new ArrayList(term.length);
        addTermsTo(l);
        return l;
    }

    /**
     * forced deep clone of terms
     */
    public Term[] cloneTermsDeep() {
        Term[] l = new Term[term.length];
        for (int i = 0; i < l.length; i++)
            l[i] = term[i].cloneDeep();
        return l;
    }


    /** clones all non-constant sub-compound terms, excluding the variables themselves which are not cloned. they will be replaced in a subsequent transform step */
    protected BaseCompound cloneVariablesDeep() {
        return (BaseCompound) clone(cloneVariableTermsDeep());
    }

    public Term[] cloneVariableTermsDeep() {
        Term[] l = new Term[term.length];
        for (int i = 0; i < l.length; i++) {
            Term t = term[i];

            if ((!(t instanceof Variable)) && (t.hasVar())) {
                t = t.cloneDeep();
            }

            //else it is an atomic term or a compoundterm with no variables, so use as-is:
            l[i] = t;
        }
        return l;
    }

    protected void transformVariableTermsDeep(VariableTransform variableTransform) {
        transformVariableTermsDeep(variableTransform, 0);
    }

    protected void transformVariableTermsDeep(VariableTransform variableTransform, int depth) {
        for (int i = 0; i < term.length; i++) {
            Term t = term[i];

            if (t.hasVar()) {
                if (t instanceof Compound) {
                    ((BaseCompound)t).transformVariableTermsDeep(variableTransform);
                } else if (t instanceof Variable) {  /* it's a variable */
                    term[i] = variableTransform.apply(this, (Variable)t, depth+1);
                }
            }
        }
    }

    /**
     * forced deep clone of terms
     */
    public ArrayList<Term> cloneTermsListDeep() {
        ArrayList<Term> l = new ArrayList(term.length);
        for (final Term t : term)
            l.add(t.clone());
        return l;
    }

    /**
     * Check the subterms (first level only) for a target term
     *
     * @param t The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean containsTerm(final Term t) {
        return Statement.Terms.contains(term, t);
    }

    /**
     * Recursively check if a compound contains a term
     * This method DOES check the equality of this term itself.
     * Although that is how Term.containsTerm operates
     *
     * @param target The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean containsTermRecursivelyOrEquals(final Term target) {
        if (this.equals(target)) return true;
        return containsTermRecursively(target);
    }

    /**
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
     *  ^^^ is this right? if so then try containsVariablesAsWildcard
     *
     * @param t The component to be checked
     * @return Whether the component is in the compound
     */
    //return Terms.containsVariablesAsWildcard(term, t);
    //^^ ???

    /**
     * searches for a subterm
     * TODO parameter for max (int) level to scan down
     */
    public boolean containsTermRecursively(final Term target) {
        for (Term x : term) {
            if (x.equals(target)) return true;
            if (x instanceof Compound) {
                if (((BaseCompound) x).containsTermRecursively(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * true if equal operate and all terms contained
     */
    public boolean containsAllTermsOf(final Term t) {
        if (Statement.Terms.equalType(this, t)) {
            return Statement.Terms.containsAll(term, ((BaseCompound) t).term);
        } else {
            return Statement.Terms.contains(term, t);
        }
    }

    /**
     * Try to replace a component in a compound at a given index by another one
     *
     * @param index The location of replacement
     * @param t     The new component
     * @return The new compound
     */
    public Term setComponent(final int index, final Term t) {



        final boolean e = (t!=null) && Statement.Terms.equalType(this, t, true, true);

        //if the subterm is alredy equivalent, just return this instance because it will be equivalent
        if (t != null && (e) && (term[index].equals(t)))
            return this;

        List<Term> list = asTermList();//Deep();

        list.remove(index);

        if (t != null) {
            if (!e) {
                list.add(index, t);
            } else {
                //final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                Term[] tt = ((BaseCompound) t).term;
                for (int i = 0; i < tt.length; i++) {
                    list.add(index + i, tt[i]);
                }
            }
        }

        return Memory.term(this, list);
    }

    public static class VariableNormalization implements VariableTransform {

        /** necessary to implement alternate variable hash/equality test for use in normalization's variable transform hashmap */
        final static class VariableID {

            final Variable v;

            public VariableID(final Variable v) {
                this.v = v;
            }

            @Override
            public boolean equals(final Object obj) {
                return Arrays.equals(((VariableID) obj).name(), name());
            }

            public byte[] name() { return v.name(); }

            @Override
            public int hashCode() {
                return v.hashCode();
            }
        }

        Map<VariableID, Variable> rename = Global.newHashMap();

        final BaseCompound result;
        boolean renamed = false;

        public VariableNormalization(BaseCompound target) {
            this.result = target.cloneVariablesDeep();
            if (this.result!=null)
                this.result.transformVariableTermsDeep(this);
        }

        @Override
        public Variable apply(final Compound ct, final Variable v, int depth) {
            VariableID vname = new VariableID(v);
//            if (!v.hasVarIndep() && v.isScoped()) //already scoped; ensure uniqueness?
//                vname = vname.toString() + v.getScope().name();

            Variable vv = rename.get(vname);

            if (vv == null) {
                //type + id
                vv = new Variable(
                        Variable.getName(v.getType(), rename.size() + 1),
                        true
                );
                rename.put(vname, vv);
                if (!vv.name().equals(v.name()))
                    renamed = true;
            }

            return vv;
        }

        public boolean hasRenamed() {
            return renamed;
        }

        public BaseCompound getResult() {
            return result;
        }
    }

}
