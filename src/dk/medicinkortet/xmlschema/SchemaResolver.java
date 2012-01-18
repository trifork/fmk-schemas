package dk.medicinkortet.xmlschema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

class SchemaResolver implements LSResourceResolver {
	private static final Logger logger = Logger.getLogger(SchemaResolver.class);

	/** Maps name of content resource to SchemaInput used to load the content. */
	private Map<String, SchemaInput> resourceCache = Collections.synchronizedMap(new HashMap<String, SchemaInput>());
	
	private Map<Key, SchemaInput> cache = Collections.synchronizedMap(new HashMap<Key, SchemaInput>());

	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		if (namespaceURI == null && baseURI == null && systemId == null && publicId == null) {
			return null;
		}
		if (baseURI == null || !baseURI.endsWith("index.xsd")) {
			systemId = "index.xsd";
		}
		Key key = new Key(namespaceURI, baseURI, systemId, publicId);
		SchemaInput input = cache.get(key); 
		if (input == null && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type)) {
			if (logger.isDebugEnabled()) logger.trace("validator: " + namespaceURI + " base: " + baseURI + " public: " + publicId + " system: " + systemId + " type: " + type);
			input = new SchemaInput(baseURI, systemId, publicId, namespaceURI);
			if (resourceCache.containsKey(input.resourceName())) {
				input.setInput(resourceCache.get(input.resourceName()));
			} else {
				resourceCache.put(input.resourceName(), input);
			}
			cache.put(key, input);
		}
		return input;
	}

	private static class Key {
		private final String key;

		protected Key(String ... keys) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < keys.length; i++) {
				sb.append(keys[i]).append("_");
			}
			key = sb.toString();
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
