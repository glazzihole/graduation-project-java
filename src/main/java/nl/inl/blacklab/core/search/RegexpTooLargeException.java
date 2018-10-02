package nl.inl.blacklab.core.search;

public class RegexpTooLargeException extends InvalidQueryException {

	public RegexpTooLargeException() {
		super("Regular expression too large.");
	}

}
