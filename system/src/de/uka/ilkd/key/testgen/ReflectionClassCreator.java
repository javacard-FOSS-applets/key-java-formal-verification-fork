package de.uka.ilkd.key.testgen;

import java.util.HashSet;
import de.uka.ilkd.key.core.Main;
import de.uka.ilkd.key.logic.sort.Sort;

/** Creates the RFL.java file, that provides setter and getter methods using the reflection API
 *  as well as object creation functions based on the objenesis library.
 * @author mbender
 * @author gladisch
 */
public class ReflectionClassCreator {
   /**
    * Constant for the line break which is used by the operating system.
    * <p>
    * <b>Do not use {@code \n}!</b>
    */
   public static final String NEW_LINE = System.getProperty("line.separator");

    public static final String NAME_OF_CLASS = "RFL";

    public static final String ARRAY = "_ARRAY_";
    public static final String SET_PREFIX = "_set_";
    public static final String GET_PREFIX = "_get_";

    // setter and getter methods will be created for these types.
    private static final String[] PRIMITIVE_TYPES = { "int", "long", "byte",
	    "char", "boolean", "float", "double" };

    // Default values for primitive types
    private static final String[] PRIM_TYP_DEF_VAL = { "0", "0", "0", "' '",
	    "false", "0", "0" };
    
    private HashSet<Sort> usedObjectSorts;
    private HashSet<String> usedObjectSortsStrings;
    
    public ReflectionClassCreator(){
    	usedObjectSorts = new HashSet<Sort>();
    	usedObjectSortsStrings = new HashSet<String>();
    }

	/** Creates the RFL.java file, that provides setter and getter methods using the reflection API
	 *  as well as object creation functions based on the objenesis library.
	 */    
	public StringBuffer createClass(boolean staticClass) {
	    // final HashSet<String> sorts = sortsToString(nonGhostVars);
	    final HashSet<String> sorts = sortsToString();
	    final StringBuffer result = new StringBuffer();
	    result.append(classDecl(staticClass));
	    result.append(ghostMapDecls(true));
	    result.append(staticInitializer(true));
	    result.append(instanceMethod());
	    result.append(instances(sorts));
	    result.append(getterAndSetter(sorts));
	    result.append(footer());
	    assert checkBraces(result) : "ReflectionClassCreator.createClass(): Problem: the number of opening and closing braces of the generated RFL file is not equal!";
	    return result;
	}
	
	/** Constructors, setter-, getter-methods will be created for the added sorts.  */
	public void addSort(Sort s){
		usedObjectSorts.add(s);
	}
	
	/** Constructors, setter-, getter-methods will be created for the added sorts.  */
	public void addSort(String s){
		usedObjectSortsStrings.add(s);
	}
	
	/** @return String representations for all non primitive types */
	private HashSet<String> sortsToString() {
	final HashSet<String> result = new HashSet<String>();
	for (final Sort var : usedObjectSorts) {
	    String sort = var.toString();
	    // We only want Object-Types
	    if (" jbyte jint jlong jfloat jdouble jboolean jchar ".indexOf(" " + sort + " ") == -1) {
			if (" jbyte[] jint[] jlong[] jfloat[] jdouble[] jboolean[] jchar[] " .indexOf(" " + sort + " ") != -1) {
			    sort = sort.substring(1);
			}
			if(!isPrimitiveType(sort)){
				result.add(sort);
			}
	    }
	}
	for (String sort : usedObjectSortsStrings) {
	    // We only want Object-Types
	    if (" jbyte jint jlong jfloat jdouble jboolean jchar ".indexOf(" " + sort + " ") == -1) {
			if (" jbyte[] jint[] jlong[] jfloat[] jdouble[] jboolean[] jchar[] " .indexOf(" " + sort + " ") != -1) {
			    sort = sort.substring(1);
			}
			if(!isPrimitiveType(sort)){
				result.add(sort);
			}
	    }
	}
	return result;
	}
	
