// This file is part of KeY - Integrated Deductive Software Design
//
// Copyright (C) 2001-2011 Universitaet Karlsruhe (TH), Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
// Copyright (C) 2011-2014 Karlsruhe Institute of Technology, Germany
//                         Technical University Darmstadt, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General
// Public License. See LICENSE.TXT for details.
//

package de.uka.ilkd.key.rule.metaconstruct.arith;

import java.math.BigInteger;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.expression.literal.IntLiteral;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.AbstractTermTransformer;
import de.uka.ilkd.key.rule.inst.SVInstantiations;



public final class MetaPow extends AbstractTermTransformer {

    public MetaPow() {
	super(new Name("#pow"), 2);
    }


    /** 
     *  checks whether the result is consistent with the axiom div_axiom 
     */
    private boolean checkResult(BigInteger a, BigInteger b, BigInteger result) {	

	//    (gt(b,0) -> (leq(0,sub(a,mul(result,b))) & lt(sub(a,mul(result,b)),b))  ) 
	if ( b.compareTo(BigInteger.ZERO) > 0 )
	    return (( BigInteger.ZERO.compareTo(a.subtract(result.multiply(b))) <= 0 ) && 
		    ( a.subtract(result.multiply(b)).compareTo(b) < 0));

	//    ( lt(b,0) -> (leq(0,sub(a,mul(result,b))) & lt(sub(a,mul(result,b)),neg(b)))  ) 
	if ( b.compareTo(BigInteger.ZERO) < 0 )
	    return (( BigInteger.ZERO.compareTo(a.subtract(result.multiply(b))) <= 0 ) && 
		    ( a.subtract(result.multiply(b)).compareTo(b.negate()) < 0));
	
	return false;
    }


    /** calculates the resulting term. */
    public Term transform(Term term, SVInstantiations svInst, Services services) {
    	
    	
    	Term arg1 = term.sub(0);
    	Term arg2 = term.sub(1);
    	BigInteger bigIntArg1;
    	BigInteger bigIntArg2;

    	bigIntArg1 = new
    			BigInteger(convertToDecimalString(arg1, services));
    	bigIntArg2 = new
    			BigInteger(convertToDecimalString(arg2, services));
    	
    	
    	if (bigIntArg2.compareTo(BigInteger.ZERO) <= -1 || bigIntArg2.compareTo(MetaShift.INT_MAX_VALUE) > 1) {
    		return term;
    	}
    	
    	BigInteger result = bigIntArg1.pow(bigIntArg2.intValue());
    	
    	IntLiteral lit = new IntLiteral(result.toString());

    	return services.getTypeConverter().convertToLogicElement(lit);

    }
}