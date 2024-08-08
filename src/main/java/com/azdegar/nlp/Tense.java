/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp;

/**
 *
 * @author Afshin Pouria
 */
public enum Tense {
    Past_Simple,
    Past_Continuous,
    Past_Perfect,
    Past_Perfect_Continuous,
    Present_Simple,
    Present_Continuous,
    Present_Perfect,
    Present_Perfect_Continuous,
    Present_Simple_Imagine,
    Present_Perfect_Imagine,
    Future_Simple,
    Future_Continuous,
    Future_Perfect,
    Future_Perfect_Continuous,
    Future_In_Past,
    Infinitive,
    NONE;

    public String time() {
        return toString().split("_")[0];
    }

    public boolean isPerfect() {
        return toString().contains("_Perfect");
    }

    public boolean isContinuous() {
        return toString().contains("_Continuous");
    }

}
