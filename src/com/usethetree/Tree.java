package com.usethetree;

import java.util.LinkedList;

public class Tree {

	// I don't care about getters and setters ;-):
  public String elemName;
  public String value;

  public LinkedList<Tree> leafs = new LinkedList<Tree>();

  public Tree parent = null;
  public Tree nextSibling = null;
  public Tree prevSibling = null;
  
  public Tree prevGroupingSibling = null;
  

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
  
  public Tree firstChild(String elemName) {
	  
	  for (Tree child : this.leafs) {
		  if (child.elemName.equals(elemName))
		  return child;
	  }
	  return null;
  }
  
  public Tree firstChild() {
	  return this.leafs.getFirst();
  }
  
  public Tree addNextSibling(String elemName) {
	  return this.parent.addLeaf(elemName);
  }
  

  
}