/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp.transform;

import edu.stanford.nlp.ling.CoreLabel;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
@FunctionalInterface
public interface Transformable {

    void check(int index, List<CoreLabel> words);

}
