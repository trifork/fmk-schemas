package dk.medicinkortet.xmlschema;

import com.trifork.xmlquery.Namespaces;

public class FmkNamespaces {

	public static Namespaces getNamespaces() {
		Namespaces namespaces = Namespaces.getOIONamespaces();
		namespaces.addNamespace("medicinecard20120601", "http://www.dkma.dk/medicinecard/xml.schema/2012/06/01");
		namespaces.addNamespace("medicinecard20130601", "http://www.dkma.dk/medicinecard/xml.schema/2013/06/01");
		namespaces.addNamespace("medicinecard20131101", "http://www.dkma.dk/medicinecard/xml.schema/2013/11/01");
		namespaces.addNamespace("medicinecard2013pem", "http://www.dkma.dk/medicinecard/xml.schema/2013/pem");
		namespaces.addNamespace("medicinecard20140501", "http://www.dkma.dk/medicinecard/xml.schema/2014/05/01");
		namespaces.addNamespace("medicinecard20140601", "http://www.dkma.dk/medicinecard/xml.schema/2014/06/01");
		namespaces.addNamespace("medicinecard20140801", "http://www.dkma.dk/medicinecard/xml.schema/2014/08/01");
		namespaces.addNamespace("medicinecard20150101", "http://www.dkma.dk/medicinecard/xml.schema/2015/01/01");
		namespaces.addNamespace("medicinecard20150101E1", "http://www.dkma.dk/medicinecard/xml.schema/2015/01/01/E1");
		namespaces.addNamespace("medicinecard20150101E2", "http://www.dkma.dk/medicinecard/xml.schema/2015/01/01/E2");
		namespaces.addNamespace("medicinecard20150101E4", "http://www.dkma.dk/medicinecard/xml.schema/2015/01/01/E4");
		namespaces.addNamespace("medicinecard20150101E5", "http://www.dkma.dk/medicinecard/xml.schema/2015/01/01/E5");
        namespaces.addNamespace("medicinecard20150601", "http://www.dkma.dk/medicinecard/xml.schema/2015/06/01");
        namespaces.addNamespace("medicinecard20150601E2", "http://www.dkma.dk/medicinecard/xml.schema/2015/06/01/E2");
        namespaces.addNamespace("medicinecard20150601E3", "http://www.dkma.dk/medicinecard/xml.schema/2015/06/01/E3");
        namespaces.addNamespace("medicinecard20150601E5", "http://www.dkma.dk/medicinecard/xml.schema/2015/06/01/E5");
		namespaces.addNamespace("ssi2013", "http://www.ssi.dk/nsi/xml.schema/2013/01/01");
		namespaces.addNamespace("cpr", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/");
		namespaces.addNamespace("cpr2002", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2002/06/28/");
		namespaces.addNamespace("cpr2006", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2006/01/17/");
		namespaces.addNamespace("xkom", "http://rep.oio.dk/xkom.dk/xml/schemas/2006/01/06/");
		namespaces.addNamespace("dkcc", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/");
		namespaces.addNamespace("dkcc2005", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/03/15/");
		namespaces.addNamespace("medcom", "http://www.medcom.dk/dgws/2006/04/dgws-1.0.xsd");
		namespaces.addNamespace("itst2005", "http://rep.oio.dk/itst.dk/xml/schemas/2005/01/10/");
		namespaces.addNamespace("itst", "http://rep.oio.dk/itst.dk/xml/schemas/2006/01/17/");
		namespaces.addNamespace("dkcc2005-2", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/05/13/");
		namespaces.addNamespace("rs", "http://dkma.dk/receptserver/apotekssnitflade/xml/schemas/");
		namespaces.addNamespace("pem", "http://dkma.dk/receptserver/apotekssnitflade/xml/schemas/");
		namespaces.addNamespace("dkma", "http://rep.oio.dk/dkma.dk/xml/schemas/2006/01/15/");
		namespaces.addNamespace("sundcom", "http://rep.oio.dk/sundcom.dk/medcom.dk/xml/schemas/2005/08/07/");
		namespaces.addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
		namespaces.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		namespaces.addNamespace("ean", "http://rep.oio.dk/ean/xml/schemas/2005/01/10/");
		namespaces.addNamespace("sdsd", "http://www.sdsd.dk/dgws/2010/08");
		namespaces.addNamespace("sdsd201206", "http://www.sdsd.dk/dgws/2012/06");
		namespaces.addNamespace("sdsd201306", "http://www.sdsd.dk/dgws/2013/06");
		namespaces.addNamespace("topic20130101", "http://nsi.dk/topic/xml.schema/2013/01/01");
		namespaces.addNamespace("reimbursement20121201", "http://nsi.dk/reimbursement/xml.schema/2012/12/01");
		namespaces.addNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		namespaces.addNamespace("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		namespaces.addNamespace("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		namespaces.addNamespace("wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
		namespaces.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
		namespaces.addNamespace("wsa","http://www.w3.org/2005/08/addressing");
		namespaces.addNamespace("xenc","http://www.w3.org/2001/04/xmlenc#");
		namespaces.addNamespace("sbf","urn:liberty:sb");
		return namespaces;
	}
	
	
}
