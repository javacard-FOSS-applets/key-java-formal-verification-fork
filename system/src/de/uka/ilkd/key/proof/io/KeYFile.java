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

package de.uka.ilkd.key.proof.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.parser.KeYLexerF;
import de.uka.ilkd.key.parser.KeYParserF;
import de.uka.ilkd.key.parser.ParserConfig;
import de.uka.ilkd.key.parser.ParserMode;
import de.uka.ilkd.key.proof.init.Includes;
import de.uka.ilkd.key.proof.init.InitConfig;
import de.uka.ilkd.key.proof.init.Profile;
import de.uka.ilkd.key.proof.init.ProofInputException;
import de.uka.ilkd.key.proof.mgt.SpecificationRepository;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.settings.ProofSettings;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.util.ProgressMonitor;
import org.antlr.runtime.RecognitionException;


/** 
 * Represents an input from a .key file producing an environment.
 */
public class KeYFile implements EnvInput {
    
    private final String name;
    
    /** the RuleSource delivering the input stream for the file.
     */
    protected final RuleSource file;
   
    /** the graphical entity to notify on the state of reading.
     */
    protected final ProgressMonitor monitor;
    
    private String javaPath;
    private boolean javaPathAlreadyParsed=false;
    
    private InputStream input;
    
    protected InitConfig initConfig;
    
    private String chooseContract = null;

    private String proofObligation = null;
    
    private final Profile profile;
    
    // when parsing the key file store the classPaths here
    private ImmutableList<String> classPaths;
    
    // when parsing the key file store the boot class path here (or null if none given)
    private String bootClassPath;

    private Includes includes;
    
    //-------------------------------------------------------------------------
    //constructors
    //------------------------------------------------------------------------- 
    
    /** creates a new representation for a given file by indicating a name
     * and a RuleSource representing the physical source of the .key file.
     */
    public KeYFile(String name, 
                   RuleSource file,
                   ProgressMonitor monitor,
                   Profile profile) {
        assert name != null;
        assert file != null;
        assert profile != null;
        this.name = name;
        this.file = file;
        this.monitor = monitor;
        this.profile = profile;
    }

        
    /** creates a new representation for a given file by indicating a name
     * and a file representing the physical source of the .key file.
     */
    public KeYFile(String name, 
                   File file, 
                   ProgressMonitor monitor,
                   Profile profile) {
	this(name, RuleSourceFactory.initRuleFile(file), monitor, profile);
    }
    

    
    //-------------------------------------------------------------------------
    //internal methods
    //-------------------------------------------------------------------------
    
    private KeYParserF createDeclParser(InputStream is) throws IOException {
        return new KeYParserF(ParserMode.DECLARATION,
                             new KeYLexerF(is,
                                          file.toString()),
                             initConfig.getServices(),
                             initConfig.namespaces());
    }


    protected InputStream getNewStream() throws FileNotFoundException {
        close();
        if (!file.isAvailable()) {
            throw new FileNotFoundException("File/Resource " + file + " not found.");  
        } 
        input = file.getNewStream();
        return input;
    }
    
    protected ProofSettings getPreferences() throws ProofInputException {
        if (initConfig.getSettings() == null) {
            return readPreferences();
        } else {
            return initConfig.getSettings();
        }
    }
    
    public ProofSettings readPreferences() throws ProofInputException {
       if (file.isDirectory()) {
          return null;
      }
        KeYParserF problemParser = null;
      try {
            problemParser =
                    new KeYParserF(ParserMode.PROBLEM,
                              new KeYLexerF(getNewStream(), file.toString()));
         problemParser.profile();
         ProofSettings settings = new ProofSettings(ProofSettings.DEFAULT_SETTINGS);
         settings.loadSettingsFromString(problemParser.preferences());
          return settings;                
      } catch (RecognitionException e) {
            // problemParser cannot be null since exception is thrown during parsing.
            String message = problemParser.getErrorMessage(e, problemParser.getTokenNames());
            throw new ProofInputException(message, e);
      } catch (IOException fnfe) {
          throw new ProofInputException(fnfe);
      } catch (de.uka.ilkd.key.util.ExceptionHandlerException ehe) {
          throw new ProofInputException(ehe.getCause().getMessage());
      }
    }
    
    
    //-------------------------------------------------------------------------
    //public interface
    //-------------------------------------------------------------------------
    
