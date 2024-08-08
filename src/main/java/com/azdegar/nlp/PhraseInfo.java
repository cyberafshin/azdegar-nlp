/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Afshin Pouria
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhraseInfo {

    private String type;
    private int number;
    private int depth;

}
