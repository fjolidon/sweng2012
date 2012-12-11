package epfl.sweng.servercomm.search;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import epfl.sweng.util.Util;



public class DefaultQuestionSearchCommunication implements
		QuestionSearchCommunication, SwengQuizURLs, HttpStatusCodes{

	@Override
	public List<QuizQuestion> getQuestionsByOwner(String owner)
			throws IOException, JSONException {
		if (!Util.isLetterOrDigit(owner)) {
			throw new IllegalArgumentException();
		}
		return getQuestionsFromURL(URL_SEARCH_BY_OWNER+owner);
	}

	@Override
	public List<QuizQuestion> getQuestionsByTag(String tag) throws IOException,
			JSONException {
		if (!Util.isLetterOrDigit(tag)) {
			throw new IllegalArgumentException();
		}
		return getQuestionsFromURL(URL_SEARCH_BY_TAG+tag);
	}
	
	private List<QuizQuestion> getQuestionsFromURL(String url) throws IOException,
			JSONException {
		ServerResponse response = ServerCommunication.sendGetRequest(url);
		

		int code = response.getStatusCode();
		
		List<QuizQuestion> result = new LinkedList<QuizQuestion>();
		
		switch(code) {
		case HTTP_NOT_FOUND:
			return result;
		case HTTP_OK:
			break;
		default:
			throw new IOException("Unexpected response from server: "+code);
		}
		
		JSONArray array = new JSONArray(response.getEntity());
		
		for (int i=0;i<array.length();++i) {
			result.add(new QuizQuestion(array.getJSONObject(i)));
		}
		
		return result;
	}

}
