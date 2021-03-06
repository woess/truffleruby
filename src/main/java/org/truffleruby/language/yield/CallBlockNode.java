/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.yield;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.DeclarationContext;

@NodeChildren({
        @NodeChild("block"),
        @NodeChild("self"),
        @NodeChild("blockArgument"),
        @NodeChild(value = "arguments", type = RubyNode[].class)
})
public abstract class CallBlockNode extends RubyNode {

    public static CallBlockNode create() {
        return CallBlockNodeGen.create(null, null, null, null);
    }

    public abstract Object executeCallBlock(DeclarationContext declarationContext, DynamicObject block, Object self, Object blockArgument, Object[] arguments);

    // blockArgument is typed as Object below because it must accept "null".
    @Specialization(
            guards = "getBlockCallTarget(block) == cachedCallTarget",
            limit = "getCacheLimit()")
    protected Object callBlockCached(
            DeclarationContext declarationContext,
            DynamicObject block,
            Object self,
            Object blockArgument,
            Object[] arguments,
            @Cached("getBlockCallTarget(block)") CallTarget cachedCallTarget,
            @Cached("createBlockCallNode(cachedCallTarget)") DirectCallNode callNode) {
        final Object[] frameArguments = packArguments(declarationContext, block, self, blockArgument, arguments);
        return callNode.call(frameArguments);
    }

    @Specialization(replaces = "callBlockCached")
    protected Object callBlockUncached(
            DeclarationContext declarationContext,
            DynamicObject block,
            Object self,
            Object blockArgument,
            Object[] arguments,
            @Cached("create()") IndirectCallNode callNode) {
        final Object[] frameArguments = packArguments(declarationContext, block, self, blockArgument, arguments);
        return callNode.call(getBlockCallTarget(block), frameArguments);
    }

    private Object[] packArguments(DeclarationContext declarationContext, DynamicObject block, Object self, Object blockArgument, Object[] arguments) {
        return RubyArguments.pack(
                Layouts.PROC.getDeclarationFrame(block),
                null,
                Layouts.PROC.getMethod(block),
                declarationContext,
                Layouts.PROC.getFrameOnStackMarker(block),
                self,
                (DynamicObject) blockArgument,
                arguments);
    }

    protected static CallTarget getBlockCallTarget(DynamicObject block) {
        return Layouts.PROC.getCallTargetForType(block);
    }

    protected DirectCallNode createBlockCallNode(CallTarget callTarget) {
        final DirectCallNode callNode = Truffle.getRuntime().createDirectCallNode(callTarget);

        if (getContext().getOptions().YIELD_ALWAYS_CLONE && callNode.isCallTargetCloningAllowed()) {
            callNode.cloneCallTarget();
        }

        if (getContext().getOptions().YIELD_ALWAYS_INLINE && callNode.isInlinable()) {
            callNode.forceInlining();
        }

        return callNode;
    }

    protected int getCacheLimit() {
        return getContext().getOptions().YIELD_CACHE;
    }

}
