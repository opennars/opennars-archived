//package nars.operator.math;
//
//import nars.io.Texts;
//import nars.logic.entity.Term;
//import nars.logic.nal1.Inheritance;
//import nars.logic.nal4.Product;
//import nars.logic.nal8.TermFunction;
//import org.encog.ml.prg.EncogProgram;
//import org.encog.ml.prg.EncogProgramContext;
//import org.encog.ml.prg.ProgramNode;
//import org.encog.ml.prg.expvalue.ExpressionValue;
//import org.encog.ml.tree.TreeNode;
//
//import java.util.List;
//
//import static nars.io.Texts.unescape;
//
///**
// * Parses an expression string to terms
// * @see https://github.com/encog/encog-java-core/blob/master/src/test/java/org/encog/ml/prg/TestProgramClone.java
// */
//public class MathExpression  extends TermFunction {
//
//    static EncogProgramContext context;
//
//
//    public MathExpression() {
//        super("^math");
//    }
//
//    final static String requireMessage = "Requires 1 string argument";
//
//    final static Term exp = Term.apply("math");
//
//
//    @Override
//    public Term function(Term[] x) {
//
//        //TODO this may not be thread-safe, this block may need synchronized:
//        synchronized (context) {
//            if (context == null) {
//                context = new EncogProgramContext();
//                context.loadAllFunctions();
//            }
//        }
//
//        if (x.length!=1) {
//            throw new RuntimeException(requireMessage);
//        }
//
//        Term content = x[0];
//        if (content.getClass()!=Term.class) {
//            throw new RuntimeException(requireMessage);
//        }
//
//        String expstring = unescape(content.name()).toString();
//        if (expstring.startsWith("\""))
//            expstring = expstring.substring(1, expstring.length()-1);
//
//        EncogProgram p = context.createProgram(expstring);
//
//        return getTerm(p.getRootNode());
//    }
//
//
//    public static Term getTerm(TreeNode node) {
//
//        CharSequence name =
//                    node instanceof ProgramNode ?
//                    ("\"" + Texts.escape(((ProgramNode)node).getName()) + '\"'):
//                    node.getClass().getSimpleName();
//
//
//        List<TreeNode> children = node.getChildNodes();
//
//
//        ExpressionValue[] data;
//
//        ProgramNode p = (ProgramNode)node;
//        data = p.getData();
//        if ((children == null) || (children.isEmpty())) {
//            if ((data == null) || (data.length == 0) || (p.isVariable())) {
//                if (p.isVariable()) {
//                    long idx = data[0].toIntValue();
//                    String varname = p.getOwner().getVariables().getVariableName((int)idx);
//                    return Term.apply(varname);
//                }
//                return Term.apply(name);
//            }
//            else
//                return getTerms(data);
//        }
//
//        if ((data!=null) && (data.length > 0))
//            return Inheritance.make(new Product(getTerms(children), getTerms(data)), Term.apply(name));
//        else
//            return Inheritance.make(getTerms(children), Term.apply(name));
//    }
//
//    public static Term getTerms(List<TreeNode> children) {
//
//        if (children.size() == 1)
//            return getTerm(children.apply(0));
//
//        Term[] c = new Term[children.size()];
//        int j = 0;
//        for (TreeNode t : children) {
//            c[j++] = getTerm(t);
//        }
//
//        return new Product(c);
//    }
//
//    public static Term getTerms(ExpressionValue[] data) {
//
//        if (data.length == 1)
//            return getTerm(data[0]);
//
//        Term[] c = new Term[data.length];
//        int j = 0;
//        for (ExpressionValue t : data) {
//            c[j++] = getTerm(t);
//        }
//
//        return new Product(c);
//    }
//
//    public static Term getTerm(ExpressionValue t) {
//        return Inheritance.make(
//                Term.apply(Texts.escape(t.toStringValue())),
//                Term.apply(t.getExpressionType().toString()));
//    }
//
//}