	/** @return Beginning of the RFL class */
	private StringBuffer classDecl(boolean staticClass) {
	    final StringBuffer r = new StringBuffer();
	    r.append(NEW_LINE);
	    r.append("// This file was generated by KeY Version "+Main.VERSION+" (www.key-project.org)." + NEW_LINE + NEW_LINE +
	          "/** This class enables the test suite to read and write protected and private" + NEW_LINE + 
	    		 " * fields of other classes. It can also simulate ghost fields using a hashmap." + NEW_LINE +
	    		 " * Ghostfields are implicit fields that exist in the specification but not in the" + NEW_LINE +
	    		 " * actual Java class. Futhermore, this class also enables to create an object of " + NEW_LINE +
	    		 " * any class even if it has no default constructor. To create objects the " + NEW_LINE +
	    		 " * the objenesis library is required and must be provided when compiling and" + NEW_LINE +
	    		 " * executing the test suite. " + NEW_LINE);
	    r.append(" * @see http://docs.oracle.com/javase/tutorial/reflect/member/ctorInstance.html" + NEW_LINE);
	    r.append(" * @see http://code.google.com/p/objenesis/" + NEW_LINE +
	    		 " * @see http://objenesis.org/" + NEW_LINE);
	    r.append(" * @author gladisch" + NEW_LINE);
	    r.append(" * @author mbender" + NEW_LINE);
	    r.append(" */" + NEW_LINE);
	    r.append("public ");
	    if (staticClass) {
	       r.append("static ");
	    }
       r.append("class " + NAME_OF_CLASS + " {" + NEW_LINE);
	    return r;
	}
	
	/** Writes a  hashmap and a utility method for associating ghost/model fiels with objects.
	 * @param ghostMapActive becomes are runtime flag that determins if the hashmap should be enabled or not.*/
	private StringBuffer ghostMapDecls(boolean ghostMapActive){
	    final StringBuffer r = new StringBuffer();
	    r.append(NEW_LINE);
	    
	    r.append("  private static final String NoSuchFieldExceptionText;");
	    
	    r.append("  public static boolean ghostMapActive;");
	
	    r.append("  public static java.util.HashMap<Integer,Object> ghostModelFields;" + NEW_LINE + NEW_LINE);
	
	    r.append("  public static int getHash(Class<?> c, Object obj, String attr){" + NEW_LINE);
	    r.append("    return c.hashCode() * (obj!=null?obj.hashCode():1) * attr.hashCode();" + NEW_LINE);
	    r.append("  }" + NEW_LINE + NEW_LINE);
	    return r;
	}
	
	/** @return The method that allows to create new instances	 */
	private StringBuffer instanceMethod() {
	    final StringBuffer r = new StringBuffer();
	    r.append(NEW_LINE + NEW_LINE);
	    r.append("  /** The Objenesis library can create instances of classes that have no default constructor. */" + NEW_LINE);
	    r.append("  private static org.objenesis.Objenesis objenesis;" + NEW_LINE + NEW_LINE);
	    r.append("  private static Object newInstance(Class c) throws Exception {" + NEW_LINE);
	    r.append("    Object res=objenesis.newInstance(c);" + NEW_LINE);
	    r.append("    if (res==null)" + NEW_LINE);
	    r.append("      throw new Exception(\"Couldn't create instance of class:\"+c);" + NEW_LINE);
	    r.append("  return res;" + NEW_LINE);
	    r.append("  }" + NEW_LINE);
	    return r;
	}
	
	private StringBuffer staticInitializer(boolean ghostMapActive){
		StringBuffer r = new StringBuffer();
		String tab = "   ";
		r.append(NEW_LINE + NEW_LINE);
		r.append(tab+"static{" + NEW_LINE);
		
		r.append(tab+"objenesis = new org.objenesis.ObjenesisStd();" + NEW_LINE);
		
		r.append(tab+"ghostMapActive = "+ghostMapActive+";" + NEW_LINE);
		
		r.append(tab+"ghostModelFields = new java.util.HashMap<Integer,Object>();" + NEW_LINE);
		
		r.append(tab+"NoSuchFieldExceptionText =" + NEW_LINE);
	    r.append(tab+tab+"  \"This exception occurs when ghost fields or model fields are used in the code or \" +" + NEW_LINE);
	    r.append(tab+tab+"  \"if mock objects are used that have different fields, than the real objects. \" +" + NEW_LINE);
	    r.append(tab+tab+"  \"The tester should extend the handling of such fields in this generated utility class RFL.java.\";" + NEW_LINE);
	    
		r.append("}" + NEW_LINE + NEW_LINE);
		
		return r;		
	}
	
