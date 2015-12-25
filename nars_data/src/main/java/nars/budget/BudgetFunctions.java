/*
 * BudgetFunctions.java
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
package nars.budget;


import nars.UtilityFunctions;
import nars.truth.Truth;


/**
 * Budget functions for resources allocation
 */
public final class BudgetFunctions extends UtilityFunctions {

    /* ----------------------- Belief evaluation ----------------------- */
    /**
     * Determine the quality of a judgment by its truth value alone
     * <p>
     * Mainly decided by confidence, though binary judgment is also preferred
     *
     * @param t The truth value of a judgment
     * @return The quality of the judgment, according to truth value only
     */
    public static float truthToQuality(Truth t) {
//        if (t == null)
//            throw new RuntimeException("truth null");
        float exp = t.getExpectation();
        return Math.max(exp, (1.0f - exp) * 0.75f);
    }


    /**
     * Evaluate the quality of a revision, then de-prioritize the premises
     *
     * @param tTruth The truth value of the judgment in the task
     * @param bTruth The truth value of the belief
     * @param truth The truth value of the conclusion of revision
     * @return The budget for the new task
     */
    public static Budget revise(Truth tTruth, Truth bTruth, Truth truth, Budget tb) {

        float difT = truth.getExpDifAbs(tTruth);

        tb.andPriority(1.0f - difT);
        tb.andDurability(1.0f - difT);

//        boolean feedbackToLinks = (p instanceof ConceptProcess);
//        if (feedbackToLinks) {
//            TaskLink tLink = ((ConceptProcess) p).getTaskLink();
//            tLink.andPriority(1.0f - difT);
//            tLink.andDurability(1.0f - difT);
//            TermLink bLink = p.getTermLink();
//            float difB = truth.getExpDifAbs(bTruth);
//            bLink.andPriority(1.0f - difB);
//            bLink.andDurability(1.0f - difB);
//        }

        float dif = truth.getConfidence() - Math.max(tTruth.getConfidence(), bTruth.getConfidence());
        
        //TODO determine if this is correct
        if (dif < 0) dif = 0;  
        
        
        float priority = UtilityFunctions.or(dif, tb.getPriority());
        float durability = UtilityFunctions.aveAri(dif, tb.getDurability());
        float quality = truthToQuality(truth);
        
        /*
        if (priority < 0) {
            memory.nar.output(ERR.class, 
                    new RuntimeException("BudgetValue.revise resulted in negative priority; set to 0"));
            priority = 0;
        }
        if (durability < 0) {
            memory.nar.output(ERR.class, 
                    new RuntimeException("BudgetValue.revise resulted in negative durability; set to 0; aveAri(dif=" + dif + ", task.getDurability=" + task.getDurability() +") = " + durability));
            durability = 0;
        }
        if (quality < 0) {
            memory.nar.output(ERR.class, 
                    new RuntimeException("BudgetValue.revise resulted in negative quality; set to 0"));
            quality = 0;
        }
        */
        
        return new UnitBudget(priority, durability, quality);
    }

//    /**
//     * Update a belief
//     *
//     * @param task The task containing new belief
//     * @param bTruth Truth value of the previous belief
//     * @return Budget value of the updating task
//     */
//    static Budget update(final Task task, final Truth bTruth) {
//        final Truth tTruth = task.getTruth();
//        final float dif = tTruth.getExpDifAbs(bTruth);
//        final float priority = or(dif, task.getPriority());
//        final float durability = aveAri(dif, task.getDurability());
//        final float quality = truthToQuality(bTruth);
//        return new Budget(priority, durability, quality);
//    }

