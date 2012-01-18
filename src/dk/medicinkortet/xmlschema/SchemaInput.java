package dk.medicinkortet.xmlschema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;

class SchemaInput implements LSInput {
	private static final Logger logger = Logger.getLogger(SchemaInput.class);
	
	private final String baseURI;
	private final String systemId;
	private final String publicId;
	private final String namespaceURI;

	/** {@see #setInput(SchemaInput)} */
	private SchemaInput input;

	private String stringData;

	protected SchemaInput(String baseURI, String systemId, String publicId, String namespaceURI) {
		this.baseURI = baseURI;
		this.systemId = systemId;
		this.publicId = publicId;
		this.namespaceURI = namespaceURI;
	}

	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * Returns null.
	 */
	public InputStream getByteStream() {
		return null;
	}

	public boolean getCertifiedText() {
		return false;
	}

	/**
	 * Returns null.
	 */
	public Reader getCharacterStream() {
		return null;
	}

	public String getEncoding() {
		return "UTF-8";
	}

	public String getPublicId() {
		return publicId;
	}

	/**
	 * Returns content.
	 */
	public String getStringData() {
		if (input!=null) {
			return input.getStringData();
		} else {
			if (stringData==null) {
				InputStream si = null;
				Reader reader = null;
				try {
					String resourceName = resourceName();
					si = getClass().getResourceAsStream(resourceName);
					if (si == null) {
						String s = "Unable to find schema file " + resourceName + " for " + systemId;
						logger.error(s);
						throw new NullPointerException(s);
					}
					reader = new InputStreamReader(si, getEncoding());
					char[] content = IOUtils.toCharArray(reader);
					stringData = new String(content);
				} catch (IOException e) {
					throw new RuntimeException("Unable to read schema file for " + systemId, e);
				} finally {
					IOUtils.closeQuietly(si);
					IOUtils.closeQuietly(reader);
				}
			}
			return stringData;
		}
	}

	public String getSystemId() {
		return systemId;
	}

	public void setBaseURI(String baseURI) {
		throw new UnsupportedOperationException();
	}

	public void setByteStream(InputStream byteStream) {
		throw new UnsupportedOperationException();
	}

	public void setCertifiedText(boolean certifiedText) {
		throw new UnsupportedOperationException();
	}

	public void setCharacterStream(Reader characterStream) {
		throw new UnsupportedOperationException();
	}

	public void setEncoding(String encoding) {
		throw new UnsupportedOperationException();
	}

	public void setPublicId(String publicId) {
		throw new UnsupportedOperationException();
	}

	public void setStringData(String stringData) {
		throw new UnsupportedOperationException();
	}

	public void setSystemId(String systemId) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns the name of the resource containing content of this SchemaInput.
	 * 
	 * @return	Name of resource containing content.
	 */
	protected String resourceName() {
		try {
			String file = null;
			int idx = systemId.lastIndexOf('/');
			if (idx > -1) {
				file = systemId.substring(idx);
			} else {
				file = systemId;
			}
			URI u = new URI(namespaceURI);
			String p = u.getPath();
			if (p == null) {
				p = namespaceURI.replaceAll(":", "_");
			}
			p = "/META-INF/schemas/" + p + "/" + file;
			p = p.replace("//", "/");
			return p;
		} catch (URISyntaxException e1) {
			throw new RuntimeException("Unable to find schema file for " + systemId);
		}
	}

	/**
	 * Sets the SchemaInput to use for retrieving the content. Used when multiple SchemaInput's point to the
	 * same resource.
	 * 
	 * @param schemaInput
	 */
	public void setInput(SchemaInput schemaInput) {
		this.input = schemaInput;
	}
}
