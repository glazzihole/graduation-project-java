package nl.inl.blacklab.core.search.matchfilter;

public class ConstraintValueInt extends ConstraintValue {

	int i;

	ConstraintValueInt(int i) {
		this.i = i;
	}

	public int getValue() {
		return i;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		ConstraintValueInt other = (ConstraintValueInt) obj;
		if (i != other.i) {
            return false;
        }
		return true;
	}

	@Override
	public boolean isTruthy() {
		return true;
	}

	@Override
	public String toString() {
		return "" + i;
	}

}
