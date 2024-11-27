package antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.jGuru.com
 * Software rights: http://www.antlr.org/RIGHTS.html
 *
 * $Id: //depot/code/org.antlr/release/antlr-2.7.2/antlr/MakeGrammar.java#1 $
 */

import antlr.collections.Stack;
import antlr.collections.impl.LList;
import antlr.collections.impl.Vector;

public class MakeGrammar extends DefineGrammarSymbols {

    protected Stack blocks = new LList(); // track subrules--Stack<BlockContext>
    protected RuleRefElement lastRuleRef;

    protected RuleEndElement ruleEnd;   // used if not nested
    protected RuleBlock ruleBlock;		// points to block of current rule.
    protected int nested = 0;			// nesting inside a subrule
    protected boolean grammarError = false;

    ExceptionSpec currentExceptionSpec = null;

    public MakeGrammar(Tool tool_, String[] args_, LLkAnalyzer analyzer_) {
        super(tool_, args_, analyzer_);
    }

    /** Abort the processing of a grammar (due to syntax errors) */
    public void abortGrammar() {
        String s = "unknown grammar";
        if (grammar != null) {
            s = grammar.getClassName();
        }
        tool.error("aborting grammar '" + s + "' due to errors");
        super.abortGrammar();
    }

    protected void addElementToCurrentAlt(AlternativeElement e) {
        e.enclosingRuleName = ruleBlock.ruleName;
        context().addAlternativeElement(e);
    }

    public void beginAlt(boolean doAutoGen_) {
        super.beginAlt(doAutoGen_);
        Alternative alt = new Alternative();
        alt.setAutoGen(doAutoGen_);
        context().block.addAlternative(alt);
    }

    public void beginChildList() {
        super.beginChildList();
        context().block.addAlternative(new Alternative());
    }

    /** Add an exception group to a rule (currently a no-op) */
    public void beginExceptionGroup() {
        super.beginExceptionGroup();
        if (!(context().block instanceof RuleBlock)) {
            tool.panic("beginExceptionGroup called outside of rule block");
        }
    }

    /** Add an exception spec to an exception group or rule block */
    public void beginExceptionSpec(Token label) {
        // Hack the label string a bit to remove leading/trailing space.
        if (label != null) {
            label.setText(StringUtils.stripFront(StringUtils.stripBack(label.getText(), " \n\r\t"), " \n\r\t"));
        }
        super.beginExceptionSpec(label);
        // Don't check for currentExceptionSpec!=null because syntax errors
        // may leave it set to something.
        currentExceptionSpec = new ExceptionSpec(label);
    }

    public void beginSubRule(Token label, Token start, boolean not) {
        super.beginSubRule(label, start, not);
        // we don't know what kind of subrule it is yet.
        // push a dummy one that will allow us to collect the
        // alternatives.  Later, we'll switch to real object.
        blocks.push(new BlockContext());
        context().block = new AlternativeBlock(grammar, start, not);
        context().altNum = 0; // reset alternative number
        nested++;
        // create a final node to which the last elememt of each
        // alternative will point.
        context().blockEnd = new BlockEndElement(grammar);
        // make sure end node points to start of block
        context().blockEnd.block = context().block;
        //labelElement(context().block, label);
    }

    public void beginTree(Token tok) throws SemanticException {

    }

    public BlockContext context() {
        if (blocks.height() == 0) {
            return null;
        }
        else {
            return (BlockContext)blocks.top();
        }
    }

