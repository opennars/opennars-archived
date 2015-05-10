/*
 * Statement.java
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
import nars.Memory;
import nars.nal.NALOperator;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Instance;
import nars.nal.nal2.InstanceProperty;
import nars.nal.nal2.Property;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.Difference;
import nars.nal.nal3.Intersect;
import nars.nal.nal4.Image;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.ImageInt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal5.Junction;
import nars.nal.nal7.TemporalRules;
import nars.util.utf8.ByteBuf;

import java.util.*;

import static nars.nal.NALOperator.STATEMENT_CLOSER;
import static nars.nal.NALOperator.STATEMENT_OPENER;

/**
 * A statement or relation is a compound term, consisting of a subject, a predicate, and a
 * relation symbol in between. It can be of either first-order or higher-order.
 */
public abstract class Statement extends Compound2 {
    
    /**
     * Constructor with partial values, called by make
     * Subclass constructors should call init after any initialization
     * 
     * @param arg The component list of the term
     */
    protected Statement(final Term subj, final Term pred) {
        super(subj, pred);
    }
    protected Statement(final Term... twoTermsPlease) {
        this(twoTermsPlease[0], twoTermsPlease[1]);
    }
    @Deprecated protected Statement() {
        this(null, null);
    }
    

    @Override
    public void init(Term[] t) {
        if (t.length!=2)
            throw new RuntimeException("Requires 2 terms: " + Arrays.toString(t));
        if (t[0]==null)
            throw new RuntimeException("Null subject: " + this);
        if (t[1]==null)
            throw new RuntimeException("Null predicate: " + this);        
        if (Global.DEBUG) {
            if (t.length > 1 && isCommutative()) {
                if (t[0].compareTo(t[1])==1) {
                    throw new RuntimeException("Commutative term requires natural order of subject,predicate: " + Arrays.toString(t));
                }
            }
        }
        super.init(t);
    }


