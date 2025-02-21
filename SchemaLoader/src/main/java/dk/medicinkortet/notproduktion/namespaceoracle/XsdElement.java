package dk.medicinkortet.notproduktion.namespaceoracle;


public class XsdElement {
	private String namespace;
	private String name;
	private XsdType type;
	private String filename;
	private boolean processed = false;
	
	public XsdElement(String namespace, String name, XsdType type) {
		this.namespace = namespace;
		this.name = name;
		this.type = type;
	}

	public XsdElement(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public XsdType getType() {
		return type;
	}

	public void setType(XsdType type) {
		this.type = type;
	}

	public String toString() {
		return "XsdElement '" + name + "' in namespace: " + namespace; 
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setProcessed() {
		processed = true;
	}
	
	public boolean isProcessed() {
		return processed;
	}
}
