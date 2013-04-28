

/* First created by JCasGen Sun Apr 28 21:09:30 CEST 2013 */
package ch.epfl.bbp.typesystem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sun Apr 28 21:09:57 CEST 2013
 * XML source: /Volumes/HDD2/ren_data/dev_hdd/bluebrain/2_process/uima_gimli/src/test/java/julie-basic-types.xml
 * @generated */
public class Protein extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Protein.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Protein() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Protein(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Protein(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Protein(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
}

    