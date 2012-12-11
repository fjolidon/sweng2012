package epfl.sweng.servercomm.search;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import epfl.sweng.quizquestions.QuizQuestion;

/** 
 *  Get from the server questions by specifying either an owner or a tag name.
 */
public interface QuestionSearchCommunication {
    List<QuizQuestion> getQuestionsByOwner(String owner) throws IOException, JSONException;
    List<QuizQuestion> getQuestionsByTag(String tag) throws IOException, JSONException;
}
