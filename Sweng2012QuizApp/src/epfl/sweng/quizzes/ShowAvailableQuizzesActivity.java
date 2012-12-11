package epfl.sweng.quizzes;



import java.util.List;

import org.json.JSONException;

import epfl.sweng.R;
import epfl.sweng.servercomm.ResponseListener;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowAvailableQuizzesActivity extends Activity implements ResponseListener, SwengQuizURLs, HttpStatusCodes, OnItemClickListener{

	private final static String MSG_NO_QUIZ = "There are no quizzes available at the moment.";
	private final static String MSG_FAILED = "An error occurred while fetching quizzes.";
	
	private ListView quizList;
	
	private boolean noQuiz = true;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_available_quizzes);
        
        quizList = (ListView)findViewById(R.id.quizzesList);
        quizList.setOnItemClickListener(this);
        
        requestList();
        
        
    }
    private void displayMessage(String msg) {
    	String[] array = { msg };
    	quizList.setAdapter(new ArrayAdapter<String>(
    			this,
    			android.R.layout.simple_list_item_1,
        		android.R.id.text1,
        		array));
    }
    
    private void requestList() {
    	noQuiz = true;
    	ServerCommunication.sendAsynchrousGetRequest(this, URL_QUIZ_LIST);
    }

	@Override
	public void readResponse(ServerResponse response) {
		if (response != null && response.getStatusCode() == HTTP_OK) {
			try {
				List<Quiz> quizzes = Quiz.generateList(response.getEntity());
				if (quizzes.size() == 0) {
					displayMessage(MSG_NO_QUIZ);
				}
				else {
					quizList.setAdapter(new ArrayAdapter<Quiz>(
			    			this,
			    			android.R.layout.simple_list_item_1,
			        		android.R.id.text1,
			        		quizzes));
					noQuiz = false;
				}
			}
			catch(JSONException ex) {
				displayMessage(MSG_FAILED);
			}
		}
		else {
			displayMessage(MSG_FAILED);
		}
		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_available_quizzes, menu);
        return true;
    }
    @Override
	public void onItemClick(AdapterView<?> rawAdapter, View arg1, int index, long arg3) {
		if (!noQuiz) {

			@SuppressWarnings("unchecked")
			ArrayAdapter<Quiz> adapter = (ArrayAdapter<Quiz>)rawAdapter.getAdapter();
			Intent intent = new Intent(this,ShowQuizActivity.class);
			intent.putExtra("QuizID", adapter.getItem(index).getID());
			startActivity(intent);
		}
		
	}

}
