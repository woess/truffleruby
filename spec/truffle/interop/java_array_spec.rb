# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.java_array" do
  
  it "converts a Ruby array to a Java array" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(1, 2, 3))).should == "Object[]"
  end
  
  it "copies the correct values" do
    a = Truffle::Interop.java_array(1, 2, 3)
    a[0].should == 1
    a[1].should == 2
    a[2].should == 3
  end

  it "will use Object[] becuase that's what splat does" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(1, 2, 3))).should == "Object[]"
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(1.1, 2.2, 3.3))).should == "Object[]"
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(:a, :b, :c))).should == "Object[]"
  end
  
  it "creates a copy" do
    a1 = [1, 2, 3]
    a2 = Truffle::Interop.java_array(*a1)
    a2[0].should == 1
    a1[0] = 10
    a2[0].should == 1
  end

end
