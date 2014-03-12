package com.usethetree;

import java.util.LinkedList;

public class Tree {

  public String elemName;
  public String value;

  public LinkedList<Tree> leafs = new LinkedList<Tree>();

  public Tree parent = null;
  public Tree nextSibling = null;
  public Tree prevSibling = null;

  public Tree(String elemName) {
    this.elemName = elemName;
  }

  public Tree(String elemName, String value) {
	    this.elemName = elemName;
	    this.value = value;
	  }
  
  public Tree(Tree parent, String elemName, String value) {
	  this.parent = parent;
	  this.elemName = elemName;
	  this.value = value;
	 }
  
  public Tree addLeaf(String elemName, String value) {
	  Tree leaf = new Tree(this, elemName, value);
	  if (!this.leafs.isEmpty()) {
		  leaf.prevSibling=this.leafs.getLast();
		  this.leafs.getLast().nextSibling=leaf;
	  }
	  this.leafs.add(leaf);
      return leaf;
  }

  public Tree addLeaf(String elemName) {
	  return addLeaf(elemName, null);
  }
  

  
  

  
}