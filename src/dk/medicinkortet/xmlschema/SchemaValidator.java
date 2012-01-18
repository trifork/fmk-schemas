package dk.medicinkortet.xmlschema;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Schema validator.
 * 
 * This class is thread safe.
 * 
 * @author recht
 *
 */
public class SchemaValidator {
	private static final Logger logger = Logger.getLogger(SchemaValidator.class);
	
	private static final ThreadLocal<DocumentBuilderFactory> builderFactory = new ThreadLocal<DocumentBuilderFactory>() {
		protected DocumentBuilderFactory initialValue() {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			return builderFactory;
		}
	};
	

	private Schema schema;
	private SchemaResolver resolver;

	public SchemaValidator() {
		SchemaFactory factory = new XMLSchemaFactory();
		try {
//			factory.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
			schema = factory.newSchema();
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
		resolver = new SchemaResolver();
	}
	
	public Document validate(String xml) throws SAXException {
		Validator validator = schema.newValidator();
		validator.setResourceResolver(resolver);
		
		try {
			DocumentBuilder builder = builderFactory.get().newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));			
			validator.validate(new DOMSource(doc));
			return doc;
		} catch (ParserConfigurationException e) {
			logger.error("Unable to parse", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
	}
	
}
