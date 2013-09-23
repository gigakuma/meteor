package lab.meteor.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MClass extends MElement {

	private String name;
	
	private Map<String, MAttribute> attributes;
	private Map<String, MReference> references;
	
	private MClass superclass;
	private MPackage parent;
	
	private Set<MClass> subclasses;
	
	private Set<MReference> utilizers;
	
	/* 
	 * ********************************
	 *          CONSTRUCTORS
	 * ********************************
	 */
	
	/**
	 * Create a new class in default package, without superclass.
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 */
	public MClass(String name) {
		this(name, null, null);
	}
	
	/**
	 * Create a new class in default package with superclass.
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 * @param superclazz superclass.
	 */
	public MClass(String name, MClass superclazz) {
		this(name, superclazz, MPackage.DEFAULT_PACKAGE);
	}
	
	/**
	 * Create a new class, without superclass.
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 * @param pkg package.
	 */
	public MClass(String name, MPackage pkg) {
		this(name, null, pkg);
	}
	
	/**
	 * Create a new class with specified name, superclass and package(owner).
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 * @param supercls superclass.
	 * @param pkg package.
	 */
	public MClass(String name, MClass supercls, MPackage pkg) {
		super(MElementType.Class);
		
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (pkg.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		if (supercls != null && supercls.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		
		this.initialize();
		this.name = name;
		this.superclass = supercls;
		if (this.superclass != null)
			this.superclass.getSubclasses().add(this);
		this.parent = pkg;
		this.parent.addClass(this);
		
		MDatabase.getDB().createElement(this);
	}
	
	/**
	 * Create a "lazy" class element with id.
	 * @param id ID of element.
	 */
	protected MClass(long id) {
		super(id, MElementType.Class);
	}
	
	/*
	 * ********************************
	 *          DESTRUCTORS
	 * ********************************
	 */
	
	/**
	 * Delete all MElement associated with this class, include attributes, 
	 * references, the references refer to this class. If there is(are) sub-class(es),
	 * all of its sub-classes's superclass automatically set to be this class's 
	 * superclass.
	 */
	@Override
	public void delete() {
		// delete attributes
		if (this.attributes != null) {
			for (MAttribute atb : this.attributes.values()) {
				atb.delete();
			}
			this.attributes.clear();
		}
		// delete references
		if (this.references != null) {
			for (MReference ref : this.references.values()) {
				ref.delete();
			}
			this.references.clear();
		}
		// delete utilizers
		if (this.utilizers != null) {
			for (MReference ref : this.utilizers) {
				ref.delete();
			}
			this.utilizers.clear();
		}
		// unlink super-sub relations
		if (this.subclasses != null) {
			for (MClass cls : this.subclasses) {
				cls.superclass = this.superclass;
			}
		}
		if (this.superclass != null)
			this.superclass.getSubclasses().remove(this);
		
		// package
		this.parent.removeClass(this);
		super.delete();
	}

	public void deleteAllInstances() {
		// TODO
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */
	
	/**
	 * Get the name of class.
	 * @return name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of class. It's notable that changing the name of class
	 * does not disturb the instantiated objects of this class. The name has
	 * to be unique, otherwise an exception will be thrown.
	 * @param name the name of class.
	 */
	public void setName(String name) {
		if (name.equals(this.name))
			return;
		if (this.parent.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeClass(this);
		this.name = name;
		this.parent.addClass(this);
		this.setChanged();
	}
	
	/**
	 * The superclass of the class.
	 * @return superclass.
	 */
	public MClass getSuperClass() {
		return this.superclass;
	}
	
	/**
	 * Set the super class of the class. It's forbidden to set class itself or its sub-
	 * class to be its super class.
	 * @param clazz superclass.
	 */
	public void setSuperClass(MClass clazz) {
		if (this.superclass == clazz)
			return;
		MClass cp = clazz;
		while (cp != null) {
			if (cp == this)
				throw new MException(MException.Reason.INVALID_SUPER_CLASS);
			cp = cp.superclass;
		}
		
		if (this.superclass != null)
			this.superclass.getSubclasses().remove(this);
		this.superclass = clazz;
		if (this.superclass != null)
			this.superclass.getSubclasses().add(this);
		this.setChanged();
	}
	
	/**
	 * Find whether this class is a sub-class of another.
	 * @param clazz another class
	 * @return {@code true} if is sub-class.
	 */
	public boolean asSubClass(MClass clazz) {
		MClass cp = this.superclass;
		do {
			if (cp == clazz)
				return true;
		} while (cp != null);
		return false;
	}
	
	/**
	 * Get all sub-classes.
	 * @return the set of sub-classes.
	 */
	private Set<MClass> getSubclasses() {
		if (this.subclasses == null)
			this.subclasses = new TreeSet<MClass>();
		return this.subclasses;
	}
	
	/**
	 * The package of the class.
	 * @return package.
	 */
	public MPackage getPackage() throws MException {
		return this.parent;
	}
	
	/**
	 * Set the package of the class.
	 * @param pkg package.
	 */
	public void setPackage(MPackage pkg) throws MException {
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasChild(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeClass(this);
		this.parent = pkg;
		this.parent.addClass(this);
		this.setChanged();
	}
	
	/**
	 * Find whether this class has an attribute or a reference named with
	 * the specified name.
	 * @param name the specified name.
	 * @return {@code true} if there is.
	 */
	public boolean hasChild(String name) {
		return this.hasAttribute(name) || this.hasReference(name);
	}
	
	/* 
	 * ********************************
	 *      ATTRIBUTE OPERATIONS
	 * ********************************
	 */
	
	/**
	 * Get all names of attributes owned by this class.
	 * @return the set of attribute names.
	 */
	public Set<String> getAttributeNames() {
		return new TreeSet<String>(this.getAttributes().keySet());
	}
	
	/**
	 * Get all names of attributes, include the attributes owned by superclass.
	 * @return the set of attribute names.
	 */
	public Set<String> getAllAttributeNames() {
		Set<String> names = new TreeSet<String>();
		MClass cls = this;
		while (cls != null) {
			names.addAll(cls.getAttributes().keySet());
			cls = cls.superclass;
		}
		return names;
	}
	
	/**
	 * Get the attribute with specific name.
	 * @param name the specific name.
	 * @return attribute with specific name. {@code null} if there is no one.
	 */
	public MAttribute getAttribute(String name) {
		MAttribute atb = null;

		MClass cls = this;
		while (cls != null) {
			atb = cls.getAttributes().get(name);
			if (atb != null)
				break;
			cls = cls.superclass;
		}
		return atb;
	}

	/**
	 * Find whether this class own an attribute named with specified name.
	 * @param name the specified name.
	 * @return {@code true} if there is.
	 */
	public boolean hasAttribute(String name) {
		MClass cls = this;
		while (cls != null) {
			if (cls.getAttributes().containsKey(name))
				return true;
			cls = cls.superclass;
		}
		return false;
	}

	/**
	 * Get the attributes.
	 * @return the map from name to attribute.
	 */
	private Map<String, MAttribute> getAttributes() {
		if (this.attributes == null)
			this.attributes = new TreeMap<String, MAttribute>();
		return this.attributes;
	}
	
	/**
	 * Add attribute.
	 * @param atb attribute.
	 */
	protected void addAttribute(MAttribute atb) {
		this.getAttributes().put(atb.getName(), atb);
	}

	/**
	 * Remove attribute.
	 * @param atb attribute.
	 */
	protected void removeAtttribute(MAttribute atb) {
		this.getAttributes().remove(atb.getName());
	}
	
	/* 
	 * ********************************
	 *       REFERENCE OPERATIONS
	 * ********************************
	 */

	/**
	 * Get all names of references owned by this class.
	 * @return the set of reference names.
	 */
	public Set<String> getReferenceNames() {
		return new TreeSet<String>(this.getReferences().keySet());
	}
	
	/**
	 * Get all names of references, include the references owned by superclass.
	 * @return the set of reference names.
	 */
	public Set<String> getAllReferenceNames() {
		Set<String> names = new TreeSet<String>();
		MClass cls = this;
		while (cls != null) {
			names.addAll(cls.getReferences().keySet());
			cls = cls.superclass;
		}
		return names;
	}
	
	/**
	 * Get the reference with specific name.
	 * @param name the specific name.
	 * @return reference with specific name. {@code null} if there is no one.
	 */
	public MReference getReference(String name) {
		MReference ref = null;
		
		MClass cls = this;
		while (cls != null) {
			ref = cls.getReferences().get(name);
			if (ref != null)
				break;
			cls = cls.superclass;
		}
		return ref;
	}
	
	/**
	 * Find whether this class own a reference named with specified name.
	 * @param name the specified name.
	 * @return {@code true} if there is.
	 */
	public boolean hasReference(String name) {
		MClass cls = this;
		while (cls != null) {
			if (cls.getReferences().containsKey(name))
				return true;
			cls = cls.superclass;
		}
		return false;
	}
	
	/**
	 * Get the references.
	 * @return the map from name to reference.
	 */
	private Map<String, MReference> getReferences() {
		if (this.references == null)
			this.references = new TreeMap<String, MReference>();
		return this.references;
	}

	/**
	 * Add reference.
	 * @param ref reference.
	 */
	protected void addReference(MReference ref) {
		this.getReferences().put(ref.getName(), ref);
	}
	
	/**
	 * Remove reference.
	 * @param ref reference.
	 */
	protected void removeReference(MReference ref) {
		this.getReferences().remove(ref.getName());
	}
	
	/*
	 * ********************************
	 *            UTILIZE
	 * ********************************
	 */
	
	/**
	 * Utilizers are the references({@code MReference}) refer to this class.
	 * @return the utilizers.
	 */
	private Set<MReference> getUtilizers() {
		if (this.utilizers == null)
			this.utilizers = new TreeSet<MReference>();
		return this.utilizers;
	}
	
	/**
	 * Add utilizer.
	 * @param utilizer a reference.
	 */
	void addUtilizer(MReference utilizer) {
		this.getUtilizers().add(utilizer);
	}
	
	/**
	 * Remove utilizer.
	 * @param utilizer a reference.
	 */
	void removeUtilizer(MReference utilizer) {
		this.getUtilizers().remove(utilizer);
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */

	@Override
	void loadFromDBInfo(Object dbInfo) {
		MDBAdapter.ClassDBInfo clsDBInfo = (MDBAdapter.ClassDBInfo) dbInfo;
		this.name = clsDBInfo.name;
		this.superclass = MDatabase.getDB().getClass(clsDBInfo.superclass_id);
		this.parent = MDatabase.getDB().getPackage(clsDBInfo.package_id);
		
		// link
		this.parent.addClass(this);
		if (this.superclass != null)
			this.superclass.getSubclasses().add(this);
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		MDBAdapter.ClassDBInfo clsDBInfo = (MDBAdapter.ClassDBInfo) dbInfo;
		clsDBInfo.id = this.id;
		clsDBInfo.name = this.name;
		clsDBInfo.superclass_id = MElement.getElementID(this.superclass);
		clsDBInfo.package_id = MElement.getElementID(this.parent);
	}
	
	/*
	 * ********************************
	 *             STRING
	 * ********************************
	 */
	
	@Override
	public String toString() {
		if (this.parent == MPackage.DEFAULT_PACKAGE)
			return this.name;
		return this.parent.toString() + "::" + this.name;
	}

}
