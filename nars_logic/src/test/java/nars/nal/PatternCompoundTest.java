package nars.nal;

import nars.term.Term;
import nars.term.compound.GenericCompound;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.auxiliary.AuxiliaryType;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import static java.lang.System.out;
import static nars.$.$;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Created by me on 12/25/15.
 */
public class PatternCompoundTest {

    abstract class SumExample {
        public abstract int calculate();
    }

    static class PreMatchImpl implements Implementation, ByteCodeAppender {


        private final Term pattern;

        public PreMatchImpl(Term pattern) {
            this.pattern = pattern;
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(Target implementationTarget) {
            return this;
        }

        @Override
        public Size apply(MethodVisitor m,
                          Implementation.Context c,
                          MethodDescription instrumentedMethod) {

            out.println("PREMATCH: " + instrumentedMethod + " " + m);
            m.visitCode();
            //new MethodReturn(false).apply(m,c);

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                    //IntegerConstant.forValue(10),
                    IntegerConstant.forValue(1),
                    //IntegerSum.INSTANCE,
                    MethodReturn.INTEGER
            ).apply(m, c);

            m.visitEnd();
            return new Size(operandStackSize.getMaximalSize(),
                    instrumentedMethod.getStackSize());


//            if (!instrumentedMethod.getReturnType().asErasure().represents(int.class)) {
//                throw new IllegalArgumentException(instrumentedMethod + " must return int");
//            }
        }
    }

    @Test
    public void testConstantPreMatch() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Term instance = $("<%1 --> %2>");

        //ClassLoader patternClassLoader = new ClassLoader();
        DynamicType.Unloaded<PatternCompound> wb = new ByteBuddy()
                .subclass(PatternCompound.class)
                .name(new AuxiliaryType.NamingStrategy.SuffixingRandom("x"))

                //call the GenericCompound copy constructor
                .constructor(ElementMatchers.isConstructor().and(ElementMatchers.takesArguments(GenericCompound.class)))
                .intercept(SuperMethodCall.INSTANCE)

                //.defineConstructor(Arrays.<Class<?>>asList(), Visibility.PUBLIC).withoutCode()

                .constructor(ElementMatchers.isDefaultConstructor()).intercept(SuperMethodCall.INSTANCE)
                .method(named("preMatch")).intercept(new PreMatchImpl(instance))


                .make();

        DynamicType.Loaded<PatternCompound> c = wb.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION);

        PatternCompound w = c.getLoaded().getConstructor(GenericCompound.class).newInstance(instance);
        out.println(w + " " + w.getClass());


        new ClassReader(c.getBytes()).accept(new TraceClassVisitor(null, new ASMifier(),
                new PrintWriter(out)), 0);

//        ClassWriter cw = new ClassWriter(0);
//
//        cw.visit(V1_1, ACC_PUBLIC, "Example", null, "java/lang/Object", null);
//
//        Method m = Method.getMethod("void <init> ()");
//        GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
//        mg.loadThis();
//        mg.invokeConstructor(Type.getType(Object.class), m);
//        mg.returnValue();
//        mg.endMethod();
//
//        m = Method.getMethod("void main (String[])");
//        mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
//        mg.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
//        mg.push("Hello world!");
//        mg.invokeVirtual(Type.getType(PrintStream.class),
//                Method.getMethod("void println (String)"));
//        mg.returnValue();
//        mg.endMethod();
//
//        cw.visitEnd();
    }
}