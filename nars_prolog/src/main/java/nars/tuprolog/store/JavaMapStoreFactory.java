package nars.tuprolog.store;


import nars.tuprolog.ClauseStore;
import nars.tuprolog.Prolog;
import nars.tuprolog.Struct;
import nars.tuprolog.PTerm;
import nars.tuprolog.lib.JavaLibrary;

import java.util.List;

public class JavaMapStoreFactory //implements ClauseStoreFactory
{
	public ClauseStore buildClause(Prolog prolog, PTerm goal, List<?> varList)
	{
		if (goal instanceof Struct)
		{
			Struct s = (Struct)goal;
			if (s.getName().equals("map_entry") && s.size() == 3)
			{
				try
				{
					JavaLibrary jl = (JavaLibrary)prolog.getLibraries().getLibrary("alice.tuprolog.lib.JavaLibrary");
					PTerm mapTerm = s.getTermX(0).getTerm();
					Object obj = jl.getRegisteredDynamicObject((Struct)mapTerm);
					if (obj instanceof java.util.Map)
						return new MapEntriesStore(prolog, (java.util.Map<?,?>)obj, s.getTermX(1), s.getTermX(2), varList, jl);
				}
				catch (Exception ex) 
				{ 
					throw new RuntimeException(ex); 
				}				
			}
		}
		return null;
	}
}