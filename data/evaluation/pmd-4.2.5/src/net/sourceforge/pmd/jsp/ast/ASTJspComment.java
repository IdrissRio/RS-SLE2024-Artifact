/* Generated By:JJTree: Do not edit this line. ASTJspComment.java */

package net.sourceforge.pmd.jsp.ast;

public class ASTJspComment extends SimpleNode {
    public ASTJspComment(int id) {
        super(id);
    }

    public ASTJspComment(JspParser p, int id) {
        super(p, id);
    }


    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JspParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
