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

package de.uka.ilkd.key.strategy.termfeature;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;

/**
 * this termfeature returns <tt>ZERO</tt> costs if the given term 
 * is non-rigid 
 */
public class IsNonRigidTermFeature extends BinaryTermFeature {

    public static final TermFeature INSTANCE = new IsNonRigidTermFeature();    
    
    private IsNonRigidTermFeature () {}
    
    protected boolean filter(Term term, Services services) {        
        return !term.isRigid();
    }

}