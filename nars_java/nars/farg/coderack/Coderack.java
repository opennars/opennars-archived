/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.coderack;

import nars.language.Term;
import nars.storage.LevelBag;

/**
 *
 * @author patrick.hammer
 */
public class Coderack extends LevelBag<Codelet,Term> {

    public Coderack(int levels, int capacity) {
        super(levels, capacity);
    }

}
