package net.sf.cglib;

import java.io.ObjectStreamException;

public class EA {
	private Long id;
	private String name;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

    protected Object writeReplace() throws ObjectStreamException {
        return null;
    }
    
    protected void finalTest(){}
    
}

