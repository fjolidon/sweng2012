package epfl.sweng.entry;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.sweng.R;
import epfl.sweng.authentication.AuthenticationActivity;
import epfl.sweng.authentication.AuthentificationState;
import epfl.sweng.editquestions.EditQuestionActivity;
import epfl.sweng.karma.KarmaLevel;
import epfl.sweng.karma.KarmaManager;
import epfl.sweng.quizzes.ShowAvailableQuizzesActivity;
import epfl.sweng.servercomm.ResponseListener;
import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.showquestions.ShowQuestionsActivity;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, ResponseListener, HttpStatusCodes, SwengQuizURLs {

	
	
	
	private Map<View,Class<?>> button2Activity = new HashMap<View,Class<?>>();
	private Button logoutButton;
	private TextView karmaLevel;
	private TextView karmaHint;
	private final static String KARMA_ERROR_MSG = "There was an error retrieving the karma";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthentificationState.getInstance().loadPrefereces(getSharedPreferences("user_session", 0));
        initButton(findViewById(R.id.showQuestionButton),ShowQuestionsActivity.class);
        initButton(findViewById(R.id.submitQuestionButton),EditQuestionActivity.class);
        initButton(findViewById(R.id.takeAQuizButton),ShowAvailableQuizzesActivity.class);
        //initButton(findViewById(R.id.searchQuestionsButton),SearchQuestionActivity.class);
        
        karmaLevel = (TextView)findViewById(R.id.karmaLevel);
        karmaHint = (TextView)findViewById(R.id.karmaHint);
        
        logoutButton = (Button)findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);
        if (AuthentificationState.getInstance().isAuthenticated()) {
        	updateKarma();
        }
    }
    
    public void onStart() {
    	super.onStart();
        checkAuth();
        System.out.println();
    }
    
    private void checkAuth() {
    	boolean auth = AuthentificationState.getInstance().isAuthenticated();
    	for (View v: button2Activity.keySet()) {
    		v.setEnabled(auth);
    	}
    	logoutButton.setEnabled(auth);

    	
    	if (!auth) {
            startActivityForResult(new Intent(this,AuthenticationActivity.class),0);
        }
    }
    
    private void initButton(View view, Class<?> activity) {
    	button2Activity.put(view, activity);
    	view.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    

	public void onClick(View v) {
		if (AuthentificationState.getInstance().isAuthenticated()) {
			if (v == logoutButton) {
				System.out.println("lol");
				AuthentificationState.getInstance().logout();
				checkAuth();
			}
			else {
				Class<?> c = button2Activity.get(v);
				if (c == null) {
					System.err.println("Unknown button!");
				}
				else {
					startActivity(new Intent(this,c));
				}
			}
		}

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkAuth();
        updateKarma();
    }

	@Override
	public void readResponse(ServerResponse response) {
		KarmaManager karma = KarmaManager.get();
		if (response == null) {
			karma.setLevel(KarmaLevel.UNKNOWN, KARMA_ERROR_MSG);
			System.out.println("null");
		}
		else {
			if (response.getStatusCode() != HTTP_OK) {
				karma.setLevel(KarmaLevel.UNKNOWN, KARMA_ERROR_MSG);
				System.out.println("status code: "+response.getStatusCode());
			}
			else {
				try {
					JSONObject entity = response.getEntityAsJSON();
					String levelStr = entity.getString("karma");
					String hint = entity.getString("hint");
					KarmaLevel level = KarmaLevel.UNKNOWN;
					
					for (KarmaLevel l : KarmaLevel.values()) {
						if (levelStr.equalsIgnoreCase(l.toString())) {
							level = l;
							break;
						}
					}
					KarmaManager.get().setLevel(level, hint);
					
				}
				catch(JSONException ex) {
					ex.printStackTrace();
					karma.setLevel(KarmaLevel.UNKNOWN, KARMA_ERROR_MSG);
				}
			}
		}
		karmaLevel.setText("Karma: "+karma.getLevel().toString());
		karmaHint.setText(karma.getHint());
		
	}
	private void updateKarma() {
		ServerCommunication.sendAsynchrousGetRequest(this, URL_KARMA);
	}
    	
    
}