	/**
	 * @param sorts
	 * @return All calls to create objects for the given sorts
	 */
	private StringBuffer instances(final HashSet<String> sorts) {
	    final StringBuffer r = new StringBuffer();
	    r.append(NEW_LINE + "  // ---The methods for object creation---" + NEW_LINE + NEW_LINE);
	    for (final String sort : sorts) {
		r.append(newRef(sort));
	    }
	    r.append(NEW_LINE);
	    return r;
	}
	
	/** * @return The call to create an object of given type */
	private StringBuffer newRef(final String sort) {
	    if (sort.indexOf('[') != -1) {
		return newArray(sort);
	    } else {
		return newInstance(sort);
	    }
	}
	
	/** Takes a string representing a type e.g. "java.lang.Object[]" and returns
	 *  a new name without "." and "[]", e.g. "java_lang_Object_ARRAY_".
	 *  It is used to create correct setter and getter method names.
	 *  This method is also used in Assignment.toString(boolean rfl) to generate the correct method names.
	 */
	public static String cleanTypeName(String s) {
		// WARNING: Make sure this fixed string begins with a SPACE and also
		// ends with a SPACE.
		if (" jbyte jint jlong jfloat jdouble jboolean jchar jbyte[] jint[] jlong[] jfloat[] jdouble[] jboolean[] jchar[] "
		        .indexOf(" " + s + " ") != -1) {
		    s = s.substring(1);
		}
		while (s.indexOf(".") != -1) {
		    s = s.substring(0, s.indexOf(".")) + "_"
			    + s.substring(s.indexOf(".") + 1);
		}
		while (s.indexOf("[]") != -1) {
		    s = s.substring(0, s.indexOf("[]")) + ARRAY
			    + s.substring(s.indexOf("[]") + 2);
		}
		return s;
	}
	
	/**
	 * @param sort
	 * @return The call to create an object of given type
	 */
	private StringBuffer newInstance(final String sort) {
	    final StringBuffer r = new StringBuffer();
	    r.append(NEW_LINE);
	    r.append("  public static " + sort + " new" + cleanTypeName(sort)   + "() throws java.lang.RuntimeException {" + NEW_LINE);
	    r.append("    try{" + NEW_LINE);
	    r.append("      return (" + sort + ")newInstance(" + sort  + ".class);" + NEW_LINE);
	    r.append("    } catch (java.lang.Throwable e) {" + NEW_LINE);
	    r.append("       throw new java.lang.RuntimeException(e);" + NEW_LINE);
	    r.append("    }" + NEW_LINE);
	    r.append("  }" + NEW_LINE);
	    return r;
	}
	
	/**
	 * @param sort
	 * @return The call to create an Array of given type
	 */
	private StringBuffer newArray(final String sort) {
	    final StringBuffer r = new StringBuffer();
	    r.append(NEW_LINE);
	    r.append("  public static " + sort + " new" + cleanTypeName(sort)
		    + "(int dim){" + NEW_LINE);
	    r.append("    return new " + sort.substring(0, sort.length() - 2)
		    + "[dim];" + NEW_LINE);
	    r.append("  }" + NEW_LINE);
	    return r;
	}
	
	private boolean isPrimitiveType(String sort){
		for(String s:PRIMITIVE_TYPES){
			if(s.equals(sort)){
				return true;
			}
		}
		return false;
	}
	
	private StringBuffer getterAndSetter(final HashSet<String> sorts) {
	    final StringBuffer result = new StringBuffer();
	    result
		    .append(NEW_LINE + "  // ---Getter and setter for primitive types---" + NEW_LINE);
	    for (int i = 0; i < 7; i++) {
		result.append(NEW_LINE);
		result.append(declareSetter(PRIMITIVE_TYPES[i], true));
		result.append(declareGetter(PRIMITIVE_TYPES[i],
		        PRIM_TYP_DEF_VAL[i], true));
	    }
	    result.append(NEW_LINE);
	    result
		    .append(NEW_LINE + "  // ---Getter and setter for Reference types---" + NEW_LINE);
	    for (final String sort : sorts) {
		result.append(NEW_LINE);
		result.append(declareSetter(sort, false));
		result.append(declareGetter(sort, "null", false));
	    }
	    return result;
	}
	
