/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.slipnet;

import nars.core.Attention;
import nars.core.Memory;
import nars.core.Param;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.Term;
import nars.storage.Bag;

/**
 *
 * @author patrick.hammer
 */
public class SlipNet extends Memory {

    public SlipNet(Param param, Attention concepts, Bag<Task<Term>, Sentence<Term>> novelTasks) {
        super(param, concepts, novelTasks);
    }
    
}
