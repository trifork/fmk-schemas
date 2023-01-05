package dk.medicinkortet.notproduktion.validateWikiExamples.xmlvalidator;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

class SchemaInput implements LSInput {
	private static final Logger log = Logger.getLogger(SchemaInput.class);
	
	private final String baseURI;
	private final String systemId;
	private final String publicId;
	private final String namespaceURI;

	private String stringData;

	protected SchemaInput(String baseURI, String systemId, String publicId, String namespaceURI) {
		this.baseURI = baseURI;
		this.systemId = systemId;
		this.publicId = publicId;
		this.namespaceURI = namespaceURI;
		
		loadContents();
	}

	private void loadContents() {
		InputStream si = null;
		Reader reader = null;
		try {
			String resourceName = resourceName();
			si = getClass().getResourceAsStream(resourceName);
			if (si == null) {
				return;
			}
			reader = new InputStreamReader(si, getEncoding());
			stringData = IOUtils.toString(reader);
		} catch (Exception e) {
			String errorMessage = getErrorMessage();
			log.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		} finally {
			IOUtils.closeQuietly(si);
			IOUtils.closeQuietly(reader);
		}
	}

	private String getErrorMessage() {
		return "Unable to read schema file for baseURI=" + baseURI
				+ ", systemId=" + systemId
				+ ", publicId=" + publicId
				+ ", namespaceURI=" + namespaceURI;
	}

	/**
	 * Returns the name of the resource containing content of this SchemaInput.
	 * 
	 * @return	Name of resource containing content.
	 */
	protected String resourceName() throws URISyntaxException {
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
		// p = p.replace("/medicinecard/xml.schema/", "/schemas/") + "/" + file;
		p = p.replace("//", "/");
		return p;
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
		return stringData;
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
}
