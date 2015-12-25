/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package nars.process;

import nars.NAR;
import nars.UtilityFunctions;
import nars.bag.BagBudget;
import nars.budget.Budget;
import nars.budget.UnitBudget;
import nars.concept.Concept;
import nars.nal.LocalRules;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.Terms;
import nars.truth.Truth;

import java.util.function.Consumer;

import static nars.budget.BudgetFunctions.truthToQuality;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
public abstract class ConceptProcess extends AbstractPremise {



    protected final BagBudget<Task> taskLink;
    protected final Concept concept;

    private Task currentBelief = null;
    private transient boolean cyclic;

    @Override
    public final Task getTask() {
        return getTaskLink().get();
    }

    public final BagBudget<Task> getTaskLink() {
        return taskLink;
    }

    @Override public final Concept getConcept() {
        return concept;
    }


    public ConceptProcess(NAR nar, Concept concept, BagBudget<Task> taskLink) {
        super(nar);

        this.taskLink = taskLink;
        this.concept = concept;

    }

    @Override
    public String toString() {
        return new StringBuilder().append(getClass().getSimpleName())
                .append('[').append(concept.toString()).append(':').append(taskLink).append(']')
                .toString();
    }



//    protected void beforeFinish(final long now) {
//
//        Memory m = nar.memory();
//        m.logic.TASKLINK_FIRE.hit();
//        m.emotion.busy(getTask(), this);
//
//    }

//    @Override
//    final protected Collection<Task> afterDerive(Collection<Task> c) {
//
//        final long now = nar.time();
//
//        beforeFinish(now);
//
//        return c;
//    }

    @Override public final void updateBelief(Task nextBelief) {
        if (nextBelief!=currentBelief) {
            currentBelief = nextBelief;
            cyclic = (nextBelief != null) && LocalRules.overlapping(getTask(), nextBelief);
        }
    }


    @Override
    public final Task getBelief() {
        return currentBelief;
    }

    @Override
    public final boolean isCyclic() {
        return cyclic;
    }

    public static int firePremises(Concept concept, BagBudget<Task>[] tasks, BagBudget<Termed>[] terms, Consumer<ConceptProcess> proc, NAR nar) {

        int total = 0;

        for (BagBudget<Task> taskLink : tasks) {
            if (taskLink == null) break;

            for (BagBudget<Termed> termLink : terms) {
                if (termLink == null) break;

                if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.get().term(), termLink.get().term()))
                    continue;

                total+= ConceptTaskTermLinkProcess.fireAll(
                    nar, concept, taskLink, termLink, proc);
            }
        }

        return total;
    }


    /* ----- Task derivation in LocalRules and SyllogisticRules ----- */
    /**
     * Forward logic result and adjustment
     *
     * @param truth The truth value of the conclusion
     * @return The budget value of the conclusion
     */
    public static Budget forward(Truth truth, ConceptProcess nal) {
        return budgetInference(truthToQuality(truth), 1, nal);
    }

    /**
     * Backward logic result and adjustment, stronger case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param nal Reference to the memory
     * @return The budget value of the conclusion
     */
    public static Budget backward(Truth truth, ConceptProcess nal) {
        return budgetInference(truthToQuality(truth), 1, nal);
    }

    /**
     * Backward logic result and adjustment, weaker case
     *
     * @param truth The truth value of the belief deriving the conclusion
     * @param nal Reference to the memory
     * @return The budget value of the conclusion
     */
    public static Budget backwardWeak(Truth truth, ConceptProcess nal) {
        return budgetInference(UtilityFunctions.w2c(1) * truthToQuality(truth), 1, nal);
    }

    /* ----- Task derivation in CompositionalRules and StructuralRules ----- */
    /**
     * Forward logic with CompoundTerm conclusion
     *
     * @param truth The truth value of the conclusion
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static Budget compoundForward(Truth truth, Term content, ConceptProcess nal) {
        return compoundForward(new UnitBudget(), truth, content, nal);
    }

    public static Budget compoundForward(Budget target, Truth truth, Term content, ConceptProcess nal) {
        int complexity = content.complexity();
        return budgetInference(target, truthToQuality(truth), complexity, nal);
    }



    /**
     * Backward logic with CompoundTerm conclusion, stronger case
     *
     * @param content The content of the conclusion
     * @return The budget of the conclusion
     */
    public static Budget compoundBackward(Term content, ConceptProcess nal) {
        return budgetInference(1.0f, content.complexity(), nal);
    }

    /**
     * Backward logic with CompoundTerm conclusion, weaker case
     *
     * @param content The content of the conclusion
     * @param nal Reference to the memory
     * @return The budget of the conclusion
     */
    public static Budget compoundBackwardWeak(Term content, ConceptProcess nal) {
        return budgetInference(UtilityFunctions.w2c(1), content.complexity(), nal);
    }

    static Budget budgetInference(float qual, int complexity, ConceptProcess nal) {
        return budgetInference(new UnitBudget(), qual, complexity, nal );
    }

    /**
     * Common processing for all logic step
     *
     * @param qual Quality of the logic
     * @param complexity Syntactic complexity of the conclusion
     * @param nal Reference to the memory
     * @return Budget of the conclusion task
     */
    static Budget budgetInference(Budget target, float qual, int complexity, ConceptProcess nal) {
        float complexityFactor = complexity > 1 ?

                // sqrt factor (experimental)
                // (float) (1f / Math.sqrt(Math.max(1, complexity))) //experimental, reduces dur and qua by sqrt of complexity (more slowly)

                // linear factor (original)
                (1.0f / Math.max(1, complexity))

                : 1.0f;

        return budgetInference(target, qual, complexityFactor, nal);
    }

    static Budget budgetInference(Budget target, float qual, float complexityFactor, ConceptProcess nal) {

        BagBudget<Task> taskLink =
                nal instanceof ConceptProcess ? nal.getTaskLink() : null;

        Budget t =
                (taskLink !=null) ? taskLink :  nal.getTask().getBudget();


        float priority = t.getPriority();
        float durability = t.getDurability() * complexityFactor;
        float quality = qual * complexityFactor;

        BagBudget<Termed> termLink = nal.getTermLink();
        if (termLink!=null) {
            priority = UtilityFunctions.or(priority, termLink.getPriority());
            durability = UtilityFunctions.and(durability, termLink.getDurability()); //originaly was 'AND'
            float targetActivation = termLink.getPriority();
            if (targetActivation >= 0) {
                termLink.orPriority(UtilityFunctions.or(quality, targetActivation));
                termLink.orDurability(quality);
            }
        }

        return target.budget(priority, durability, quality);


        /* ORIGINAL: https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/inference/BudgetFunctions.java
            Item t = memory.currentTaskLink;
            if (t == null) {
                t = memory.currentTask;
            }
            float priority = t.getPriority();
            float durability = t.getDurability() / complexity;
            float quality = qual / complexity;
            TermLink termLink = memory.currentBeliefLink;
            if (termLink != null) {
                priority = or(priority, termLink.getPriority());
                durability = and(durability, termLink.getDurability());
                float targetActivation = memory.getConceptActivation(termLink.getTarget());
                termLink.incPriority(or(quality, targetActivation));
                termLink.incDurability(quality);
            }
            return new BudgetValue(priority, durability, quality);
         */
    }


    //    /** supplies at most 1 premise containing the pair of next tasklink and termlink into a premise */