    /**
     * Make a Statement from String, called by StringParser
     *
     * @param o The relation String
     * @param subject The first component
     * @param predicate The second component
     * @return The Statement built
     */
    final public static Statement make(final NALOperator o, final Term subject, final Term predicate, boolean customOrder, int order) {

        switch (o) {
            case INHERITANCE:
                return Inheritance.make(subject, predicate);
            case SIMILARITY:
                return Similarity.make(subject, predicate);
            case INSTANCE:
                return Instance.make(subject, predicate);
            case PROPERTY:
                return Property.make(subject, predicate);
            case INSTANCE_PROPERTY:
                return InstanceProperty.make(subject, predicate);
            case IMPLICATION:
                return Implication.make(subject, predicate);
            case IMPLICATION_AFTER:
                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_FORWARD);
            case IMPLICATION_BEFORE:
                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_BACKWARD);
            case IMPLICATION_WHEN:
                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE:
                return Equivalence.make(subject, predicate);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_FORWARD);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_CONCURRENT);
        }

        return null;
    }

    /**
     * Make a Statement from given term, called by the rules
     *
     * @param order The temporal order of the statement
     * @return The Statement built
     * @param subj The first component
     * @param pred The second component
     * @param statement A sample statement providing the class type
     * @param memory Reference to the memory
     */
    final public static Statement make(final Statement statement, final Term subj, final Term pred, int order) {

        return make(statement.operator(), subj, pred, true, order);
    }


    /**
     * Override the default in making the nameStr of the current term from
     * existing fields
     *
     * @return the nameStr of the term
     */
    @Override
    protected CharSequence makeName() {

        return makeStatementName(getSubject(), operator(), getPredicate());
    }

    @Override
    protected byte[] makeKey() {
        return makeStatementKey(getSubject(), operator(), getPredicate());
    }

    /**
     * Default method to make the nameStr of an image term from given fields
     *
     * @param subject The first component
     * @param predicate The second component
     * @param relation The relation operate
     * @return The nameStr of the term
     */
    final protected static CharSequence makeStatementNameSB(final Term subject, final NALOperator relation, final Term predicate) {
        final CharSequence subjectName = subject.toString();
        final CharSequence predicateName = predicate.toString();
        int length = subjectName.length() + predicateName.length() + relation.toString().length() + 4;
        
        StringBuilder sb = new StringBuilder(length)
            .append(STATEMENT_OPENER.ch)
            .append(subjectName)

            .append(' ').append(relation).append(' ')
            //.append(relation)

            .append(predicateName)
            .append(STATEMENT_CLOSER.ch);
            
        return sb.toString();
    }
    
    @Deprecated final protected static CharSequence makeStatementName(final Term subject, final NALOperator relation, final Term predicate) {
        throw new RuntimeException("Not necessary, utf8 keys should be used instead");
//        final CharSequence subjectName = subject.toString();
//        final CharSequence predicateName = predicate.toString();
//        int length = subjectName.length() + predicateName.length() + relation.toString().length() + 4;
//
//        StringBuilder cb = new StringBuilder(length);
//
//        cb.append(STATEMENT_OPENER.ch);
//
//        //Texts.append(cb, subjectName);
//        cb.append(subjectName);
//
//        cb.append(' ').append(relation).append(' ');
//        //cb.append(relation);
//
//        //Texts.append(cb, predicateName);
//        cb.append(predicateName);
//
//        cb.append(STATEMENT_CLOSER.ch);
//
//        return cb.toString();
    }


    final protected static byte[] makeStatementKey(final Term subject, final NALOperator relation, final Term predicate) {
        return ByteBuf.create(64)
                .add((byte)STATEMENT_OPENER.ch)
                .add(subject.name())
                .add((byte) ' ' ).add(relation.toBytes()).add((byte) ' ' )
                .add(predicate.name())
                .add((byte)STATEMENT_CLOSER.ch)
                .toBytes();
    }

    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     * @param subject The first component
     * @param predicate The second component
     * @return Whether The Statement is invalid
     */
    final public static boolean invalidStatement(final Term subject, final Term predicate) {
        if (subject==null || predicate==null) return true;
        
        if (subject.equals(predicate)) {
            return true;
        }        
        if (invalidReflexive(subject, predicate)) {
            return true;
        }
        if (invalidReflexive(predicate, subject)) {
            return true;
        }
        if ((subject instanceof Statement) && (predicate instanceof Statement)) {
            final Statement s1 = (Statement) subject;
            final Statement s2 = (Statement) predicate;

            final Term t11 = s1.getSubject();
            final Term t22 = s2.getPredicate();
            if (!t11.equals(t22)) return false;

            final Term t12 = s1.getPredicate();
            final Term t21 = s2.getSubject();
            if (t12.equals(t21)) {
                return true;
            }

            /*if (t11.equals(t22) && t12.equals(t21))
                return true;
            */
        }
        return false;
    }

    /**
     * Check if one term is identical to or included in another one, except in a
     * reflexive relation
     * <p>
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether they cannot be related in a statement
     */
    private static boolean invalidReflexive(final Term t1, final Term t2) {
        if (!(t1 instanceof Compound)) {
            return false;
        }
        if ((t1 instanceof Image /*Ext) || (t1 instanceof ImageInt*/)) {
            return false;
        }
        final Compound ct1 = (Compound) t1;
        return ct1.containsTerm(t2);
    }


    public static boolean invalidPair(final Term s1, final Term s2) {
        boolean s1Indep = s1.hasVarIndep();
        boolean s2Indep = s2.hasVarIndep();
        return (s1Indep && !s2Indep || !s1Indep && s2Indep);
    }
    

    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     * Minimum requirement: the two terms cannot be the same, or containing each
     * other as component
     *
     * @return Whether The Statement is invalid
     */
    public boolean invalid() {
        return invalidStatement(getSubject(), getPredicate());
    }
    
 
    /**
     * Return the first component of the statement
     *
     * @return The first component
     */
    public Term getSubject() {
        return term[0];
    }

    /**
     * Return the second component of the statement
     *
     * @return The second component
     */
    public Term getPredicate() {
        return term[1];
    }

    @Override public abstract Statement clone();

    public boolean subjectOrPredicateIsIndependentVar() {
        if (!hasVarIndep()) return false;

        Term subj = getSubject();
        if ((subj instanceof Variable) && (subj.hasVarIndep()))
            return true;
        Term pred = getPredicate();
        if ((pred instanceof Variable) && (pred.hasVarIndep()))
            return true;

        return false;
    }

    /**
     * Static utility class for static methods related to Terms
     * @author me
     */
    public static class Terms {

        public final static Term[] EmptyTermArray = new Term[0];

        public static final boolean equalType(final Term a, final Term b) {
            return equalType(a, b, false);
        }

        /**
         * use this instead of .getClass() == .getClass() comparisons, to allow for different implementations of the same essential type;
         * only compares operator
         */
        public static final boolean equalType(final Term a, final Term b, final boolean exactClassIfAtomic) {
            if (a instanceof Compound) {
                return (a.operator() == b.operator());
            } else {
                if (exactClassIfAtomic)
                    return a.getClass() == b.getClass();
                else
                    return true;
                /*if (a instanceof Interval) return b instanceof Interval;
                else if (a instanceof Variable) return b instanceof Variable;
                else if (a instanceof Operator) return b instanceof Operator;*/

                //return a.getClass() == b.getClass();
            }
        }

        /**
         * use this instead of .getClass() == .getClass() comparisons, to allow for different implementations of the same essential type
         */
        public static final boolean equalType(final Term a, final Term b, final boolean operator, final boolean temporalOrder) {
            if (operator) {
                if (!equalType(a, b)) return false;
            }
            if (temporalOrder) {
                if (!TemporalRules.matchingOrder(a.getTemporalOrder(), b.getTemporalOrder()))
                    return false;
            }
            return true;
        }


        public static boolean equalSubTermsInRespectToImageAndProduct(final Term a, final Term b) {
            if (a == null || b == null) {
                return false;
            }
            if (!((a instanceof Compound) && (b instanceof Compound))) {
                return a.equals(b);
            }

            if (a instanceof Inheritance && b instanceof Inheritance) {
                return equalSubjectPredicateInRespectToImageAndProduct((Statement) a, (Statement) b);
            }
            if (a instanceof Similarity && b instanceof Similarity) {
                return equalSubjectPredicateInRespectToImageAndProduct((Statement) a, (Statement) b) || equalSubjectPredicateInRespectToImageAndProduct((Statement) b, (Statement) a);
            }
            final Compound CA = ((Compound) a);
            final Compound CB = ((Compound) b);

            final int sz = CA.size();
            if (sz != CB.size()) return false;

            for (int i = 0; i < sz; i++) {
                Term x = CA.getTerm(i);
                Term y = CB.getTerm(i);
                if (!x.equals(y)) {
                    if (!validSubjPredImageAndProductPair(x, y))
                        return false;
                }
            }
            return true;
        }

        private static boolean validSubjPredImageAndProductPair(final Term x, final Term y) {
            if (x instanceof Inheritance && y instanceof Inheritance) {
                if (!equalSubjectPredicateInRespectToImageAndProduct((Statement) x, (Statement) y)) {
                    return false;
                } else {
                    return true;
                }
            }
            if (x instanceof Similarity && y instanceof Similarity) {
                if (!equalSubjectPredicateInRespectToImageAndProduct((Statement) x, (Statement) y) && !equalSubjectPredicateInRespectToImageAndProduct((Statement) y, (Statement) x)) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }

        public static Term reduceUntilLayer2(final Compound _itself, Term replacement, Memory memory) {
            if (_itself == null)
                return null;

            Term reduced = reduceComponentOneLayer(_itself, replacement, memory);
            if (!(reduced instanceof BaseCompound))
                return null;

            BaseCompound itself = (BaseCompound) reduced;
            int j = 0;
            for (Term t : itself.term) {
                Term t2 = unwrapNegation(t);
                if (!(t2 instanceof Implication) && !(t2 instanceof Equivalence) && !(t2 instanceof Junction)) {
                    j++;
                    continue;
                }
                Term ret2 = reduceComponentOneLayer((BaseCompound) t2, replacement, memory);

                //CompoundTerm itselfCompound = itself;
                Term replaced = null;
                if (j < itself.term.length)
                    replaced = itself.setTermInClone(j, ret2);

                if (replaced != null) {
                    if (replaced instanceof Compound)
                        itself = (BaseCompound) replaced;
                    else
                        return replaced;
                }
                j++;
            }
            return itself;
        }
        /*
        @Deprecated public static Term make(final String op, final ArrayList<Term> arg, final Memory memory) {
        final int length = op.length();
        if (length == 1) {
        final char c = op.charAt(0);
        switch (c) {
        case Symbols.SET_EXT_OPENER:
        return SetExt.make(arg, memory);
        case Symbols.SET_INT_OPENER:
        return SetInt.make(arg, memory);
        case Symbols.INTERSECTION_EXT_OPERATORc:
        return IntersectionExt.make(arg, memory);
        case Symbols.INTERSECTION_INT_OPERATORc:
        return IntersectionInt.make(arg, memory);
        case Symbols.DIFFERENCE_EXT_OPERATORc:
        return DifferenceExt.make(arg, memory);
        case Symbols.DIFFERENCE_INT_OPERATORc:
        return DifferenceInt.make(arg, memory);
        case Symbols.PRODUCT_OPERATORc:
        return Product.make(arg, memory);
        case Symbols.IMAGE_EXT_OPERATORc:
        return ImageExt.make(arg, memory);
        case Symbols.IMAGE_INT_OPERATORc:
        return ImageInt.make(arg, memory);
        }
        }
        else if (length == 2) {
        //since these symbols are the same character repeated, we only need to compare the first character
        final char c1 = op.charAt(0);
        final char c2 = op.charAt(1);
        if (c1 == c2) {
        switch (c1) {
        case Symbols.NEGATION_OPERATORc:
        return Negation.make(arg, memory);
        case Symbols.DISJUNCTION_OPERATORc:
        return Disjunction.make(arg, memory);
        case Symbols.CONJUNCTION_OPERATORc:
        return Conjunction.make(arg, memory);
        }
        } else if (op.equals(Symbols.SEQUENCE_OPERATOR)) {
        return Conjunction.make(arg, TemporalRules.ORDER_FORWARD, memory);
        } else if (op.equals(Symbols.PARALLEL_OPERATOR)) {
        return Conjunction.make(arg, TemporalRules.ORDER_CONCURRENT, memory);
        }
        }
        throw new RuntimeException("Unknown Term operate: " + op);
        }
         */

        /**
         * Try to remove a component from a compound
         *
         * @param t1     The compound
         * @param t2     The component
         * @param memory Reference to the memory
         * @return The new compound
         */
        public static Term reduceComponents(final Compound t1, final Term t2, final Memory memory) {
            final Term[] list;
            if (Terms.equalType(t1, t2)) {
                list = t1.cloneTermsExcept(true, ((Compound) t2));
            } else {
                list = t1.cloneTermsExceptTerm(true, t2);
            }
            if (list != null) {
                if (list.length > 1) {
                    return memory.term(t1, list);
                }
                if (list.length == 1) {
                    if ((t1 instanceof Junction) || (t1 instanceof Intersect) || (t1 instanceof Difference)) {
                        return list[0];
                    }
                }
            }
            return null;
        }

        public static Term reduceComponentOneLayer(Compound t1, Term t2, Memory memory) {
            Term[] list;
            if (Terms.equalType(t1, t2)) {
                list = t1.cloneTermsExcept(true, (Compound) t2);
            } else {
                list = t1.cloneTermsExceptTerm(true, t2);
            }
            if (list != null) {
                if (list.length > 1) {
                    return memory.term(t1, list);
                } else if (list.length == 1) {
                    return list[0];
                }
            }
            return t1;
        }


        public static Term unwrapNegation(final Term T) {
            if (T != null && T instanceof Negation) {
                return ((BaseCompound) T).term[0];
            }
            return T;
        }

        public static boolean equalSubjectPredicateInRespectToImageAndProduct(final Statement a, final Statement b) {
            return equalSubjectPredicateInRespectToImageAndProduct(a, b, true);
        }

        public static boolean equalSubjectPredicateInRespectToImageAndProduct(final Statement a, final Statement b, boolean requireEqualImageRelation) {

            if (a == null || b == null) {
                return false;
            }

            /*if (!(a instanceof Statement) || !(b instanceof Statement)) {
                return false;
            }*/

            if (a.equals(b)) {
                return true;
            }

            Statement A = (Statement) a;
            Statement B = (Statement) b;

            /*
            //REMOVED this prevents the non-statement cases further down
                "if ((predA instanceof Product) && (subjB instanceof ImageInt))"
                "if ((predB instanceof Product) && (subjA instanceof ImageInt))"
                    --both will never occur since this test prevents them
            if (!((A instanceof Similarity && B instanceof Similarity)
                    || (A instanceof Inheritance && B instanceof Inheritance)))
                return false;*/

            Term subjA = A.getSubject();
            Term predA = A.getPredicate();
            Term subjB = B.getSubject();
            Term predB = B.getPredicate();

            Term ta = null, tb = null; //the compound term to put itself in the comparison set
            Term sa = null, sb = null; //the compound term to put its components in the comparison set

            if ((subjA instanceof Product) && (predB instanceof ImageExt)) {
                ta = predA;
                sa = subjA;
                tb = subjB;
                sb = predB;
            }
            if ((subjB instanceof Product) && (predA instanceof ImageExt)) {
                ta = subjA;
                sa = predA;
                tb = predB;
                sb = subjB;
            }
            if ((predA instanceof ImageExt) && (predB instanceof ImageExt)) {
                ta = subjA;
                sa = predA;
                tb = subjB;
                sb = predB;
            }

            if ((subjA instanceof ImageInt) && (subjB instanceof ImageInt)) {
                ta = predA;
                sa = subjA;
                tb = predB;
                sb = subjB;
            }

            if ((predA instanceof Product) && (subjB instanceof ImageInt)) {
                ta = subjA;
                sa = predA;
                tb = predB;
                sb = subjB;
            }
            if ((predB instanceof Product) && (subjA instanceof ImageInt)) {
                ta = predA;
                sa = subjA;
                tb = subjB;
                sb = predB;
            }

            if (ta == null)
                return false;

            Term[] sat = ((BaseCompound) sa).term;
            Term[] sbt = ((BaseCompound) sb).term;

            //original code did not check relation index equality
            //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/language/CompoundTerm.java
            if (requireEqualImageRelation) {
                if (sa instanceof Image && sb instanceof Image) {
                    if (((Image) sa).relationIndex != ((Image) sb).relationIndex) {
                        return false;
                    }
                }
            }

            return Compound.containsAll(sat, ta, sbt, tb);

            /*
            for(Term sA : componentsA) {
                boolean had=false;
                for(Term sB : componentsB) {
                    if(sA instanceof Variable && sB instanceof Variable) {
                        if(sA.name().equals(sB.name())) {
                            had=true;
                        }
                    }
                    else if(sA.equals(sB)) {
                        had=true;
                    }
                }
                if(!had) {
                    return false;
                }
            }
            */
        }


        /**
         * Make a Statement from given components, called by the rules
         *
         * @param subj      The first component
         * @param pred      The second component
         * @param statement A sample statement providing the class type
         * @return The Statement built
         */
        public static Statement makeStatement(final Statement statement, final Term subj, final Term pred) {
            if (statement instanceof Inheritance) {
                return Inheritance.make(subj, pred);
            }
            if (statement instanceof Similarity) {
                return Similarity.make(subj, pred);
            }
            if (statement instanceof Implication) {
                return Implication.make(subj, pred, statement.getTemporalOrder());
            }
            if (statement instanceof Equivalence) {
                return Equivalence.make(subj, pred, statement.getTemporalOrder());
            }
            return null;
        }

        /**
         * Make a symmetric Statement from given term and temporal
         * information, called by the rules
         *
         * @param statement A sample asymmetric statement providing the class type
         * @param subj      The first component
         * @param pred      The second component
         * @param order     The temporal order
         * @return The Statement built
         */
        final public static Statement makeSymStatement(final Statement statement, final Term subj, final Term pred, final int order) {
            if (statement instanceof Inheritance) {
                return Similarity.make(subj, pred);
            }
            if (statement instanceof Implication) {
                return Equivalence.make(subj, pred, order);
            }
            return null;
        }

    }
}