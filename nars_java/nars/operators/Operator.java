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
import nars.operators.math.*;
import java.util.*;

import nars.language.*;
import nars.entity.Task;
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
     * @param args Arguments of the operation, both input (constant) and output (variable)
     * @param memory The memory to work on
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    protected abstract ArrayList<Task> execute(ArrayList<Term> args, Memory memory);

    /**
     * Register the mental operators in the memory
     * <p>
     * @return The operator table with preloaded content
     */
    public static HashMap<String, Operator> loadOperators() {
        HashMap<String, Operator> table = new HashMap<>(20);
        // task creation
        registerOperator(table, new Believe());
        registerOperator(table, new Want());
        registerOperator(table, new Wonder());
        registerOperator(table, new Evaluate());
        // concept operations
        registerOperator(table, new Remind());
        registerOperator(table, new Consider());
        registerOperator(table, new Name());
        registerOperator(table, new Abbreviate());
        registerOperator(table, new Register());
        // truth-value operations
        registerOperator(table, new Doubt());
        registerOperator(table, new Hesitate());
        // feeling operations
        registerOperator(table, new FeelHappy());
        registerOperator(table, new FeelBusy());   
        /* 
         *          I/O operations under consideration
         * observe          // get the most active input (Channel ID: optional?)
         * anticipate       // get the input matching a given statement with variables (Channel ID: optional?)
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)
         */
        loadOptionalOperators(table);
        return table;
     }
    
    /**
     * Register optional operators in the memory
     * <p>
     * The only method to modify when adding a new registered operator into NARS.
     *
     * @param table The operator table
     */
    public static void loadOptionalOperators(HashMap<String, Operator> table) {
        // math operations
        registerOperator(table, new Count());
        registerOperator(table, new Add());
     }
    
    private static void registerOperator(HashMap<String, Operator> table, Operator op) {
        table.put(op.getName(), op);
    }

    /**
     * The standard way to carry out an operation, which invokes the execute
     * method defined for the operator, and handles feedback tasks as input
     *
     * @param op The operator to be executed
     * @param args The arguments to be taken by the operator
     * @param memory The memory on which the operation is executed
     */
    public void call(Operator op, ArrayList<Term> args, Memory memory) {
        ArrayList<Task> feedback = op.execute(args, memory); // to identify failed operation?
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
}
