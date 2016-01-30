package nars.buffermodel;

import nars.$;
import nars.Global;
import nars.NAR;
import nars.guifx.NARide;
import nars.nal.nal7.Sequence;
import nars.nar.Default;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.ArrayList;
import java.util.HashMap;

class Exhaustive {
        ArrayList<Task> observed = new ArrayList<>();
        HashMap<ArrayList<Task>, Integer> dic = null;

        public Task getPattern(int memtime) {
                ArrayList<Task> maxpattern = null;
                int maxvote = 0;
                dic = new HashMap<>();
                int MAXSIZE = 10; //max size of pattern to search for
                for (int wid = 1; wid < MAXSIZE; wid++) {
                        System.out.println(String.valueOf(wid));
                        for (int pos = 0; pos < observed.size(); pos++) {
                                if (pos + wid > observed.size()) {
                                        continue;
                                }
                                //boolean equ = true;
                                ArrayList<Task> pattern = new ArrayList<>();
                                for (int i = 0; i < wid; i++) {
                                        pattern.add(observed.get(pos + i)); //0 <= i <= wid-1
                                }

                                //pattern constructed
                                //1. search through all keys of the dic
                                //whether this pattern is already included
                                ArrayList<Task> included = null;
                                for (ArrayList<Task> pat : dic.keySet()) {
                                        if (pat.size() == pattern.size()) {
                                                //ok same size now check elements content equal
                                                for (int u = 0; u < pat.size(); u++) {
                                                        if (pat.get(u).getTerm().equals(pattern.get(u).getTerm())) {
                                                                //content equal, so check whether both positive
                                                                if (u == pat.size() - 1 && pat.get(u).getTruth().getFrequency() > 0.5 && //both positive
                                                                        pattern.get(u).getFrequency() > 0.5) {
                                                                        included = pat;
                                                                } else if (u == pat.size() - 1 && pat.get(u).getTruth().getFrequency() < 0.5 && //both negative
                                                                        pattern.get(u).getFrequency() < 0.5) {
                                                                        included = pat;
                                                                } else {
                                                                        //break if not equal in frequ
                                                                        if (!(pat.get(u).getTruth().getFrequency() > 0.5 && //both positive
                                                                                pattern.get(u).getFrequency() > 0.5) &&
                                                                                !(pat.get(u).getTruth().getFrequency() < 0.5 && //both negative
                                                                                        pattern.get(u).getFrequency() < 0.5)) {
                                                                                break; //not equal based on truth value
                                                                        }
                                                                }
                                                        } else {
                                                                break; //isn't equal
                                                        }
                                                }
                                        }
                                        if (included != null) { //no need to search further we found the pattern
                                                break; //and we only store it once in the key set
                                        }
                                }
                                if (included == null) {
                                        included = new ArrayList<>(); //wasn't found so we need a new key
                                        for (Task t : pattern) {
                                                included.add(t);
                                        }
                                        dic.put(included, 0); //and set the vote for this pattern to 0 by default
                                }
                                dic.put(included, dic.get(included) + included.size());
                                //dic.put(included,dic.get(included) + 1);
                                if (dic.get(included) > maxvote) {
                                        maxvote = dic.get(included);
                                        maxpattern = pattern;
                                }
                        }
                }

                //2. construct truth value as intersection of all events
                Truth truthvalue = null;
                for (Task t : maxpattern) {
                        if (truthvalue == null) {
                                truthvalue = t.getTruth();
                        } else {
                                truthvalue = TruthFunctions.intersection(truthvalue, t.getTruth());
                        }
                }

                //3. construct sequence term

                Term[] subterms = new Term[maxpattern.size()];
                int[] intervals = new int[maxpattern.size() + 1]; //+1 cause there is also a interval
                for (int i = 0; i < maxpattern.size(); i++) {         //at the beginning: i0 a1 i1 a2 i2
                        subterms[i] = maxpattern.get(i).getTerm(); //unnecessary but it is this way currently
                        if (i > 0) {
                                intervals[i] = (int) (maxpattern.get(i).getOccurrenceTime() - maxpattern.get(i - 1).getOccurrenceTime());
                        }
                }

                Compound seq = (Compound) Sequence.makeSequence(subterms, intervals);

                Task result = $.belief(seq, truthvalue).occurr(memtime).budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY);
                return result;
        }

        public void addObservation(Task t) {
                if (!t.isJudgment() || t.isEternal()) {
                        return;
                }
                while (observed.size() > 100) { //max event buffer size
                        observed.remove(0);
                }
                observed.add(t);
        }

        public static void main(String[] args) {
                Exhaustive exh = new Exhaustive();
                NAR StringToTask = new Default();

                Task t1 = StringToTask.inputTask("<a --> A>. :|:"); //to construct task ^^
                exh.addObservation(t1);
                StringToTask.frame();
                Task t2 = StringToTask.inputTask("<b --> B>. :|:"); //to construct task ^^
                exh.addObservation(t2);
                StringToTask.frame();

                Task t3 = StringToTask.inputTask("<a --> A>. :|:"); //to construct task ^^
                exh.addObservation(t3);
                StringToTask.frame();

                Task t4 = StringToTask.inputTask("<b --> B>. :|:"); //to construct task ^^
                exh.addObservation(t4);
                StringToTask.frame();

                Task ret = exh.getPattern(1);
                NAR testnar = new Default();

                NARide.show(testnar.loop(), (i) -> {
                        testnar.input(ret);
                });

        }
}