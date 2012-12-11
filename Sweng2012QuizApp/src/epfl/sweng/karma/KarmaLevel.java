package epfl.sweng.karma;

/** Enum for Karma levels*/
public enum KarmaLevel {
	LOW, MEDIUM, HIGH, UNKNOWN;

	@Override
	public String toString() {
		switch (this) {
			case LOW:
				return "Low";
			case MEDIUM:
				return "Medium";
			case HIGH:
				return "High";
			default:
				return "Unknown";
		}
	};
}
