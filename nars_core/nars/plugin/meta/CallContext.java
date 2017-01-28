package nars.plugin.meta;

import nars.entity.Concept;

// carries all variables
public class CallContext {
    Concept concept; // only valid for "pseudo-maintainDisappointedAnticipationsIndirected"

    // for ctor chaining
    CallContext setConcept(Concept concept) {
        this.concept = concept;
        return this;
    }
}
