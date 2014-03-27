package com.usethetree;

import java.util.Iterator;

public class ChildrenIterator implements Iterator<Reference> {
	
	Reference parent = null;
	Reference next = null;
	

	public ChildrenIterator(Reference parent) {
	    
		this.parent = parent;
		this.next = parent.firstChild;
		
	}

	@Override
	public boolean hasNext() {
		
		return next!=null;
	}

	@Override
	public Reference next() {
		
//		return next = next.nextSibling;
		
		Reference nextReturn = next;
		next = next.nextSibling;
		return nextReturn;
	}

	@Override
	public void remove() {
		
		parent.children.remove(next.prevSibling);
		

	}

}
