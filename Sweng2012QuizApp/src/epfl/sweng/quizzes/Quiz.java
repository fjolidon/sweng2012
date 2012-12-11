package epfl.sweng.quizzes;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Quiz {
	private int id;
	private String name;
	public Quiz(int id, String name) {
		this.id = id;
		this.name = name;
	}
	public Quiz(JSONObject obj) throws JSONException {
		id = obj.getInt("id");
		name = obj.getString("title");
		
	}
	public static List<Quiz> generateList(String json) throws JSONException {
		List<Quiz> result = new ArrayList<Quiz>();
		JSONArray array = new JSONArray(json);
		for (int i=0;i<array.length();++i) {
			try {
				JSONObject obj = array.getJSONObject(i);
				result.add(new Quiz(obj));
			}
			catch(JSONException ex) {
				System.err.println("One quiz was removed from the list because it could not be parsed.");
			}
		}
		return result;
	}
	
	
	
	public int getID() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String toString() {
		return name;
	}
	
	
	

}
