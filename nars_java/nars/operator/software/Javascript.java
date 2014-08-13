package nars.operator.software;

import java.util.ArrayList;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import nars.core.Parameters;
import nars.entity.Task;
import nars.io.Texts;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.Operation;
import nars.operator.Operator;
import static nars.operator.Operator.theTask;
import nars.storage.Memory;

/**
 * Executes a Javascript expression
 */
public class Javascript extends Operator {

    final ScriptEngineManager factory = new ScriptEngineManager();
    final ScriptEngine js = factory.getEngineByName("JavaScript");      

    public Javascript() {
        super("^js");
    }

    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        
        js.put("memory", memory);
        //TODO make memory access optional by constructor argument
        //TODO allow access to NAR instance?
        //TODO allow without variable term, for just invoking something

        if (args.length != 2)
            return null;
        
        if (!(args[1] instanceof Variable)){
            //TODO report error
            return null;
        }        
        
        Object result = null;
        
        String input = Texts.unescape(args[0].name()).toString();
        if (input.charAt(0) == '"')
            input = input.substring(1, input.length()-1);                
        try {
            result = js.eval(input);
        } catch (Throwable ex) {            
            result = ex.toString();
        }
        
        memory.output(Javascript.class, input + " | " + result);
        
        String resultName;
        if (result instanceof Number) {
            resultName = String.valueOf(result);
        }
        else {
            resultName = Texts.escape('"' + result.toString() + '"').toString();
        }
        
        Operation oresult = operation.clone();
        oresult.getArguments()[1] = new Term(resultName);
        oresult.rename();

        return theTask( 
                memory.newTask(oresult, '.', 1f, 0.9f, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY) 
        );

    }
    
}
