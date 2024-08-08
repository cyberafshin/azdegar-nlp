package com.azdegar.nlp;

import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public interface TypeRepository {

    List<String> find(String label, String pos);
}