    /* ----------------------- Links ----------------------- */
    /**
     * Distribute the budget of a task among the links to it
     *
     * @param b The original budget
     * @param factor to scale dur and qua
     * @return Budget value for each tlink
     */
    public static UnitBudget clonePriorityMultiplied(Budget b, float factor) {
        float newPriority = b.getPriority() * factor;
        return new UnitBudget(newPriority, b.getDurability(), b.getQuality());
    }

    
//    /* ----------------------- Concept ----------------------- */
//    /**
//     * Activate a concept by an incoming TaskLink
//     *
//     *
//     * @param factor linear interpolation factor; 1.0: values are applied fully,  0: values are not applied at all
//     * @param receiver The budget receiving the activation
//     * @param amount The budget for the new item
//     */
//    public static void activate(final Budget receiver, final Budget amount, final Activating mode, final float factor) {
//        switch (mode) {
//            /*case Max:
//                receiver.max(amount);
//                break;*/
//
//            case Accum:
//                receiver.accumulate(amount);
//                break;
//
//            case Classic:
//                float priority = or(receiver.getPriority(), amount.getPriority());
//                float durability = aveAri(receiver.getDurability(), amount.getDurability());
//                receiver.setPriority(priority);
//                receiver.setDurability(durability);
//                break;
//
//            case WTF:
//
//                final float currentPriority = receiver.getPriority();
//                final float targetPriority = amount.getPriority();
//                /*receiver.setPriority(
//                        lerp(or(currentPriority, targetPriority),
//                                currentPriority,
//                                factor) );*/
//                float op = or(currentPriority, targetPriority);
//                if (op > currentPriority) op = lerp(op, currentPriority, factor);
//                receiver.setPriority( op );
//
//                final float currentDurability = receiver.getDurability();
//                final float targetDurability = amount.getDurability();
//                receiver.setDurability(
//                        lerp(aveAri(currentDurability, targetDurability),
//                                currentDurability,
//                                factor) );
//
//                //doesnt really change it:
//                //receiver.setQuality( receiver.getQuality() );
//
//                break;
//        }
//
//    }
//
//    /**
//     */
//    public static void activate(final Budget receiver, final Budget amount, Activating mode) {
//        activate(receiver, amount, mode, 1f);
//    }

//    /* ---------------- Bag functions, on all Items ------------------- */
//    /**
//     * Decrease Priority after an item is used, called in Bag.
//     * After a constant time, p should become d*p. Since in this period, the
//     * item is accessed c*p times, each time p-q should multiple d^(1/(c*p)).
//     * The intuitive meaning of the parameter "forgetRate" is: after this number
//     * of times of access, priority 1 will become d, it is a system parameter
//     * adjustable in run time.
//     *
//     * @param budget The previous budget value
//     * @param forgetCycles The budget for the new item
//     * @param relativeThreshold The relative threshold of the bag
//     */
//    @Deprecated public static float forgetIterative(Budget budget, float forgetCycles, float relativeThreshold) {
//        float newPri = budget.getQuality() * relativeThreshold;      // re-scaled quality
//        float dp = budget.getPriority() - newPri;                     // priority above quality
//        if (dp > 0) {
//            newPri += (float) (dp * pow(budget.getDurability(), 1.0f / (forgetCycles * dp)));
//        }    // priority Durability
//        budget.setPriority(newPri);
//        return newPri;
//    }
//
//
//
//    /** forgetting calculation for real-time timing */
//    public static float forgetPeriodic(Budget budget, float forgetPeriod /* cycles */, float minPriorityForgettingCanAffect, long currentTime) {
//
//        float currentPriority = budget.getPriority();
//        long forgetDelta = budget.setLastForgetTime(currentTime);
//        if (forgetDelta == 0) {
//            return currentPriority;
//        }
//
//        minPriorityForgettingCanAffect *= budget.getQuality();
//
//        if (currentPriority < minPriorityForgettingCanAffect) {
//            //priority already below threshold, don't decrease any further
//            return currentPriority;
//        }
//
//        float forgetProportion = forgetDelta / forgetPeriod;
//        if (forgetProportion <= 0) return currentPriority;
//
//        //more durability = slower forgetting; durability near 1.0 means forgetting will happen slowly, near 0.0 means will happen at a max rate
//        forgetProportion *= (1.0f - budget.getDurability());
//
//        float newPriority = forgetProportion > 1.0f ? minPriorityForgettingCanAffect : currentPriority * (1.0f - forgetProportion) + minPriorityForgettingCanAffect * (forgetProportion);
//
//
//        budget.setPriority(newPriority);
//
//        return newPriority;
//
//
//        /*if (forgetDelta > 0)
//            System.out.println("  " + currentPriority + " -> " + budget.getPriority());*/
//
//    }


    /*public final static float abs(final float a, final float b) {
        float c = (a - b);
        return (c >= 0) ? c : -c;
    }*/




}
