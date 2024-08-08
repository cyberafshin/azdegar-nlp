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
public enum SentenceType {
    None,
    Affirmative,
    Interrogative,
    Imperative,                 // 𝑷𝒍𝒆𝒂𝒔𝒆 𝒍𝒐𝒘𝒆𝒓 𝒚𝒐𝒖𝒓 𝒗𝒐𝒊𝒄𝒆.
    Subjunctive,
    InterrogativeSubjunctive,   // 𝑾𝒉𝒚 𝒔𝒉𝒐𝒖𝒍𝒅 𝒚𝒐𝒖 𝒃𝒆 𝒊𝒏𝒕𝒆𝒓𝒆𝒔𝒕𝒆𝒅 𝒊𝒏 𝒍𝒐𝒈𝒊𝒄?
    Exclamatory                 // 𝑾𝒉𝒂𝒕 𝒂 𝒕𝒊𝒓𝒊𝒏𝒈 𝒅𝒂𝒚!
}



/*

    Whom did you give the book to? (formal)
    To whom did you give the book? (very formal)

*/