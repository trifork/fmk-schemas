package dk.medicinkortet.notproduktion.validateWikiExamples.xmlvalidator;

import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
//import org.springframework.xml.validation.ValidationErrorHandler;
import org.xml.sax.ErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
	
	private static final String debugNamespaces[] = {
		"http://schemas.microsoft.com/2004/09/ServiceModel/Diagnostics",
		"http://schemas.microsoft.com/vstudio/diagnostics/servicemodelsink"
	};
	private static final String debugElements[] = {
		"ActivityId",
		"VsDebuggerCausalityData"
	};
	
	private static final ThreadLocal<DocumentBuilderFactory> builderFactory = new ThreadLocal<DocumentBuilderFactory>() {
		protected DocumentBuilderFactory initialValue() {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			return builderFactory;
		}
	};
	

	private Schema schema;
	private static SchemaResolver resolver = new SchemaResolver();

	public SchemaValidator() {
		SchemaFactory factory = new XMLSchemaFactory();
		try {
//			factory.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
			schema = factory.newSchema();
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * FMK-1580/FMK-1723 Allow use of MS debug headers
	 * @param doc
	 * @throws SAXException
	 */
	void removeDebugHeadersFromDocument(Document doc) throws SAXException {
		
		for(int i = 0; i < debugNamespaces.length; i++) {
			NodeList nl = doc.getElementsByTagNameNS(debugNamespaces[i], debugElements[i]);
			if(nl != null) {
				for(int nodeIndex = 0; nodeIndex < nl.getLength(); nodeIndex++) {
					Node debugNode = nl.item(nodeIndex);
					if(debugNode != null) {
						debugNode.getParentNode().removeChild(debugNode);
					}
				}
			}
		}
	}
	
	public Document validate(String xml) throws SAXException {
		Validator validator = schema.newValidator();
		validator.setResourceResolver(resolver);
        validator.setErrorHandler(new DefaultValidationErrorHandler());

		try {
			DocumentBuilder builder = builderFactory.get().newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			removeDebugHeadersFromDocument(doc);
            validator.validate(new DOMSource(doc));
			return doc;
		} catch (ParserConfigurationException e) {
			logger.error("Unable to parse", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
	}

//    public void newValidator(String xml) {
//        try {
//            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//            f.setValidating(true); // Default is false
//            f.setSchema(schema);
//            DocumentBuilder b = f.newDocumentBuilder();
//            b.setErrorHandler(new DefaultValidationErrorHandler());
//            //b.setEntityResolver(resolver);
//            Document d = b.parse(new InputSource(new StringReader(xml)));
//        } catch (ParserConfigurationException e) {
//            System.out.println(e.toString());
//        } catch (SAXException e) {
//            System.out.println(e.toString());
//        } catch (IOException e) {
//            System.out.println(e.toString());
//        }
//    }

	public void validate(Source xmlSource) throws SAXException {
        validate(xmlSource, null);
	}

	public void validate(Source xmlSource, DefaultValidationErrorHandler errorHandler) throws SAXException {
		Validator validator = schema.newValidator();
		validator.setResourceResolver(resolver);
        if (errorHandler != null) {
            validator.setErrorHandler(errorHandler);
        }

		try {
			validator.validate(xmlSource);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    //'Borrowed' from org.springframework.xml.validation.Jaxp13ValidatorFactory.DefaultValidationErrorHandler
    private static class DefaultValidationErrorHandler implements ErrorHandler {

        public void warning(SAXParseException e) throws SAXException {
			System.out.println("    WARNING: " + e.getMessage() + "  Line " + e.getLineNumber() + " column " + e.getColumnNumber());
        }

        public void error(SAXParseException e) throws SAXException {
			System.out.println("    ERROR: " + e.getMessage() + "  Line " + e.getLineNumber() + " column " + e.getColumnNumber());
        }

        public void fatalError(SAXParseException e) throws SAXException {
			System.out.println("    FATAL ERROR: " + e.getMessage() + "  Line " + e.getLineNumber() + " column " + e.getColumnNumber());
        }
    }

}