    @Override
    public String name() {
        return name;
    }
    
    
    @Override
    public int getNumberOfChars() {
	return file.getNumberOfBytes();
    }
    
    
    @Override
        public void setInitConfig(InitConfig conf) {
        this.initConfig=conf;
    }

    
    @Override
    public Includes readIncludes() throws ProofInputException {
        if (includes == null) {
            KeYParserF problemParser = null;
            try {
                ParserConfig pc = new ParserConfig
                (new Services(getProfile()), 
                        new NamespaceSet());
                // FIXME: there is no exception handler here, thus, when parsing errors are ecountered
                // during collection of includes (it is enough to mispell \include) the error
                // message is very uninformative - ProofInputException without filename, line and column
                // numbers. Somebody please fix that. /Woj
                problemParser = new KeYParserF(ParserMode.PROBLEM,
                        new KeYLexerF(getNewStream(),
                                file.toString()),
                                pc, 
                                pc, 
                                null, 
                                null); 
                problemParser.parseIncludes(); 
                includes = problemParser.getIncludes();
            } catch (RecognitionException e) {
                // problemParser cannot be null since exception is thrown during parsing.
                String message = problemParser.getErrorMessage(e);
                throw new ProofInputException(message, e);
            } catch (IOException e) {
                throw new ProofInputException(e);
            } catch(de.uka.ilkd.key.util.ExceptionHandlerException ehe){
                throw new ProofInputException(ehe);
            }
        }
        return includes;            
    }


    @Override    
    public File readBootClassPath() throws IOException {
        if(!javaPathAlreadyParsed) {
            throw new IllegalStateException("Can access this only after 'readJavaPath' has been called");
        }
        
        if(bootClassPath == null) {
            return null;
        }
        File bootClassPathFile = new File(bootClassPath);
        
        if (!bootClassPathFile.isAbsolute()) {
            String parentDirectory = file.file().getParent();
            bootClassPathFile = new File(parentDirectory, bootClassPath);            
        }
        
        return bootClassPathFile.getCanonicalFile();         
    }
    

