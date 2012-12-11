package epfl.sweng.authentication;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import epfl.sweng.servercomm.ServerCommunication;
import epfl.sweng.servercomm.ServerResponse;
import epfl.sweng.util.HttpStatusCodes;
import epfl.sweng.util.SwengQuizURLs;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

public class AuthentificationState implements HttpStatusCodes, SwengQuizURLs {
	
	private static AuthentificationState uniqueInstance = new AuthentificationState();
	private SharedPreferences pref = null;
	private String sessionID = null; 
	
	private boolean isAuth = false;

	private final static String URL_LOGIN_TEQUILA = "https://tequila.epfl.ch/cgi-bin/tequila/login";


	private AuthentificationState() {
		
	}
	
	public synchronized void loadPrefereces(SharedPreferences sharedPref) {
		if (pref == null) {
			pref = sharedPref;
			sessionID = sharedPref.getString("SESSION_ID",null);
			if (sessionID != null) {
				isAuth = true;
			}
		}
	}
	
	
	public static AuthentificationState getInstance() {
		return uniqueInstance;
	}
	
	public synchronized boolean isAuthenticated() {
		return isAuth;
	}
	public synchronized String getSessionID() {
		return sessionID;
	}
	public synchronized void login(final AuthenticationActivity caller, final String user, final String password) {
		if (pref == null) {
			System.err.println("Tried to login without a SharedPreference loaded!");
		}
		else {
			new AsyncTask<Void, Integer, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					try {
						ServerResponse response = ServerCommunication.sendGetRequest(URL_LOGIN_SWENG);
						String token = response.getEntityAsJSON().getString("token");
						if (token == null) {
							return false;
						}
												
						List<NameValuePair> list = new LinkedList<NameValuePair>();
						list.add(new BasicNameValuePair("requestkey", token));
						list.add(new BasicNameValuePair("username", user));
						list.add(new BasicNameValuePair("password", password));
						
						response = ServerCommunication.sendPostRequest(URL_LOGIN_TEQUILA, new UrlEncodedFormEntity(list), false);
						
						
						switch (response.getStatusCode()) {
						case HTTP_FOUND: //if auth was successful
							break;
						case HTTP_OK: //if auth failed
							return false;
						default: //if anything else went wrong...
							return false;
						}
						
						
						JSONObject json = new JSONObject();
						json.put("token", token);
						response = ServerCommunication.sendPostRequest(URL_LOGIN_SWENG, new StringEntity(json.toString()),true);

						
						if (response.getStatusCode() != HTTP_OK) {
							System.err.println("Server returned non-OK status code: "+response.getStatusCode());
							return false;
						}
						
						synchronized (AuthentificationState.this) {
							sessionID = response.getEntityAsJSON().getString("session");
							
							
							Editor edit = pref.edit();
							edit.putString("SESSION_ID", sessionID);
							edit.commit();
							
							isAuth = true;
						}
						
					}
					catch(Exception ex) { //if anything went wrong, abort authentification
						//ex.printStackTrace();
						return false;
					}
					return true;
				}
				protected void onPostExecute(Boolean result) {
					if (result) {
						caller.loginSucceeded();
					}
					else {
						caller.loginFailed();
					}
				}
				
				
			}.execute();
		}
	}
	
	public synchronized void logout() {
		if (pref == null) {
			System.err.println("Tried to logout without a SharedPreference loaded!");
		}
		else {
			sessionID = null;
			
			Editor edit = pref.edit();
			edit.remove("SESSION_ID");
			edit.commit();
			
			isAuth = false;
		}
	}
}
