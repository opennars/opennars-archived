/*
 * Term.java
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
import nars.Symbols;
import nars.nal.NALOperator;
import nars.nal.Named;
import nars.nal.Sentence;
import nars.nal.nal7.TemporalRules;
import nars.util.data.sorted.SortedList;

import java.io.Serializable;
import java.util.*;

public interface Term extends Cloneable, Comparable<Term>, Named<byte[]>, Termed, Serializable {


    default Term getTerm() {
        return this;
    }

    public NALOperator operator();

    public short getComplexity();

    default public boolean isAtom() {
        return (getComplexity()==1);
    }

    default public boolean isEmpty() {
        return getComplexity() == 0;
    }

    public void recurseSubterms(final TermVisitor v, Term parent);

    default public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) { }

    default public int containedTemporalRelations() { return 0; }

    default public void recurseSubterms(final TermVisitor v) {
        recurseSubterms(v, null);
    }


    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    public boolean isConstant();

    default public boolean isNormalized() { return false; }

    /** returns the normalized form of the term, or this term itself if normalization is unnecessary */
    default public <T extends Term> T normalized() {
        return (T)this;
    }


    public boolean containsTerm(final Term target);

    public boolean containsTermRecursivelyOrEquals(final Term target);

    @Deprecated default public Term ensureNormalized(String role) {
        if (hasVar() && !isNormalized()) {
            //System.err.println(this + " is not normalized but as " + role + " should have already been");
            //System.exit(1);
            throw new RuntimeException(this + " is not normalized but as " + role + " should have already been");
        }
        return this;
    }

    default public boolean isList() { return false; }

    default public boolean isExecutable(final Memory mem) {
        return false;
    }


    /** shallow clone, using the same subterm references */
    public Term clone();

    /** deep clone, creating clones of all subterms recursively */
    public Term cloneDeep();


    default public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }

    default boolean hasVar(final char type) {
        switch (type) {
            case Symbols.VAR_DEPENDENT:
                return hasVarDep();
            case Symbols.VAR_INDEPENDENT:
                return hasVarIndep();
            case Symbols.VAR_QUERY:
                return hasVarQuery();
        }
        throw new RuntimeException("Invalid variable type: " + type);
    }

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    default public boolean hasVar() { return false; }

    default public boolean hasVarIndep() { return false; }

    default public boolean hasVarDep() { return false; }

    default public boolean hasVarQuery() { return false; }

    default public boolean equalsType(final Term t) {
        return Statement.Terms.equalType(this, t);
    }

    default public boolean equalsName(final Term t) {
        final byte[] a = name();
        final byte[] b = t.name();
        if (a == b) return true;
        return equals2(a, b);
    }

    /** ordinary array equals comparison with some conditions removed */
    static boolean equals2(final byte[] a, final byte[] a2) {
        /*if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;*/

        int length = a.length;
        if (a2.length != length)
            return false;

        //backwards
        for (int i=length-1; i>=0; i--)
            if (a[i] != a2[i])
                return false;

        return true;
    }




    public static boolean levelValid(Term t, int nal) {
        NALOperator o = t.operator();
        int minLevel = o.level;
        if (minLevel > 0) {
            if (nal < minLevel)
                return false;
        }
        if (t instanceof Compound) {
            Compound tt= (Compound)t;
            int sz = tt.size();
            for (int j = 0; j < sz; j++) {
                if (!levelValid(tt.getTerm(j), nal))
                    return false;
            }
        }
        return true;
    }

    public static boolean levelValid(Sentence sentence, int nal) {
        if (nal >= 8) return true;

        Term t = sentence.getTerm();
        if (!sentence.isEternal() && nal < 7) return false;
        return levelValid(t, nal);
    }


}

