package epfl.sweng.editquestions;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.entity.StringEntity;

import epfl.sweng.R;
import epfl.sweng.quizquestions.QuizQuestion;
import epfl.sweng.servercomm.ResponseListener;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EditQuestionActivity extends Activity implements OnClickListener, TextWatcher, ResponseListener, SwengQuizURLs, HttpStatusCodes{

	private EditText questionText;
	private LinearLayout answers;
	private EditText tags;
	
	private Button addAnswer;
	
	private Button submitButton;
	//reference to the correctness button of the correct answer, so we can change it to incorrect if we set
	//another answer to correct
	private Button correctAnswerButton = null;
	
	private final static String CORRECT_CHAR = "\u2714";
	private final static String INCORRECT_CHAR = "\u2718";
	
	private final static String SUBMIT_SUCCESS_MSG = "Question was successfully sent to the server!";
	private final static String SUBMIT_FAIL_MSG = "Failed to send question to the server!";
	
    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question);
        
        questionText = (EditText)findViewById(R.id.editQuestionText);
        answers = (LinearLayout)findViewById(R.id.answersList);
        tags = (EditText)findViewById(R.id.editTags);
        
        tags.addTextChangedListener(this);
        
        addAnswer = (Button)findViewById(R.id.addAnswerButton);
        
        submitButton = (Button)findViewById(R.id.editSubmitButton);
        
        addAnswer.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        
        questionText.addTextChangedListener(this);
        
        
        initNewQuestion();
    }
    
    private void initNewQuestion() {
    	//clean up answers
    	answers.removeAllViews();
    	questionText.setText("");
    	tags.setText("");
    	
    	addAnswer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_question, menu);
        return true;
    }
    private void addAnswer() {
    	LinearLayout answerView = (LinearLayout)getLayoutInflater().inflate(R.layout.edit_answer, null);
    	answerView.findViewById(R.id.removeAnswerButton).setOnClickListener(this);
    	answerView.findViewById(R.id.editAnswerCorrectButton).setOnClickListener(this);
    	((EditText)answerView.findViewById(R.id.editAnswer)).addTextChangedListener(this);
    	answers.addView(answerView);
    	submitButton.setEnabled(false);
    }
    private QuizQuestion getQuestion() {
    	int correctSolution = -1;
    	List<String> answerList = new LinkedList<String>();
    	for (int i = 0; i < answers.getChildCount(); ++i) {
			LinearLayout layout = (LinearLayout) answers.getChildAt(i);
			answerList.add(((EditText) layout.findViewById(R.id.editAnswer)).getText().toString());
			if (((Button) layout.findViewById(R.id.editAnswerCorrectButton)).getText().toString().equals(CORRECT_CHAR)) {
				correctSolution = i;
			}
    	}
    	
    	
    	String tags = this.tags.getText().toString();
    	String output = "";
    	boolean lastCharAlphaNum = false;
    	for (int i=0;i<tags.length();++i) {
    		char c = tags.charAt(i);
    		if ((c >= 'a' && c <= 'z') ||
    			(c >= 'A' && c <= 'Z') ||
    			(c >= '0' && c <= '9')) { //char is alhpanum
    			lastCharAlphaNum = true;
    			output += c;
    		}
    		else if (lastCharAlphaNum) {
				output += ' ';
				lastCharAlphaNum = false;
    		}
    	}
    	tags = output.trim();
    	
    	Set<String> tagSet = new HashSet<String>();
    	String[] tagsArray = tags.split(" ");
    	
    	if (tagsArray.length == 1 && tagsArray[0].length() == 0) {
    		//if the field is empty, we will end up with an array containing an empty string, instead of an empty array like we want
    		tagsArray = new String[0];
    	}
    	for (String s : tagsArray) {
    		tagSet.add(s);
    	}
    	//id and owner aren't set because these will be assigned by the server
    	return new QuizQuestion(questionText.getText().toString(), answerList, correctSolution, tagSet, -1, null);
    	
    }
    private void checkQuestion() {
    	QuizQuestion question = getQuestion();
    	if (question.isSubmitable()) {
    		submitButton.setEnabled(true);
    	}
    	else {
    		submitButton.setEnabled(false);
    	}
    }
	@Override
	public void readResponse(ServerResponse response) {
		if (response == null || response.getStatusCode() != HTTP_CREATED) {
			submitQuestionResult(false);
		}
		else {
			submitQuestionResult(true);
		}
		
	}
	private void submitQuestionResult(boolean success) {
		Toast.makeText(this,
				success?SUBMIT_SUCCESS_MSG:SUBMIT_FAIL_MSG,
				Toast.LENGTH_SHORT).show();
			
		if (success) {
			initNewQuestion();
		}
	
		submitButton.setEnabled(true);
	}
    private void submitQuestion() {
    	QuizQuestion question = getQuestion();
    	if (question.isSubmitable()) { //no need to establish a connection if the question lacks some fields
    		try {
    			ServerCommunication.sendAsynchrousPostRequest(this, URL_SUBMIT_QUESTION, new StringEntity(question.getJSON().toString()), true);
    		}
    		catch(UnsupportedEncodingException ex) {
    			submitQuestionResult(false);
    		}
    	}
    	else {
			Toast.makeText(this,
					"Couldn't send the question because some required fields were missing!",
					Toast.LENGTH_SHORT).show();
    	}
    	
    }
	@Override
	public void onClick(View v) {
		if (v == addAnswer) {
			addAnswer();
		}
		else if (v == submitButton) {
			//disables the button during the operation, so user can't trigger it again
			submitButton.setEnabled(false);
			submitQuestion();
		}
		else if (v.getId() == R.id.editAnswerCorrectButton) {
			Button currentButton = (Button)v;
			if (currentButton != correctAnswerButton) {
				if (correctAnswerButton != null) {
					correctAnswerButton.setText(R.string.incorrect);
				}
				currentButton.setText(R.string.correct);
				correctAnswerButton = currentButton;
			}
		}
		else if (v.getId() == R.id.removeAnswerButton) {
			answers.removeView((View)v.getParent());
		}
		
		if (v != submitButton) {
			checkQuestion();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		checkQuestion();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
	
	
	public int auditGui() {
		int result = 0;
		
		//Ex 2
		if (!questionText.getHint().equals("Type in the question's text body")
				|| questionText.getVisibility() != View.VISIBLE) {
			++result;
		}
		boolean answerFailed = false;
		for (int i = 0; i < answers.getChildCount(); ++i) {
			LinearLayout layout = (LinearLayout) answers.getChildAt(i);
			EditText text = (EditText) layout.findViewById(R.id.editAnswer);
			if (!text.getHint().equals("Type in the answer") ||
					text.getVisibility() != View.VISIBLE) {
				answerFailed = true;
			}
    	}
		if (answerFailed) {
			++result;
		}
		if (!tags.getHint().equals("Type in the question's tags")
				|| tags.getVisibility() != View.VISIBLE) {
			++result;
		}
		
		//Ex 3
		if (!addAnswer.getText().toString().equals("+")
				|| addAnswer.getVisibility() != View.VISIBLE) {
			++result;
		}

		if (!submitButton.getText().toString().equals("Submit")
				|| submitButton.getVisibility() != View.VISIBLE) {
			++result;
		}
		boolean removeFailed = false;
		boolean correctFailed = false;
		for (int i = 0; i < answers.getChildCount(); ++i) {
			LinearLayout layout = (LinearLayout) answers.getChildAt(i);
			Button button = (Button) layout.findViewById(R.id.removeAnswerButton);
			if (!button.getText().toString().equals("-") ||
					button.getVisibility() != View.VISIBLE) {
				removeFailed = true;
			}
			button = (Button) layout.findViewById(R.id.editAnswerCorrectButton);
			if ((!button.getText().toString().equals(CORRECT_CHAR) &&
					!button.getText().toString().equals(INCORRECT_CHAR)) ||
					button.getVisibility() != View.VISIBLE) {
				correctFailed = true;
			}
    	}
		if (removeFailed) {
			++result;
		}
		if (correctFailed) {
			++result;
		}
		
		
		//Ex 4
		boolean correctExists = false;
		boolean correctAmountFailed = false;
		for (int i = 0; i < answers.getChildCount(); ++i) {
			LinearLayout layout = (LinearLayout) answers.getChildAt(i);
			Button button = (Button) layout.findViewById(R.id.editAnswerCorrectButton);
			if (button.getText().toString().equals(CORRECT_CHAR)) {
				if (correctExists) {
					correctAmountFailed = true;
				}
				else {
					correctExists = true;
				}
			}
    	}
		if (correctAmountFailed) {
			++result;
		}
		
		//Ex 5
		if (submitButton.isEnabled() != getQuestion().isSubmitable()) {
			++result;
		}
		
		return result;
	}

}
