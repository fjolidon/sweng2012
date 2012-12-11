package epfl.sweng.quizquestions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class QuizQuestion extends Question{
	
	private int correctIndex = -1;
	private Set<String> tags = new HashSet<String>();
	private int id = -1;
	private String owner = null;
	private boolean answered = false;

	
	/** The constructor for quiz questions received as JSON strings from the Sweng2012QuizApp server
	* @param json The JSON string received from the Sweng2012QuizApp server
	*/
	public QuizQuestion(String json) throws JSONException {
		this(new JSONObject(json));
	}
	
	/** The constructor for quiz questions already parsed into as JSON object
	* @param json The JSON object with a question
	*/
	public QuizQuestion(JSONObject jsonObj) throws JSONException {
		super(jsonObj);
		id = jsonObj.getInt("id");
		correctIndex = jsonObj.getInt("solutionIndex");
		owner = jsonObj.getString("owner");
		
		JSONArray array = jsonObj.getJSONArray("tags");
		for (int i=0;i<array.length();++i) {
			tags.add((String)array.get(i));
		}
	}

	/** The constructor for quiz questions defined by the user
	* @param text The body of the question, as input by the user
	* @param answers The list of possible answers of the question, as input by the user
	* @param solutionIndex The index identifying the correct answer, as input by the user
	* @param tags The set of tags of the question, as input by the user
	* @param id The id of the question
	* @param owner The owner of the question
	*/
	public QuizQuestion(String text, List<String> answers, int solutionIndex, Set<String> tags, int id, String owner) {
		super(text, answers);
		correctIndex = solutionIndex;
		this.tags.addAll(tags);
		this.id = id;
		this.owner = owner;
		
	}
	
	public boolean checkAnswer(int i) {
		if (answered) {
			return false;
		}
		if (i == correctIndex) {
			answers[i] += "  \u2714";
			answered = true;
			return true;
		}
		if (!answers[i].endsWith("\u2718")) {
			answers[i] += "  \u2718";
		}
		
		return false;
	}
	public JSONObject getJSON() {
		try {
			JSONObject result = new JSONObject();
			if (question != null) {
				result.put("question", question);
			}
			if (answers != null && answers.length>0) {
				JSONArray answerArray = new JSONArray();
				for (String s : answers) {
					answerArray.put(s);
				}

				result.put("answers", answerArray);
			}
			if (correctIndex != -1) {
				result.put("solutionIndex", correctIndex);
			}

			JSONArray tagsArray = new JSONArray();
			for (String s : tags) {
				tagsArray.put(s);
			}
			result.put("tags", tagsArray);
		
			if (id != -1) {
				result.put("id", id);
			}
			if (owner != null) {
				result.put("owner", owner);
			}
			return result;
		}
		catch(JSONException ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	


	public int getCorrectIndex() {
		return correctIndex;
	}

	public Set<String> getTags() {
		Set<String> result = new HashSet<String>();
		result.addAll(tags);
		return result;
	}

	public int getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}
	//check if this question has all the field required before sending it to the server
	public boolean isSubmitable() {
		if (question == null || question.length() > 500 || question.trim().length() == 0) {
			return false;
		}
		if (answers == null || answers.length < 2 || answers.length > 10) {
			return false;
		}
		for (String s: answers) {
			if (s == null || s.length() > 500 || s.trim().length() == 0) {
				return false;
			}
		}
		if (correctIndex < 0 || correctIndex >= answers.length) {
			return false;
		}
		for (String s: tags) {
			if (s == null || s.length() > 500 || s.trim().length() == 0) {
				return false;
			}
		}
		
		
		return true;
	}
	
	
}
