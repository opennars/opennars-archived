package nars.clojure;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentList;
import clojure.lang.PersistentVector;
import clojure.lang.Var;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Objects;
import nars.core.NAR;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.util.ContinuousBagNARBuilder;

/**
 * //http://clojure.org/special_forms
 *
 * @see https://github.com/projectodd/shimdandy/ allows multiple Clojure
 * runtimes in the same JVM.
 */
public class TestClojure {

    protected static class NALClojure {

        static {
            new clojure.lang.RT();
        }
        
        public final NAR n;
        
        public NALClojure(NAR n) {
            this.n = n;
            
            new TextOutput(n, System.out);
        }


        public String product(Collection p) {
            String s = "(*,";
            int i = 0;
            for (Object o : p) {
                if (o instanceof Collection) {
                    s += product((Collection)o);
                }
                else {
                    s += "\"" + o.toString() + "\"";
                }
                if (i < p.size()-1)
                    s += ",";
                i++;
            }
            s += ")";
            return s;
        }
        
        public String n(Object o) {
            if (o instanceof Collection)
                return product((Collection)o);
            else
                return "\"" +o.toString()+"\"";
        }
        
        public Object read(final String s) {
            //clojure result
            Object cIn = null, c = null;
            try {
                cIn = Clojure.read(s);
                
                //cOut = clojure.lang.Compiler.load(new java.io.StringReader(s));
                c = clojure.lang.Compiler.load(new java.io.StringReader(s));
            }
            catch (ClassCastException cce) {
            }
            catch (Exception e) {
                String ce = e.getClass().getSimpleName();
                n.addInput("(*,clojure_syntax,\""+ s + "\"," + ce + ").");
                e.printStackTrace();                
            }
            
            if (cIn == null) return null;
            if (c == null) { c = cIn; cIn = null; } 
            
            String result = "";
            switch (c.getClass().getSimpleName()) {
                case "Var":
                    Var v = (Var)c;
                    result = ("<" + v.sym.getName() + " --> Var>");
                    result = ("<" + n(v.get()) + " --> " + v.sym.getName() + ">");
                    break;
                case "PersistentList":
                    PersistentList pl = (PersistentList)c;
                    result = ("<" + product(pl) + " --> List>");
                    break;
                case "PersistentVector":
                    PersistentVector pv = (PersistentVector)c;
                    result = ("<" + product(pv) + " --> Vector>");
                    break;
                case "Long":
                case "Integer":
                case "Boolean":
                    result = ("<" + n(c) + " --> " + c.getClass().getSimpleName() + ">");
                    break;
                default:
                    break;
            }
            if (result.length() > 0) {
                if (cIn!=null) {
                    n.addInput("<" + n(cIn) + " ==> " + result + ">. :|:");
                }
                else {
                    n.addInput(result + ". :|:");
                }
            }
            
            //if includeSyntax:
            String cs = Objects.toString(c);
            String cn = c.getClass().getSimpleName();            
            n.addInput("<(*,\""+ s + "\"," + cn + ",\"" + cs + "\") --> clojure_syntax>.");                 
            n.finish(1);
            
            
            return c;
        }
        
        public static IFn var(Object qualifiedName) {
            return Clojure.var(qualifiedName);
        }

        public static IFn var(Object ns, Object name) {
            return Clojure.var(ns, name);
        }
        
        public void repl() throws IOException {
            
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("> ");
                String l = br.readLine();
                read(l);
            }
            
            
            
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println(Clojure.read("(a b c)"));

        NAR nar = new ContinuousBagNARBuilder().
                setConceptBagSize(16000). 
                build();
        
        NALClojure n = new NALClojure(nar);

        new NARSwing(n.n);

        n.repl();
        
    }
}