    @Override    
    public List<File> readClassPath() {
        if(!javaPathAlreadyParsed) {
            throw new IllegalStateException("Can access this only after 'readJavaPath' has been called");
        }

        String parentDirectory = file.file().getParent();
        List<File> fileList = new ArrayList<File>();
        for (String cp : classPaths) {
            if (cp == null) {
                fileList.add(null);
            } else {
                File f = new File(cp);
                if (!f.isAbsolute()) {
                    f = new File(parentDirectory, cp);
                }
                fileList.add(f);
            }
        }
        return fileList;
    }

    
    @Override    
    public String readJavaPath() throws ProofInputException {
        if (javaPathAlreadyParsed) {
            return javaPath;       
        }
        KeYParserF problemParser = null;
        try {
            problemParser = new KeYParserF(ParserMode.PROBLEM,
                                                    new KeYLexerF(getNewStream(),
                                                                 file.toString()));
            
            problemParser.profile(); // skip profile
            problemParser.preferences(); // skip preferences

            bootClassPath = problemParser.bootClassPath();
            classPaths = problemParser.classPaths();
            javaPath = problemParser.javaSource(); 
            
            if(javaPath != null) {
                File cfile = new File(javaPath);
                if (!cfile.isAbsolute()) { // test relative pathname
                    File parent=file.file().getParentFile();
                    cfile = new File(parent,javaPath).
                    getCanonicalFile().getAbsoluteFile();
                    javaPath = cfile.getAbsolutePath();
                }
                if (!cfile.exists()) {
                    throw new ProofInputException("Declared Java source " 
                            + javaPath + " not found.");
                }                      
            }
            
            javaPathAlreadyParsed = true;
            
            return javaPath;
        } catch (RecognitionException e) {
            // problemParser cannot be null since exception is thrown during parsing.
            String message = problemParser.getErrorMessage(e);
            throw new ProofInputException(message, e);
        } catch (IOException ioe) {
            throw new ProofInputException(ioe);
        } catch(de.uka.ilkd.key.util.ExceptionHandlerException ehe){
            ehe.printStackTrace();
            System.out.println(ehe.getCause().getMessage());
            throw new ProofInputException(ehe.getCause().getMessage());
        }
    }
    
    
    @Override
    public void read() throws ProofInputException {
	if(initConfig == null) {
	    throw new IllegalStateException("KeYFile: InitConfig not set.");
	}
        
        //read .key file
	KeYParserF problemParser = null;
	try {
            Debug.out("Reading KeY file", file);
                   
            final ParserConfig normalConfig 
                    = new ParserConfig(initConfig.getServices(), initConfig.namespaces());                       
            final ParserConfig schemaConfig 
                    = new ParserConfig(initConfig.getServices(), initConfig.namespaces());

            CountingBufferedReader cinp =
                    new CountingBufferedReader
                        (getNewStream(),monitor,getNumberOfChars()/100);
            try {
                problemParser = new KeYParserF(ParserMode.PROBLEM,
                        new KeYLexerF(cinp,
                                file.toString()),
                                schemaConfig, 
                                normalConfig, 
                                initConfig.getTaclet2Builder(), 
                                initConfig.getTaclets()); 
                problemParser.problem(); 
                initConfig.addCategory2DefaultChoices(problemParser.
                        getCategory2Default());
                ImmutableSet<Taclet> st = problemParser.getTaclets();
                initConfig.setTaclets(st);

                SpecificationRepository specRepos 
                = initConfig.getServices().getSpecificationRepository();
                specRepos.addContracts(problemParser.getContracts());
                specRepos.addClassInvariants(problemParser.getInvariants());
                chooseContract = problemParser.getChooseContract();
                proofObligation = problemParser.getProofObligation();
                Debug.out("Read KeY file   ", file);
            } finally {
                cinp.close();
            }
	} catch (RecognitionException e) {
            // problemParser cannot be null since exception is thrown during parsing.
            String message = problemParser.getErrorMessage(e);
            throw new ProofInputException(message, e);
        } catch (IOException io) {
            throw new ProofInputException(io);            
        }
    }

    
    /** reads the sorts declaration of the .key file only, 
     * modifying the sort namespace
     * of the initial configuration 
     */
    public void readSorts() throws ProofInputException {
        KeYParserF p = null;
        try {
            InputStream is = getNewStream();
            try { 
                p=createDeclParser(is);
                p.parseSorts();
                initConfig.addCategory2DefaultChoices(p.getCategory2Default());
            } finally {
                is.close();
            }
	} catch (RecognitionException e) {
	    // p cannot be null here
            throw new ProofInputException(p.getErrorMessage(e), e);
        } catch (IOException io) {
            throw new ProofInputException(io);            
        }
    }
    
    
    /** reads the functions and predicates declared in the .key file only, 
     * modifying the function namespaces of the respective taclet options. 
     */
    public void readFuncAndPred() throws ProofInputException {	
	if(file == null) return;

	KeYParserF p = null;
	try {
            InputStream is = getNewStream();
            try { 
                p=createDeclParser(getNewStream());
                p.parseFuncAndPred();
            } finally {
                is.close();
            }
	} catch (RecognitionException e) {
	    // p cannot be null here
            throw new ProofInputException(p.getErrorMessage(e), e);
        } catch (IOException io) {
            throw new ProofInputException(io);            
        }
    }

    
   /** reads the rules and problems declared in the .key file only, 
     * modifying the set of rules 
     * of the initial configuration 
     */
    public void readRulesAndProblem() throws ProofInputException {
        final ParserConfig schemaConfig = 
	    new ParserConfig(initConfig.getServices(), initConfig.namespaces());
        final ParserConfig normalConfig = 
	    new ParserConfig(initConfig.getServices(), initConfig.namespaces());
        
        KeYParserF problemParser = null;
        try {
            final CountingBufferedReader cinp = new CountingBufferedReader
                    (getNewStream(), monitor, getNumberOfChars()/100);
            try {
                problemParser = new KeYParserF(ParserMode.PROBLEM,
                        new KeYLexerF(cinp,
                                file.toString()),
                                schemaConfig,
                                normalConfig,
                                initConfig.getTaclet2Builder(),
                                initConfig.getTaclets());
                problemParser.parseTacletsAndProblem();
                initConfig.setTaclets(problemParser.getTaclets());
            } finally {
                cinp.close();
            }
	} catch (RecognitionException e) {
	    // problemParser cannot be null here
	    throw new ProofInputException(problemParser.getErrorMessage(e), e);
        } catch (IOException io) {
            throw new ProofInputException(io);            
        }
    }

    
    public void close() {
        try {
            if (input != null) { 
		input.close();
	    }
        } catch(IOException ioe) {
            System.err.println("WARNING: Cannot close stream "+file+"\n"+ioe);
        }
    }
    
    
    public String chooseContract() {
        return chooseContract;
    }
    
    
    public String getProofObligation() {
        return proofObligation;
    }

    
    @Override    
    public String toString() {
	return name() + " " + file.toString();
    }
    
    
    @Override    
    public boolean equals(Object o) {
        if(o == null || o.getClass() != this.getClass()) {
            return false;
        }
        KeYFile kf = (KeYFile) o;
        return kf.file.getExternalForm().equals(file.getExternalForm());

    }

    
    @Override    
    public int hashCode(){
        final String externalForm = file.getExternalForm();
        if (externalForm == null) {
            return -1;
        }
	return externalForm.hashCode();
    }

    @Override
    public Profile getProfile() {
        return profile;
    }
    
    @Override
    public File getInitialFile() {
       return file != null ? file.file() : null;
    }
}