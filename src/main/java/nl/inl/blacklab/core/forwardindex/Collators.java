package nl.inl.blacklab.core.forwardindex;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

import nl.inl.blacklab.core.forwardindex.ForwardIndex.CollatorVersion;

/**
 * Collators to use for term equality testing for different sensitivity settings.
 */
public class Collators {

	private CollatorVersion version;

	private Collator sensitive;

	private Collator insensitive;

	public Collators(Collator base, CollatorVersion version) {
		super();
		this.version = version;
		sensitive = (Collator)base.clone();
		sensitive.setStrength(Collator.TERTIARY);
		insensitive = getCollatorInsensitive((RuleBasedCollator)base.clone(), version);
	}

	public Collator get(boolean caseSensitive, boolean diacSensitive) {
		if (caseSensitive != diacSensitive) {
			throw new UnsupportedOperationException("Different values for case- and diac-sensitive not supported here yet.");
		}
		return caseSensitive ? sensitive : insensitive;
	}

	/**
	 * Returns a case-/accent-insensitive version of the specified collator that also doesn't
	 * ignore dash or space (as most collators do by default in PRIMARY mode). This way, the
	 * comparison is identical to lowercasing and stripping accents before calling String.equals(),
	 * which is what we use everywhere else for case-/accent-insensitive comparison.
	 *
	 * @param coll collator to make insensitive
	 * @param collatorVersion version of the insensitive collator we want
	 * @return insensitive collator
	 */
	private static Collator getCollatorInsensitive(RuleBasedCollator coll, CollatorVersion collatorVersion) {
		switch (collatorVersion) {
		case V1:
			// Basic case- and accent-insensitive collator
			// Note that this ignores dashes and spaces, which is different
			// from how the rest of blacklab deals with term equality.
			// Hence V2.
			Collator cl = (Collator) coll.clone();
			cl.setStrength(Collator.PRIMARY);
			return cl;
		case V2:
		default:
			// Case- and accent-insensitive collator that doesn't
			// ignore dash and space like the regular insensitive collator (V1) does.
			String rules = coll.getRules().replaceAll(",'-'", ""); // don't ignore dash
			rules = rules.replaceAll("<'_'", "<' '<'-'<'_'"); // sort dash and space before underscore
			try {
				coll = new RuleBasedCollator(rules);
				coll.setStrength(Collator.PRIMARY);  // ignore case and accent differences
				return coll;
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Collators getDefault() {
		return new Collators(Collator.getInstance(), CollatorVersion.V2);
	}

	public CollatorVersion getVersion() {
		return version;
	}
}
