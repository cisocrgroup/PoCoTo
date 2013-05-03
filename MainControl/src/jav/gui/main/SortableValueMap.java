package jav.gui.main;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *Copyright (c) 2012, IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:

 *Redistributions of source code must retain the above copyright
 *notice, this list of conditions and the following disclaimer.
 *Redistributions in binary form must reproduce the above copyright
 *notice, this list of conditions and the following disclaimer in the
 *documentation and/or other materials provided with the distribution.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This file is part of the ocr-postcorrection tool developed
 * by the IMPACT working group at the Centrum für Informations- und Sprachverarbeitung, University of Munich.
 * For further information and contacts visit http://ocr.cis.uni-muenchen.de/
 * 
 * @author thorsten (thorsten.vobl@googlemail.com)
 */
public class SortableValueMap<K, V extends Comparable<V>>
  extends LinkedHashMap<K, V> {
  public SortableValueMap() { }

  public SortableValueMap( Map<K, V> map ) {
    super( map );
  }

  public void sortByValue() {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( entrySet() );

    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
      @Override
      public int compare( Map.Entry<K, V> entry1, Map.Entry<K, V> entry2 ) {
        return entry1.getValue().compareTo( entry2.getValue() );
      }
    });

    clear();

    for( Map.Entry<K, V> entry : list ) {
      put( entry.getKey(), entry.getValue() );
    }
  }

  private static void print( String text, Map<String, Double> map ) {
    System.out.println( text );

    for( String key : map.keySet() ) {
      System.out.println( "key/value: " + key + "/" + map.get( key ) );
    }
  }

  public static void main( String[] args ) {
    SortableValueMap<String, Double> map =
      new SortableValueMap<String, Double>();

    map.put( "A", 67.5 );
    map.put( "B", 99.5 );
    map.put( "C", 82.4 );
    map.put( "D", 42.0 );

    print( "Unsorted map", map );
    map.sortByValue();
    print( "Sorted map", map );
  }
}