	private StringBuffer declareSetter(final String sort, final boolean prim) {
	    final StringBuffer r = new StringBuffer();
	    final String cmd = "      "+(prim ? 
		      "f.set" + Character.toUpperCase(sort.charAt(0)) + sort.substring(1)+ "(obj, val);" + NEW_LINE 
		    : "f.set(obj, val);" + NEW_LINE);
	    r.append(NEW_LINE);
	    r.append("  public static void "+SET_PREFIX + cleanTypeName(sort)
		    + "(Class<?> c, Object obj, String attr, " + sort
		    + " val) throws RuntimeException{" + NEW_LINE);
	    r.append("    try {" + NEW_LINE);
	    r.append("      java.lang.reflect.Field f = c.getDeclaredField(attr);" + NEW_LINE);
	    r.append("      f.setAccessible(true);" + NEW_LINE);
	    r.append(cmd);
	    r.append("    } catch(NoSuchFieldException e) {" + NEW_LINE);
	    r.append("      if(ghostMapActive)" + NEW_LINE);
	    r.append("        ghostModelFields.put(getHash(c,obj,attr), val);" + NEW_LINE);
	    r.append("      else" + NEW_LINE);
	    r.append("        throw new RuntimeException(e.toString() + NoSuchFieldExceptionText);" + NEW_LINE);
	    r.append("    } catch(Exception e) {" + NEW_LINE);
	    r.append("      throw new RuntimeException(e);" + NEW_LINE);
	    r.append("    }" + NEW_LINE);
	    r.append("  }" + NEW_LINE);
	    return r;
	}
	
	private String primToWrapClass(String sort){
	    if(sort.equals("int"))
		return "Integer";
	    else if(sort.equals("char"))
		return "Character";
	    else
		return Character.toUpperCase(sort.charAt(0)) + sort.substring(1);
	}
	
	private StringBuffer declareGetter(final String sort, final String def,
	        final boolean prim) {
	    final StringBuffer r = new StringBuffer();
	    final String cmd = "      "+(prim ? 
		    "return f.get" + Character.toUpperCase(sort.charAt(0)) + sort.substring(1)+ "(obj);" + NEW_LINE 
		  : "return (" + sort + ") f.get(obj);" + NEW_LINE);
	    r.append(NEW_LINE);
	    r.append("  public static "
		            + sort
		            + " "+GET_PREFIX
		            + cleanTypeName(sort)
		            + "(Class<?> c, Object obj, String attr) throws RuntimeException{" + NEW_LINE);
	    r.append("    " + sort + " res = " + def + ";" + NEW_LINE);
	    r.append("    try {" + NEW_LINE);
	    r.append("      java.lang.reflect.Field f = c.getDeclaredField(attr);" + NEW_LINE);
	    r.append("      f.setAccessible(true);" + NEW_LINE);
	    r.append(cmd);
	    r.append("      } catch(NoSuchFieldException e) {" + NEW_LINE);
	    r.append("      return ("+ (prim? primToWrapClass(sort) : sort) +")ghostModelFields.get(getHash(c,obj,attr));" + NEW_LINE);
	    r.append("    } catch(Exception e) {" + NEW_LINE);
	    r.append("      throw new RuntimeException(e);" + NEW_LINE);
	    r.append("    }" + NEW_LINE);
	    r.append("  }" + NEW_LINE);
	    return r;
	}
	
	/**
	 * @return the closing bracket and a newline for the end of the class
	 */
	private String footer() {
	    return "}" + NEW_LINE;
	}
	
	/** Sanity check. Checks if number of opening and closing braces is equal.	*/
	private boolean checkBraces(final StringBuffer buf) {
	    int curly = 0;
	    int round = 0;
	    int edged = 0;
	    for (int i = 0; i < buf.length(); i++) {
		switch (buf.charAt(i)) {
		case '{':
		    curly++;
		    break;
		case '}':
		    curly--;
		    break;
		case '(':
		    round++;
		    break;
		case ')':
		    round--;
		    break;
		case '[':
		    edged++;
		    break;
		case ']':
		    edged--;
		    break;
		}
	    }
	    if(curly == 0 && round == 0 && edged == 0){
	    	return true;
	    }else{
	    	System.out.println("Error braces in RFL.java: curly:"+curly+" round:"+round+" edged:"+edged);
	    	return false;
	    }
	}

}
