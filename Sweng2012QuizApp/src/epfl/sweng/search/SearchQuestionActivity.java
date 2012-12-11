package epfl.sweng.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import epfl.sweng.R;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.ResponseListener;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.servercomm.search.QuestionSearchCommunication;
import epfl.sweng.servercomm.search.QuestionSearchCommunicationFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

public class SearchQuestionActivity extends Activity implements OnClickListener, ResponseListener {

	private Button searchButton;

	private RadioButton ownerButton;
	private RadioButton tagButton;
	
	private ListView resultList;

	private enum SearchType {
		OWNER,TAG;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_question);
        searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        ownerButton = (RadioButton)findViewById(R.id.radioOwner);
        tagButton = (RadioButton)findViewById(R.id.radioTag);
        resultList = (ListView)findViewById(R.id.searchResultsList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search_question, menu);
        return true;
    }
    
    private void displaySearch(List<QuizQuestion> questions) {
    	List<String> list = new ArrayList<String>(questions.size());
    	for (QuizQuestion q : questions) {
    		list.add(q.getQuestion());
    	}
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
    	
    	resultList.setAdapter(adapter);
    }

    @Override
	public void readResponse(ServerResponse response) {
		// TODO Auto-generated method stub
		
	}
    
    private void initSearch(final String searchText, final SearchType type) {
    	ServerCommunication.sendAsynchrousGetRequest(this, null);
    	
    	
    	new AsyncTask<Void, Void, List<QuizQuestion>>() {
    		
			protected List<QuizQuestion> doInBackground(Void... params) {
				QuestionSearchCommunication comm = QuestionSearchCommunicationFactory.getInstance();
				try {
					switch(type) {
					case TAG:
						return comm.getQuestionsByTag(searchText);
					case OWNER:
						return comm.getQuestionsByOwner(searchText);
					default:
						return null;
					}
				}
				catch(IOException ex) {
					ex.printStackTrace();
					return null;
				}
				catch(JSONException ex) {
					ex.printStackTrace();
					return null;
				}
			}
			protected void onPostExecute(List<QuizQuestion> questions) {
				if (questions == null) {
					Toast.makeText(SearchQuestionActivity.this,
							"An error occured during the search!",
							Toast.LENGTH_SHORT).show();
					displaySearch(new LinkedList<QuizQuestion>());
				}
				else {
					displaySearch(questions);
				}

				searchButton.setEnabled(true);
			}
    		
    		
    	}.execute();
    }
    
	@Override
	public void onClick(View v) {
		if (v == searchButton) {
			searchButton.setEnabled(false);
			String text = ((EditText)findViewById(R.id.searchNameOrTagEditText)).getText().toString();
			if (tagButton.isChecked()) {
				initSearch(text, SearchType.TAG);
			}
			else if (ownerButton.isChecked()) {
				initSearch(text, SearchType.OWNER);
			}
			else {
				searchButton.setEnabled(true);
			}

		}
	}

}
