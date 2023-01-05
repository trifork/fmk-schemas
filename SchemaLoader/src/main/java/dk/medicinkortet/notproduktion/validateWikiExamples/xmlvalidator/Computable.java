package dk.medicinkortet.notproduktion.validateWikiExamples.xmlvalidator;

public interface Computable<A,V> {
	
	public V compute(A a) throws InterruptedException;

}
