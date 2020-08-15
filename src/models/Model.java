package models;

import java.util.HashMap;


@SuppressWarnings("rawtypes")
public abstract class Model<Father extends Model, Child extends Model> {
	 
	Father father;
	int id;
	HashMap<Integer, Child> childrens;
	
	public Model(Father f, int id) {
		this.father = f;
		this.id=id;
		childrens = new HashMap<Integer, Child>();
	}
	
	public void download() {
		childrens.values().forEach(c -> c.download());
	}
	
	public String getDownloadPath() throws NullPointerException {
		return father.getDownloadPath() + "/" + this.getClass().getName() + "_" + this.getID();
	}
	
	public Father getFather() {
		return this.father;
	}
	
	protected HashMap<Integer, Child> getChildren() {
		return childrens;
	}
	
	public void AddChildren(Child child) {
		this.childrens.put(child.getID(), child);
	}
	
	public int getID() {
		return this.id;
	}
}
