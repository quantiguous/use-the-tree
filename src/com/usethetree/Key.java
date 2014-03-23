package com.usethetree;

public class Key {

	String elemName;
	int index;
	
	
	public Key(String elemName) {
	    
		this.elemName = elemName;
		this.index = 0;
		
	}
	
	public Key(String elemName, int index) {
	    
		this.elemName = elemName;
		this.index = index;
		
	}
	
    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof Key) {
            Key s = (Key)obj;
            return elemName.equals(s.elemName) && index==s.index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (elemName + index).hashCode();
    }

}
