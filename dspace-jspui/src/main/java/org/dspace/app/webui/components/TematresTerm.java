/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.components;

/**
 * Basic class for representing terms from Tematres
 * 
 * @author Sinayra Pascoal Cotts Moreira
 *
 */
public class TematresTerm
{
	private int id;
	private boolean isMetaTerm;
	private String name;

	public TematresTerm(){};	

	public TematresTerm(int id, boolean isMetaTerm, String name){
		this.id = id;
		this.isMetaTerm = isMetaTerm;
		this.name = name;
	}

	public void setIsMetaTerm(boolean isMetaTerm){
		this.isMetaTerm = isMetaTerm;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return this.id;
	}
	
	public boolean getIsMetaTerm(){
		return this.isMetaTerm;
	}
	
	public String getName(){
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		TematresTerm o = (TematresTerm) obj;
		return o.id == this.id && o.isMetaTerm == this.isMetaTerm && o.name == this.name;
	}
}
