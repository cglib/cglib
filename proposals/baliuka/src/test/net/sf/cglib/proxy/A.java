package net.sf.cglib.proxy;





public class A {

	private Long id;

	private String name;
        
      	private String privateName;

        /** Holds value of property intP. */
        private int intP;        

        /** Holds value of property longP. */
        private long longP;        

        /** Holds value of property booleanP. */
        private boolean booleanP;
        
        /** Holds value of property charP. */
        private char charP;
        
        /** Holds value of property byteP. */
        private byte byteP;
        
        /** Holds value of property shortP. */
        private short shortP;
        
        /** Holds value of property floatP. */
        private float floatP;
        
        /** Holds value of property doubleP. */
        private double doubleP;
        
        /** Holds value of property stringP. */
        private String stringP;
        
	/**

	 * Returns the id.

	 * @return Long

	 */

	public Long getId() {

		return id;

	}



	/**

	 * Returns the name.

	 * @return String

	 */

	public String getName() {

		return name;

	}



	/**

	 * Sets the id.

	 * @param id The id to set

	 */

	public void setId(Long id) {

		this.id = id;

	}



	/**

	 * Sets the name.

	 * @param name The name to set

	 */

	public void setName(String name) {

		this.name = name;

	}

   protected Object writeReplace() throws java.io.ObjectStreamException {
     return null;
   }

   	 String getPrivateName() {

		return privateName;

	}

 	void setPrivateName(String name) {

		this.privateName = name;

	}

    
   /** Getter for property intP.
    * @return Value of property intP.
    */
   public int getIntP() {
       return this.intP;
   }
   
   /** Setter for property intP.
    * @param intP New value of property intP.
    */
   public void setIntP(int intP) {
       this.intP = intP;
   }
   
   /** Getter for property longP.
    * @return Value of property longP.
    */
   public long getLongP() {
       return this.longP;
   }
   
   /** Setter for property longP.
    * @param longP New value of property longP.
    */
   public void setLongP(long longP) {
       this.longP = longP;
   }
   
   /** Getter for property booleanP.
    * @return Value of property booleanP.
    */
   public boolean isBooleanP() {
       return this.booleanP;
   }
   
   /** Setter for property booleanP.
    * @param booleanP New value of property booleanP.
    */
   public void setBooleanP(boolean booleanP) {
       this.booleanP = booleanP;
   }
   
   /** Getter for property charP.
    * @return Value of property charP.
    */
   public char getCharP() {
       return this.charP;
   }
   
   /** Setter for property charP.
    * @param charP New value of property charP.
    */
   public void setCharP(char charP) {
       this.charP = charP;
   }
   
   /** Getter for property byteP.
    * @return Value of property byteP.
    */
   public byte getByteP() {
       return this.byteP;
   }
   
   /** Setter for property byteP.
    * @param byteP New value of property byteP.
    */
   public void setByteP(byte byteP) {
       this.byteP = byteP;
   }
   
   /** Getter for property shortP.
    * @return Value of property shortP.
    */
   public short getShortP() {
       return this.shortP;
   }
   
   /** Setter for property shortP.
    * @param shortP New value of property shortP.
    */
   public void setShortP(short shortP) {
       this.shortP = shortP;
   }
   
   /** Getter for property floatP.
    * @return Value of property floatP.
    */
   public float getFloatP() {
       return this.floatP;
   }
   
   /** Setter for property floatP.
    * @param floatP New value of property floatP.
    */
   public void setFloatP(float floatP) {
       this.floatP = floatP;
   }
   
   /** Getter for property doubleP.
    * @return Value of property doubleP.
    */
   public double getDoubleP() {
       return this.doubleP;
   }
   
   /** Setter for property doubleP.
    * @param doubleP New value of property doubleP.
    */
   public void setDoubleP(double doubleP) {
       this.doubleP = doubleP;
   }
   
   /** Getter for property stringP.
    * @return Value of property stringP.
    */
   public String getStringP() {
       return this.stringP;
   }
   
   /** Setter for property stringP.
    * @param stringP New value of property stringP.
    */
   public void setStringP(String stringP) {
       this.stringP = stringP;
   }
   
}

