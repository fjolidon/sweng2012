package epfl.sweng.servercomm.search;

/**
 * Factory class for constructing QuestionSearchCommunication objects.
 *
 */
public class QuestionSearchCommunicationFactory {
    private static QuestionSearchCommunication questionSearch;
    
    public static synchronized QuestionSearchCommunication getInstance() {
        if (questionSearch == null) {
        	questionSearch = new DefaultQuestionSearchCommunication();
        }
        return questionSearch;
    }
    
    public static synchronized void setInstance(QuestionSearchCommunication instance) {
        questionSearch = instance;
    }
}
