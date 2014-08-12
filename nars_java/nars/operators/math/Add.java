/*
 * Copyright (C) 2014 peiwang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.operators.math;

import java.util.ArrayList;
import nars.entity.*;
import nars.language.*;
import nars.operators.Operator;
import nars.storage.Memory;

/**
 * Count the number of elements in a set
 */
public class Add extends Operator {

    public Add() {
        super("^add");
    }

    /**
     * To add two numbers and get the sum
     *
     * @param args Arguments, two numbers and a variable
     * @param memory The memory in which the operation is executed
     * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(ArrayList<Term> args, Memory memory) {
        if (args.size() != 4) {
            return null;
        }
        try {
            int n1 = Integer.parseInt(args.get(0).getName());
            int n2 = Integer.parseInt(args.get(1).getName());
            Term numberTerm = new Term(n1 + n2 + "");
            args.set(2, numberTerm);
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
