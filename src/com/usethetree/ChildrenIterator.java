package com.usethetree;

import java.util.Iterator;

public class ChildrenIterator implements Iterator<Ref> {
	
	Ref parent = null;
	Ref next = null;
	

	public ChildrenIterator(Ref parent) {
	    
		this.parent = parent;
		this.next = parent.firstChild;
		
	}

	@Override
	public boolean hasNext() {
		
		return next!=null;
	}

	@Override
	public Ref next() {
		
//		return next = next.nextSibling;
		
		Ref nextReturn = next;
		next = next.nextSibling;
		return nextReturn;
	}

	@Override
	public void remove() {
		
		parent.children.remove(next.prevSibling);
		

	}

}
