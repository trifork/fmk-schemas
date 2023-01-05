package dk.medicinkortet.notproduktion.namespaceoracle;

import java.util.ArrayList;
import java.util.List;

public class XsdType {
	private String namespace;
	private String name;
	private List<XsdElement> elements = new ArrayList<XsdElement>();
	
	public XsdType(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;
	}

	public List<XsdElement> getElements() {
		return elements;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "XsdType(" + namespace + ":" + name + " with " + elements.size() + " elements)";
	}
}

