package epfl.sweng.karma;

public interface KarmaManagerInterface {

    /**
     * Sets the karma level and the hint. Can be called by the server
     * communication code to update the karma.
     * 
     * @param level
     *            the karma.
     * @param hint
     *            the advice to the user.
     */
    public void setLevel(KarmaLevel level, String hint);

    /**
     * @return The current karma.
     */
    public KarmaLevel getLevel();

    /**
     * @return The current hint.
     */
    public String getHint();
}
