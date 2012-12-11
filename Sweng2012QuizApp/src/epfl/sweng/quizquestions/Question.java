package epfl.sweng.quizquestions;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.ArrayAdapter;

public class Question {

	protected String question = null;
	protected String[] answers = null;
	
	private Question next;
	private Question previous;
	
	private int submittedAnswer = -1;
	
	public Question(String question, List<String> answers) {
		this.question = question;
		int i=0;
		this.answers = new String[answers.size()];
		for (String a : answers) {
			this.answers[i++] = a;
		}
		
	}
	public Question(JSONObject json) throws JSONException {
		question = json.getString("question");

		JSONArray array = json.getJSONArray("answers");
		answers = new String[array.length()];
		for (int i=0;i<array.length();++i) {
			answers[i] = (String)array.get(i);
		}
		
	}
	
	public String getQuestion() {
		return question;
	}

	public String getAnswer(int i) {
		return answers[i];
	}
	public int getNbAnswers() {
		return answers.length;
	}
	
	public void setNext(Question question) {
		next = question;
		question.previous = this;
	}
	
	public Question getNext() {
		return next;
	}
	
	public Question getPrevious() {
		return previous;
	}
	
	public void submitAnswer(int answer) {
		if (submittedAnswer == answer) {
			submittedAnswer = -1;
		}
		else {
			submittedAnswer = answer;
		}
	}
	public ArrayAdapter<String> getArrayAdapter(Context context) {
		return getArrayAdapter(context, answers);
	}
	private ArrayAdapter<String> getArrayAdapter(Context context, String[] data) {
		return new ArrayAdapter<String>(
        		context,
        		android.R.layout.simple_list_item_1,
        		android.R.id.text1,
        		data);
	}
	public ArrayAdapter<String> getArrayAdapterWithSubmittedAnswer(Context context) {
		if (submittedAnswer != -1) {
			String[] array = new String[answers.length];
			for (int i=0;i<array.length;++i) {
				array[i] = answers[i];
				if (submittedAnswer == i) {
					array[i] +=  " \u2724";
				}
			}
			return getArrayAdapter(context, array);
		}
		else {
			return getArrayAdapter(context, answers);
		}
	}
	public int getSubmittedAnswer() {
		return submittedAnswer;
	}


}
