package dk.medicinkortet.notproduktion.validateWikiExamples.xmlvalidator;

import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

class SchemaResolver implements LSResourceResolver {
	private static final Logger log = Logger.getLogger(SchemaResolver.class);

	/** Maps name of content resource to SchemaInput used to load the content. */
	Memoizer<Key, SchemaInput> memoizer = new Memoizer<Key, SchemaInput>(new SchemaInputComputable());


	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		//non ws call to http://host/fmk12/ws/MedicineCard results in a lookup of this soap-envelope
		if (namespaceURI == null || namespaceURI == "http://www.w3.org/2003/05/soap-envelope"
				||  namespaceURI == "http://dk.acure.pem.security") { //test call from medicin-it
			return null;
		}
		if (baseURI == null || !baseURI.endsWith("index.xsd")) {
			systemId = "index.xsd";
		} else if (systemId == null) {
			return null;
		}
		Key key = new Key(namespaceURI, baseURI, systemId, publicId);
		SchemaInput input = null;
		try {
			input = memoizer.compute(key);
		} catch (InterruptedException e) {
			log.error("Memoizer was interrupted", e);
		}
		return input;
	}

	
	/**
	 * When necessary this class allocates the SchemaInput.
	 */
	static class SchemaInputComputable implements Computable<Key, SchemaInput> {
		@Override
		public SchemaInput compute(Key key) throws InterruptedException {
			return new SchemaInput(key.baseURI, key.systemId, key.publicId, key.namespaceURI);
		}
	}
	
	
	static class Key {
		private final String key;
		public final String namespaceURI;
		public final String baseURI;
		public final String systemId;
		public final String publicId;

		protected Key(String namespaceURI, String baseURI, String systemId, String publicId) {
			
			this.namespaceURI = namespaceURI;
			this.baseURI = baseURI;
			this.systemId = systemId;
			this.publicId = publicId;
			
			this.key = namespaceURI + "_" + baseURI + "_" + systemId + "_" + publicId;
		}

		public int hashCode() {
			return key.hashCode();
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Key other = (Key) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
	}
}
