package epfl.sweng.authentication;

import epfl.sweng.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AuthenticationActivity extends Activity implements OnClickListener {

	
	private Button loginButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_authentication, menu);
        return true;
    }
    
    public void loginSucceeded() {
        setResult(Activity.RESULT_OK);
    	finish();
    }
    
    public void loginFailed() {
    	Toast.makeText(this, "Login failed!",
				Toast.LENGTH_SHORT).show();
    	loginButton.setEnabled(true);
    }

	@Override
	public void onClick(View v) {
		loginButton.setEnabled(false);
		String user = ((EditText)findViewById(R.id.authGasparUser)).getText().toString();
		String password = ((EditText)findViewById(R.id.authGasparPassword)).getText().toString();

		
		AuthentificationState.getInstance().login(this, user, password);
		
	}
}