    /**Used to build nextToken() for the lexer.
     * This builds a rule which has every "public" rule in the given Vector of
     * rules as it's alternate.  Each rule ref generates a Token object.
     * @param g  The Grammar that is being processed
     * @param lexRules A vector of lexer rules that will be used to create an alternate block.
     * @param rname The name of the resulting rule.
     */
    public static RuleBlock createNextTokenRule(Grammar g, Vector lexRules, String rname) {
        // create actual rule data structure
        RuleBlock rb = new RuleBlock(g, rname);
        rb.setDefaultErrorHandler(g.getDefaultErrorHandler());
        RuleEndElement ruleEnd = new RuleEndElement(g);
        rb.setEndElement(ruleEnd);
        ruleEnd.block = rb;
        // Add an alternative for each element of the rules vector.
        for (int i = 0; i < lexRules.size(); i++) {
            RuleSymbol r = (RuleSymbol)lexRules.elementAt(i);
            if (!r.isDefined()) {
                g.antlrTool.error("Lexer rule " + r.id.substring(1) + " is not defined");
            }
            else {
                if (r.access.equals("public")) {
					Alternative alt = new Alternative(); // create alt we'll add to ref rule
					RuleBlock targetRuleBlock = r.getBlock();
					Vector targetRuleAlts = targetRuleBlock.getAlternatives();
					// collect a sem pred if only one alt and it's at the start;
					// simple, but faster to implement until real hoisting
					if ( targetRuleAlts!=null && targetRuleAlts.size()==1 ) {
						Alternative onlyAlt = (Alternative)targetRuleAlts.elementAt(0);
						if ( onlyAlt.semPred!=null ) {
							// ok, has sem pred, make this rule ref alt have a pred
							alt.semPred = onlyAlt.semPred;
							// REMOVE predicate from target rule???  NOPE, another
							// rule other than nextToken() might invoke it.
						}
					}

                    // create a rule ref to lexer rule
                    // the Token is a RULE_REF not a TOKEN_REF since the
                    // conversion to mRulename has already taken place
                    RuleRefElement rr =
                        new RuleRefElement(g,
                                           new CommonToken(ANTLRTokenTypes.RULE_REF, r.getId()),
                                           GrammarElement.AUTO_GEN_NONE);
                    rr.setLabel("theRetToken");
                    rr.enclosingRuleName = "nextToken";
                    rr.next = ruleEnd;
					alt.addElement(rr);  		// add rule ref to alt
                    alt.setAutoGen(true);		// keep text of elements
                    rb.addAlternative(alt);		// add alt to rule block
                    r.addReference(rr);			// track ref to this rule in rule blk
                }
            }
        }

        rb.setAutoGen(true);		// keep text of elements
        rb.prepareForAnalysis();
        //System.out.println(rb);
        return rb;
    }

    /** Return block as if they had typed: "( rule )?" */
    private AlternativeBlock createOptionalRuleRef(String rule, Token start) {

     return null;
    }

    public void defineRuleName(Token r,
                               String access,
                               boolean ruleAutoGen,
                               String docComment)
        throws SemanticException {


    }

    public void endAlt() {

    }

    public void endChildList() {

    }

    public void endExceptionGroup() {

    }

    public void endExceptionSpec() {

    }

    /** Called at the end of processing a grammar */
    public void endGrammar() {

    }

    public void endRule(String rule) {

    }

    public void endSubRule() {

    }

    public void endTree() {
    
    }

    /** Remember that a major error occured in the grammar */
    public void hasError() {

    }

    private void labelElement(AlternativeElement el, Token label) {

    }

    public void noAutoGenSubRule() {

    }

    public void oneOrMoreSubRule() {

    }

    public void optionalSubRule() {

    }

    public void refAction(Token action) {
   
    }

    public void setUserExceptions(String thr) {
;
    }

    // Only called for rule blocks
    public void refArgAction(Token action) {
  
    }

    public void refCharLiteral(Token lit, Token label, boolean inverted, int autoGenType, boolean lastInRule) {
        
    }

    public void refCharRange(Token t1, Token t2, Token label, int autoGenType, boolean lastInRule) {

    }

    public void refTokensSpecElementOption(Token tok,
                                           Token option,
                                           Token value) {
    
    }

    public void refElementOption(Token option, Token value) {

    }

    /** Add an exception handler to an exception spec */
    public void refExceptionHandler(Token exTypeAndName, Token action) {

    }

    public void refInitAction(Token action) {

    }

    public void refMemberAction(Token act) {

    }

    public void refPreambleAction(Token act) {

    }

    // Only called for rule blocks
    public void refReturnAction(Token returnAction) {
  
    }

    public void refRule(Token idAssign,
                        Token r,
                        Token label,
                        Token args,
                        int autoGenType) {
    }

    public void refSemPred(Token pred) {

    }

    public void refStringLiteral(Token lit, Token label, int autoGenType, boolean lastInRule) {

    }

    public void refToken(Token idAssign, Token t, Token label, Token args,
                         boolean inverted, int autoGenType, boolean lastInRule) {

    }

    public void refTokenRange(Token t1, Token t2, Token label, int autoGenType, boolean lastInRule) {

    }

    public void refTreeSpecifier(Token treeSpec) {

    }

    public void refWildcard(Token t, Token label, int autoGenType) {

    }

    /** Get ready to process a new grammar */
    public void reset() {

    }

    public void setArgOfRuleRef(Token argAction) {

    }

    public static void setBlock(AlternativeBlock b, AlternativeBlock src) {

    }

    public void setRuleOption(Token key, Token value) {

    }

    public void setSubruleOption(Token key, Token value) {
     
    }

    public void synPred() {
       
  
    }

    public void zeroOrMoreSubRule() {
    
    }
}
