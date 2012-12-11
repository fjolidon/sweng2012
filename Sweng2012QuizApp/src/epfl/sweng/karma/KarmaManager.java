package epfl.sweng.karma;

public class KarmaManager implements KarmaManagerInterface {
	
	private static KarmaManager uniqueInstance;
	
	private KarmaLevel mLevel = KarmaLevel.UNKNOWN;
	private String mHint = "";

	@Override
	public void setLevel(KarmaLevel level, String hint) {
		synchronized (KarmaManager.class) { //static sync
			mLevel = level;
			mHint = hint;
		}
	}

	@Override
	public KarmaLevel getLevel() {
		synchronized (KarmaManager.class) {
			return mLevel;
		}
	}

	@Override
	public String getHint() {
		synchronized (KarmaManager.class) {
			return mHint;
		}
	}
	public synchronized static KarmaManager get() {
		if (uniqueInstance == null) {
			uniqueInstance = new KarmaManager();
		}
		return uniqueInstance;
	}

}
