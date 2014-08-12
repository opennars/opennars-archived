/*
 * Operator.java
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
package nars.operators;

import nars.operators.mental.*;
import java.util.*;

import nars.language.*;
import nars.entity.Task;
import nars.io.Symbols;
import nars.storage.Memory;

/**
 * An individual operator that can be execute by the system, which can be either
 * inside NARS or outside it, in another system or device.
 * <p>
 * This is the only file to modify when registering a new operator into NARS.
 */
public abstract class Operator extends Term {

    protected Operator(String name) {
        super(name);
    }

    /**
     * Required method for every operator, specifying the corresponding
     * operation
     *
     * @param task The task with the arguments to be passed to the operator
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    protected abstract ArrayList<Task> execute(ArrayList<Term> args, Memory memory);

    /**
     * The standard way to carry out an operation, which invokes the execute
     * method defined for the operator, and handles feedback tasks as input
     *
     * @param op The operator to be executed
     * @param args The arguments to be taken by the operator
     * @param memory The memory on which the operation is executed
     */
    public void call(Operator op, ArrayList<Term> args, Memory memory) {
        ArrayList<Task> feedback = op.execute(args, memory);
        Operation operation = Operation.make(op, args, memory);
        memory.executedTask(operation);
//        reportExecution(op, args, memory);
        if (feedback != null) {
            for (Task t : feedback) {
                memory.inputTask(t);
            }
        }
    }

    /**
     * Display a message in the output stream to indicate the reportExecution of
     * an operation
     * <p>
     * @param operation The content of the operation to be executed
     */
//    private void reportExecution(Operator op, ArrayList<Term> args, Memory memory) {
//        Operation operation = Operation.make(op.getName(), args, memory);
//        memory.executedTask(operation);
//        StringBuilder buffer = new StringBuilder(args.size());
//        for (Term t : args) {
//            buffer.append(t).append(",");
//        }
//        // to be redirected to an output channel
//        System.out.println("EXECUTE: " + op + "(" + buffer.toString() + ")");
//
//  }

    /**
     * Register all known operators in the memory
     * <p>
     * The only method to modify when adding a new registered operator into
     * NARS. An operator name should contain at least two characters after '^'.
     *
     * @param memory The memory space in which the operators are registered
     */
    public static void loadDefaultOperators(Memory memory) {
        // create self
        memory.getConcept(new Term(Symbols.SELF));
        // template to be removed later
        memory.registerOperator(new Sample());  
        // task creation
        memory.registerOperator(new Believe());
        memory.registerOperator(new Want());
        memory.registerOperator(new Wonder());
        memory.registerOperator(new Evaluate());
        // concept operations
        memory.registerOperator(new Remind());
        memory.registerOperator(new Consider());
        memory.registerOperator(new Name());
        memory.registerOperator(new Abbreviate());
        memory.registerOperator(new Register());
        // truth-value operations
        memory.registerOperator(new Doubt());
        memory.registerOperator(new Hesitate());
        
        /*
         * feel             // the overall happyness, average solution quality, and predictions
         * busy             // the overall business
        
         * do               // to turn a judgment into a goal (production rule) ??
         *
         * count            // count the number of elements in a set
         * arithmatic       // + - * /
         * comparisons      // < = >
         * inference        // binary inference
         * 
         * observe          // get the most active input (Channel ID: optional?)
         * anticipate       // get input of a certain pattern (Channel ID: optional?)
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)
         */
     }
}
