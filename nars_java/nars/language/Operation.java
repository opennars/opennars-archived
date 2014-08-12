/*
 * Inheritance.java
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
package nars.language;

import java.util.ArrayList;
import nars.io.Symbols;

import nars.operators.Operator;
import nars.storage.Memory;

/**
 * An operation consists of an operator and an (non-empty) argument list. It is
 * interpreted as an Inheritance relation <(*, arguments) --> operator> in
 * inference, while named and displayed as (operator, arguments) in the
 * interface.
 */
public class Operation extends Inheritance {

    /**
     * Constructor with partial values, called by make
     *
     * @param name
     * @param arg The component list of the term
     */
    public Operation(String name, ArrayList<Term> arg) {
        super(arg);
        this.name = name;
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param con Whether the term is a constant
     * @param i Syntactic complexity of the compound
     */
    protected Operation(String n, ArrayList<Term> cs, boolean con, short i) {
        super(n, cs, con, i);
    }

    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Object clone() {
        return new Operation(name, cloneList(components), isConstant, complexity);
    }
    
    /**
     * Try to make a new compound from two components. Called by the inference
     * rules.
     *
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Operation make(Operator oper, ArrayList<Term> args, Memory memory) {
        String name = makeName(oper.getName(), args, memory);
        Term t = memory.nameToTerm(name);
        if (t != null) {
            return (Operation) t;
        }
        ArrayList<Term> opArg = new ArrayList<>(2);
        Term list = Product.make(args, memory);
        opArg.add(list);
        opArg.add(oper);
        return new Operation(name, opArg);
    }
    
    /**
     * Try to make a new compound from two components. Called by the inference
     * rules.
     *
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Operation make(String op, ArrayList<Term> arg, Memory memory) {
        Operator oper = memory.getOperator(op);
        if (oper == null) {
            return null;
        }
        String name = makeName(op, arg, memory);
        Term t = memory.nameToTerm(name);
        if (t != null) {
            return (Operation) t;
        }
        ArrayList<Term> opArg = new ArrayList<>();
        Term self = memory.nameToTerm(Symbols.SELF);
        arg.add(self);
        Term list = Product.make(arg, memory);
        opArg.add(list);
        opArg.add(oper);
        return new Operation(name, opArg);
    }

    public static String makeName(final String op, ArrayList<Term> arg, Memory memory) {
        final StringBuilder nameBuilder = new StringBuilder(16 /* estimate */)
                .append(Symbols.COMPOUND_TERM_OPENER).append(op);
        for (int i = 0; i < arg.size()-1; i++) { 
            nameBuilder.append(Symbols.ARGUMENT_SEPARATOR);
            nameBuilder.append(arg.get(i).getName());
        }
        nameBuilder.append(Symbols.COMPOUND_TERM_CLOSER);
        return nameBuilder.toString();
    }
    
    public Operator getOperator() {
        return (Operator) getPredicate();
    }
    
    public ArrayList<Term> getArguments() {
        return ((Product) getSubject()).getComponents();
    }
}
