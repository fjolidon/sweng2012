package epfl.sweng.quizzes;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.R;
import epfl.sweng.quizquestions.Question;
import epfl.sweng.servercomm.ResponseListener;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ShowQuizActivity extends Activity implements OnItemClickListener ,OnClickListener, ResponseListener, SwengQuizURLs, HttpStatusCodes {

	private FullQuiz quiz;
	private Question currentQuestion = null;
	
	private Button previous;
	private Button next;
	private Button submit;
	
	private ListView answers;
	
	private TextView questionText;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_quiz);
        
        previous = (Button)findViewById(R.id.quizPreviousButton);
        next = (Button)findViewById(R.id.quizNextButton);
        submit = (Button)findViewById(R.id.handInQuizButton);
        
        questionText = (TextView)findViewById(R.id.quizQuestion);
        
        answers = (ListView)findViewById(R.id.quizAnswerList);
        
        enableButtons(false);
        
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        submit.setOnClickListener(this);
        
        answers.setOnItemClickListener(this);
        
        ServerCommunication.sendAsynchrousGetRequest(this, URL_QUIZ+getIntent().getExtras().getInt("QuizID"));
        
    }
    
    private void enableButtons(boolean enabled) {
    	previous.setEnabled(enabled);
    	next.setEnabled(enabled);
    	submit.setEnabled(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_quiz, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		if (v == next) {
			displayQuestion(currentQuestion.getNext());
		}
		else if (v == previous) {
			displayQuestion(currentQuestion.getPrevious());
		}
		else if (v == submit) {
			enableButtons(false);
			JSONArray array = new JSONArray();
			Question q = quiz.getFirstQuestion();
			for (int i=0;i<quiz.size();++i) {
				int sub = q.getSubmittedAnswer();
				if (sub == -1) {
					array.put(JSONObject.NULL);
				}
				else {
					array.put(sub);
				}
				q = q.getNext();
			}
			JSONObject json = new JSONObject();
			try {
				json.put("choices",array);
				System.out.println(json.toString());
				ServerCommunication.sendAsynchrousPostRequest(new ResponseListener() {
					public void readResponse(ServerResponse response) {
						receiveQuizResult(response);
					}
				},
				URL_QUIZ+quiz.getID()+URL_QUIZ_SUBMIT_SUFFIX,
				new StringEntity(json.toString()),
				true);
			}
			catch(JSONException ex) {
				ex.printStackTrace();
				displayAlertDialog("An error occurred while handing in your answers");
			}
			catch(UnsupportedEncodingException ex) {
				ex.printStackTrace();
				displayAlertDialog("An error occurred while handing in your answers");
			}
		}
	}
	
	private void failedToFetchQuiz() {
		questionText.setText("An error occurred while loading the quiz.");
	}
	private void receiveQuizResult(ServerResponse response) {
		if (response != null && response.getStatusCode() == HTTP_OK) {
			try {
				double score = response.getEntityAsJSON().getDouble("score");
				DecimalFormat df = new DecimalFormat("#.##");
				displayAlertDialog("Your score is "+df.format(score));
			}
			catch(JSONException ex) {
				ex.printStackTrace();
				displayAlertDialog("An error occurred while handing in your answers");
			}
		}
		else {
			System.out.println(response.getStatusCode());
			displayAlertDialog("An error occurred while handing in your answers");
		}
	}
	
	private void displayAlertDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                enableButtons(true);
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void displayQuestion(Question question) {
		currentQuestion = question;
		
		questionText.setText(question.getQuestion());
		answers.setAdapter(question.getArrayAdapterWithSubmittedAnswer(this));



        enableButtons(true);
	}

	@Override
	public void readResponse(ServerResponse response) {
		if (response != null && response.getStatusCode() == HTTP_OK) {
			try {
				quiz = new FullQuiz(response.getEntityAsJSON());
				displayQuestion(quiz.getFirstQuestion());
			}
			catch(JSONException ex) {
				failedToFetchQuiz();
			}
		}
		else {
			failedToFetchQuiz();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View answer, int newAnswer, long arg3) {
		TextView newView = (TextView)answer;
		int lastAnswer = currentQuestion.getSubmittedAnswer();
		if (lastAnswer != -1) {
			TextView view = (TextView)answers.getChildAt(lastAnswer);
			String str = view.getText().toString();
			view.setText(str.substring(0, str.length()-2));
		}
		if (newAnswer != lastAnswer) {
			newView.setText(newView.getText().toString() + " \u2724");
		}
		currentQuestion.submitAnswer(newAnswer);
	}
}