//    public static Stream<Task> nextPremise(NAR nar, final Concept concept, float taskLinkForgetDurations, Function<ConceptProcess,Stream<Task>> proc) {
//
//        TaskLink taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, nar.memory());
//        if (taskLink == null) return Stream.empty();
//
//        TermLink termLink = concept.getTermLinks().forgetNext(nar.memory().termLinkForgetDurations, nar.memory());
//        if (termLink == null) return Stream.empty();
//
//
//        return proc.apply(premise(nar, concept, taskLink, termLink));
//
//    }

//    public static ConceptProcess premise(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink) {
////        if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
////            return null;
//
////        if (taskLink.isDeleted())
////            throw new RuntimeException("tasklink null"); //bag should not have returned this
//
//    }



//    public abstract Stream<Task> derive(final Deriver p);

//    public static void forEachPremise(NAR nar, @Nullable final Concept concept, @Nullable TaskLink taskLink, int termLinks, float taskLinkForgetDurations, Consumer<ConceptProcess> proc) {
//        if (concept == null) return;
//
//        concept.updateLinks();
//
//        if (taskLink == null) {
//            taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, concept.getMemory());
//            if (taskLink == null)
//                return;
//        }
//
//
//
//
//        proc.accept( new ConceptTaskLinkProcess(nar, concept, taskLink) );
//
//        if ((termLinks > 0) && (taskLink.type!=TermLink.TRANSFORM))
//            ConceptProcess.forEachPremise(nar, concept, taskLink,
//                    termLinks,
//                    proc
//            );
//    }

//    /** generates a set of termlink processes by sampling
//     * from a concept's TermLink bag
//     * @return how many processes generated
//     * */
//    public static int forEachPremise(NAR nar, Concept concept, TaskLink t, final int termlinksToReason, Consumer<ConceptProcess> proc) {
//
//        int numTermLinks = concept.getTermLinks().size();
//        if (numTermLinks == 0)
//            return 0;
//
//        TermLink[] termlinks = new TermLink[termlinksToReason];
//
//        //int remainingProcesses = Math.min(termlinksToReason, numTermLinks);
//
//        //while (remainingProcesses > 0) {
//
//            Arrays.fill(termlinks, null);
//
//            concept.getPremiseGenerator().nextTermLinks(concept, t, termlinks);
//
//            int created = 0;
//            for (TermLink tl : termlinks) {
//                if (tl == null) break;
//
//                proc.accept(
//                    new ConceptTaskTermLinkProcess(nar, concept, t, tl)
//                );
//                created++;
//            }
//
//
//          //  remainingProcesses--;
//
//
//        //}
//
//        /*if (remainingProcesses == 0) {
//            System.err.println(now + ": " + currentConcept + ": " + remainingProcesses + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
//                    //+ currentConcept.getTermLinks().values()
//            );
//            //currentConcept.taskLinks.printAll(System.out);
//        }*/
//
//        return created;
//
//    }

//    /** override-able filter for derivations which can be applied
//     * once the term and the truth value are known */
//    public boolean validJudgment(Term derivedTerm, Truth truth) {
//        return true;
//    }
//
//    /** override-able filter for derivations which can be applied
//     * once the term and the truth value are known */
//    public boolean validGoal(Term derivedTerm, Truth truth) {
//        return true;
//    }

}
