package epfl.sweng.showquestions;


import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.R;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.ResponseListener;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import android.os.Bundle;
import android.app.Activity;
import android.widget.AdapterView.OnItemClickListener;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowQuestionsActivity extends Activity implements ResponseListener, SwengQuizURLs, HttpStatusCodes {

	private TextView questionView;
	private ListView answersView;
	
	private QuizQuestion currentQuestion;
	
	private Button nextQuestionButton;
	
	private RatingHandler ratingHandler;
	
	//set when we failed to fetch some data (eg the rating) so we don't display any
	//other error message if anything else went wrong (so we dont flood the user with error message)
	private boolean failure = false;
	
	private final static String RATING_GET_FAIL = "There was an error retrieving the ratings";
	private final static String RATING_POST_FAIL = "There was an error setting the rating";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_questions);
        
        questionView = (TextView) findViewById(R.id.question);
        answersView = (ListView) findViewById(R.id.answersList);
        nextQuestionButton = (Button) findViewById(R.id.nextButton);
        
        ratingHandler = new RatingHandler();
    	ratingHandler.setEnabled(false);
        
        
        ClickListener l = new ClickListener();
        
        answersView.setOnItemClickListener(l);
        nextQuestionButton.setOnClickListener(l);

        questionView.setEnabled(true);
        answersView.setEnabled(false);
        nextQuestionButton.setEnabled(false);

       
        fetchQuestion();
    }
    private void displayQuestion(QuizQuestion question) {
    	currentQuestion = question;
    	failure = false;
    	questionView.setText(question.getQuestion());
    	answersView.setAdapter(question.getArrayAdapter(this));
    	answersView.setEnabled(true);
    	ratingHandler.resetRating();
    	nextQuestionButton.setEnabled(false);
    }
    

    @Override
	public void readResponse(ServerResponse response) {
		if (response == null || response.getStatusCode() != HTTP_OK) {
			fetchFailure();
		}
		else {
			try {
				displayQuestion(new QuizQuestion(response.getEntity()));
				fetchRatings();
			}
			catch(JSONException ex) {
				fetchFailure();
			}
			
		}
	}
    
    private void fetchQuestion() {
    	ServerCommunication.sendAsynchrousGetRequest(this, URL_RANDOM);
    }
    private void fetchRatings() {
    	final int id = currentQuestion.getId();
    	ServerCommunication.sendAsynchrousGetRequest(new ResponseListener() {
			public void readResponse(ServerResponse response) {
				receiveRatingAll(id, response);
			}
		}, URL_RATING_PREFIX+id+URL_RATING_ALL);
    	
    	ServerCommunication.sendAsynchrousGetRequest(new ResponseListener() {
			public void readResponse(ServerResponse response) {
				receiveRatingSelf(id, response);
			}
		}, URL_RATING_PREFIX+id+URL_RATING_SELF);
    	
    }
    private void displayErrorMessage(String message) {
    	if (!failure) {
        	failure = true;
        	Toast.makeText(this,
    				message,
    				Toast.LENGTH_SHORT).show();
    	}
    }
    private void receiveRatingAll(int questionID, ServerResponse response) {
    	//ignores it if the request is outdated (ie related to an older question)
    	if (currentQuestion.getId() == questionID) {
    		if (response == null || response.getStatusCode() != HTTP_OK) {
        		displayErrorMessage(RATING_GET_FAIL);
    		}
    		else {
    			try {
    				int like, dislike, incorrect;
    				JSONObject entity = response.getEntityAsJSON();
    				like = entity.getInt("likeCount");
    				dislike = entity.getInt("dislikeCount");
    				incorrect = entity.getInt("incorrectCount");
    				ratingHandler.updateRatingAll(like, dislike, incorrect);
    			}
    			catch(JSONException ex) {
    	    		displayErrorMessage(RATING_GET_FAIL);
    			}
    			
    		}
    	}
    }
    private void receiveRatingSelf(int questionID, ServerResponse response) {
    	//ignores it if the request is outdated (ie related to an older question)
    	if (currentQuestion.getId() == questionID) {
    		if (response == null || (response.getStatusCode() != HTTP_OK && response.getStatusCode() != HTTP_NO_CONTENT)) {
    			displayErrorMessage(RATING_GET_FAIL);
    		}
    		else {
    			if (response.getStatusCode() == HTTP_OK) {
    				try {
        				String rating = response.getEntityAsJSON().getString("verdict");
        				ratingHandler.updateRatingSelf(Rating.get(rating));
        			}
        			catch(JSONException ex) {
        	    		displayErrorMessage(RATING_GET_FAIL);
        			}
    			}
    			//nothing to do actually, just enable ratingHandler, which is done either way
    			/*else { // NO_CONTENT
    				
    			} */
    			ratingHandler.setEnabled(true);
    		}
    	}
    }
    
    
    
    private void showError(String e) {
    	questionView.setText(e);
    	answersView.setAdapter(null);
    	ratingHandler.setEnabled(false);
    	nextQuestionButton.setEnabled(true);
    }
    private void fetchFailure() {
    	showError("There was an error retrieving the question!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_questions, menu);
        return true;
    }
    
    private class ClickListener implements OnItemClickListener, OnClickListener {

		@SuppressWarnings({ "rawtypes" })
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int answerIndex,
				long arg3) {
			if (currentQuestion.checkAnswer(answerIndex)) {
				nextQuestionButton.setEnabled(true);
				answersView.setEnabled(false);
			}
			((ArrayAdapter) answersView.getAdapter()).notifyDataSetChanged();
			
		}

		@Override
		public void onClick(View v) {
			fetchQuestion();
			nextQuestionButton.setEnabled(false);
			
		}
    	
    }
    private class RatingHandler {
    	
    	private Rating currentRating = Rating.NONE;
    	private TextView ratingTextView;
    	private Map<Rating,IntButton> rating2Button = new HashMap<Rating, IntButton>();
    	
    	private RatingHandler() {
    		ratingTextView = (TextView)findViewById(R.id.likedTextView);
            
            
            for(Rating r : EnumSet.allOf(Rating.class)) {
            	if (r != Rating.NONE) {
            		rating2Button.put(r, new IntButton(r, this));
            	}
            }
    	}
    	
    	
    	private void resetRating() {
    		for (IntButton b:rating2Button.values()) {
    			b.setValue(0);
    		}
    		updateRatingSelf(Rating.NONE);
    	}
    	private void setEnabled(boolean enabled) {
    		for (IntButton b:rating2Button.values()) {
    			b.setEnabled(enabled);
    		}
    	}
    	private void updateRatingSelf(Rating rating) {
    		currentRating = rating;
    		ratingTextView.setText(rating.getTextViewText());
    	}
    	private void updateRatingAll(int like, int dislike, int incorrect) {
    		rating2Button.get(Rating.LIKE).increment(like);
    		rating2Button.get(Rating.DISLIKE).increment(dislike);
    		rating2Button.get(Rating.INCORRECT).increment(incorrect);
    	}
    	private void clicked(IntButton button) {
    		final Rating rating = button.getRating();
    		if (currentRating != rating) {
    			setEnabled(false);
        		try {
    				JSONObject json = new JSONObject();
    				json.put("verdict", rating.getJsonValue());
    				final int id = currentQuestion.getId();
    				
    				ServerCommunication.sendAsynchrousPostRequest(new ResponseListener() {
    					public void readResponse(ServerResponse response) {
    						receiveRatingSelf(rating, id, response);
    					}
    				}, URL_RATING_PREFIX+id+URL_RATING_SELF,
    				new StringEntity(json.toString()), true);
    				
    				
    			}
        		catch(UnsupportedEncodingException ex) {
        			ex.printStackTrace();
	    			displayErrorMessage(RATING_POST_FAIL);
        			
        		}
    			catch(JSONException ex) {
    				ex.printStackTrace();
	    			displayErrorMessage(RATING_POST_FAIL);
    			}
    		}

    	}


		
		private void receiveRatingSelf(Rating rating, int questionID, ServerResponse response) {
			//ignores it if the request is outdated (ie related to an older question)
	    	if (currentQuestion.getId() == questionID) {
	    		if (response == null || (response.getStatusCode() != HTTP_OK && response.getStatusCode() != HTTP_CREATED)) {
	    			displayErrorMessage(RATING_POST_FAIL);
	    			if (response == null) {
	    				System.out.println("null");
	    			}
	    			else {
	    				System.out.println(response.getStatusCode());
	    			}
	    		}
	    		else {
    				IntButton button = rating2Button.get(rating);
    				if (currentRating != Rating.NONE) {
    	    			rating2Button.get(currentRating).increment(-1);
    	    		}
    				button.increment(1);
    	    		updateRatingSelf(rating);
	    			setEnabled(true);
	    		}
	    	}

	    	
			
		}
    	
    	
    }
    
    private enum Rating {
    	NONE("","You have not rated this question","",0),
    	LIKE("Like","You like the question","like",R.id.likeButton),
    	DISLIKE("Dislike","You dislike the question","dislike",R.id.dislikeButton),
    	INCORRECT("Incorrect","You think the question is incorrect","incorrect",R.id.incorrectButton);
    	
    	private String buttonText;
    	private String textViewText;
    	private String jsonValue;
    	private int buttonID;
    	
    	private Rating(String bText, String viewText, String jsonText, int buttonID) {
    		buttonText = bText;
    		textViewText = viewText;
    		jsonValue = jsonText;
    		this.buttonID = buttonID;
    	}
		private String getButtonText() {
			return buttonText;
		}

		private String getTextViewText() {
			return textViewText;
		}

		private String getJsonValue() {
			return jsonValue;
		}
		private static Rating get(String jsonStr) {
			for (Rating r : values()) {
				if (r.getJsonValue().equals(jsonStr)) {
					return r;
				}
			}
			return NONE;
		}
    	
    }
    private class IntButton implements OnClickListener {
    	
    	private Button button;
    	private int counter = 0;
    	private Rating rating;
    	private RatingHandler handler;
    	
    	private IntButton(Rating rating, RatingHandler handler) {
    		this(rating,handler,0);
    	}
    	
    	private IntButton(Rating rating, RatingHandler handler, int counter) {
    		this.counter = counter;
    		this.rating = rating;
    		this.handler = handler;
    		button = (Button)findViewById(rating.buttonID);
    		button.setOnClickListener(this);
    	}
    	private void updateText() {
    		button.setText(rating.getButtonText() + " ("+counter+")");
    	}
    	private void setValue(int value) {
    		counter = value;
    		updateText();
    	}

		@Override
		public void onClick(View v) {
			handler.clicked(this);
		}

		private void increment(int value) {
			counter += value;
			updateText();
		}
		private void setEnabled(boolean enabled) {
			button.setEnabled(enabled);
		}
		private Rating getRating() {
			return rating;
		}
    }
}
