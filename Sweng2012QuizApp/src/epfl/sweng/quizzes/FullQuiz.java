package epfl.sweng.quizzes;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.quizquestions.Question;

public class FullQuiz extends Quiz {
	
	private Question mFirstQuestion;
	private int nbQuestions;
	

	public FullQuiz(JSONObject json) throws JSONException{
		super(json);
		JSONArray array = json.getJSONArray("questions");
		Question prev = null;
		nbQuestions = array.length();
		for (int i=0;i<nbQuestions;++i) {
			Question current = new Question(array.getJSONObject(i));
			if (i == 0) {
				mFirstQuestion = current;
			}
			else {
				prev.setNext(current);
			}
			prev = current;
		}
		if (array.length() > 0) {
			prev.setNext(mFirstQuestion);
		}
	}
	
	public Question getFirstQuestion() {
		return mFirstQuestion;
	}
	public int size() {
		return nbQuestions;
	}

}
