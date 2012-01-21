package dk.medicinkortet.xmlschema;

import java.util.Map;

public class Namespaces extends com.trifork.xmlquery.Namespaces {

	private static final Namespaces INSTANCE = new Namespaces();
	
	public static Namespaces getInstance() {
		return INSTANCE;
	}
	
	private Namespaces() {
		super.addNamespace("mc2008", "http://www.dkma.dk/medicinecard/xml.schema/2008/06/01");
		super.addNamespace("mc2009", "http://www.dkma.dk/medicinecard/xml.schema/2009/01/01");
		super.addNamespace("mc2010", "http://www.dkma.dk/medicinecard/xml.schema/2010/06/01");
		super.addNamespace("mc2011", "http://www.dkma.dk/medicinecard/xml.schema/2011/01/01");
		super.addNamespace("mc2012", "http://www.dkma.dk/medicinecard/xml.schema/2012/01/01");
		super.addNamespace("cpr", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/");
		super.addNamespace("cpr2002", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2002/06/28/");
		super.addNamespace("cpr2006", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2006/01/17/");
		super.addNamespace("xkom", "http://rep.oio.dk/xkom.dk/xml/schemas/2006/01/06/");
		super.addNamespace("dkcc", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/");
		super.addNamespace("dkcc2005", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/03/15/");
		super.addNamespace("medcom", "http://www.medcom.dk/dgws/2006/04/dgws-1.0.xsd");
		super.addNamespace("itst2005", "http://rep.oio.dk/itst.dk/xml/schemas/2005/01/10/");
		super.addNamespace("itst", "http://rep.oio.dk/itst.dk/xml/schemas/2006/01/17/");
		super.addNamespace("dkcc2005-2", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/05/13/");
		super.addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
		super.addNamespace("rs", "http://dkma.dk/receptserver/apotekssnitflade/xml/schemas/");
		super.addNamespace("pem", "http://dkma.dk/receptserver/apotekssnitflade/xml/schemas/");
		super.addNamespace("dkma", "http://rep.oio.dk/dkma.dk/xml/schemas/2006/01/15/");
		super.addNamespace("sundcom", "http://rep.oio.dk/sundcom.dk/medcom.dk/xml/schemas/2005/08/07/");
		super.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
		super.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		super.addNamespace("ean", "http://rep.oio.dk/ean/xml/schemas/2005/01/10/");
		super.addNamespace("sdsd", "http://www.sdsd.dk/dgws/2010/08");
		
		super.addNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		super.addNamespace("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		super.addNamespace("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		super.addNamespace("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
		super.addNamespace("wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
		super.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
		super.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
	}
	
	public void addNamespace(String prefix, String ns) {
		throw new IllegalStateException("This is supposed to be a immutable singleton Namespaces class - don't call this method");
	}
	
	public void setNamespaces(Map<String, String> namespaces) {
		throw new IllegalStateException("This is supposed to be a immutable singleton Namespaces class - don't call this method");
	}
	
}
