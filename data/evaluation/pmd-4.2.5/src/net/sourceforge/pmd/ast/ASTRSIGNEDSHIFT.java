/* Generated By:JJTree: Do not edit this line. ASTRSIGNEDSHIFT.java */

package net.sourceforge.pmd.ast;

public class ASTRSIGNEDSHIFT extends SimpleJavaNode {
    public ASTRSIGNEDSHIFT(int id) {
        super(id);
    }

    public ASTRSIGNEDSHIFT(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
