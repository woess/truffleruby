package org.jruby.truffle.parser.ast;

import org.jruby.truffle.parser.ast.visitor.NodeVisitor;
import org.jruby.truffle.language.RubySourceSection;

/**
 *
 */
public class KeywordRestArgParseNode extends ArgumentParseNode {
    public KeywordRestArgParseNode(RubySourceSection position, String name, int index) {
        super(position, name, index);
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitKeywordRestArgNode(this);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.KEYWORDRESTARGNODE;
    }
}
