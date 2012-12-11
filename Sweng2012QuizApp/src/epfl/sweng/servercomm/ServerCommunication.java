package epfl.sweng.servercomm;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.AsyncTask;

import epfl.sweng.authentication.AuthentificationState;

public class ServerCommunication extends AsyncTask<Void, Void, ServerResponse>{

	private ResponseListener mListener;

	
	private HttpUriRequest mRequest;
	

	private ServerCommunication(String request) {
		this(null, request);
	}
	private ServerCommunication(ResponseListener listener, String request) {
		mListener = listener;
		mRequest = new HttpGet(request);
	}
	private ServerCommunication(String request, HttpEntity entity, boolean contentIsJSON ) {
		this(null, request, entity, contentIsJSON);
	}
	private ServerCommunication(ResponseListener listener, String request, HttpEntity entity, boolean contentIsJSON ) {
		mListener = listener;
		HttpPost post = new HttpPost(request);
		post.setEntity(entity);
		if (contentIsJSON) {
			post.setHeader("Content-type", "application/json");
		}
		mRequest = post;
	}
	
	public static ServerResponse sendGetRequest(String request) throws IOException {
		return new ServerCommunication(request).sendRequest();
	}
	
	public static ServerResponse sendPostRequest(String request, HttpEntity entity, boolean contentIsJSON) throws IOException {
		return new ServerCommunication(request, entity, contentIsJSON).sendRequest();
	}
	private ServerResponse sendRequest() throws IOException  {
		AuthentificationState auth = AuthentificationState.getInstance();
		if (auth.isAuthenticated()) {
			mRequest.setHeader("Authorization", "Tequila "+AuthentificationState.getInstance().getSessionID());
		}
		return new ServerResponse(SwengHttpClientFactory.getInstance().execute(mRequest));

	}
	
	
	
	
	
	
	public static void sendAsynchrousGetRequest(ResponseListener listener, String request) {
		new ServerCommunication(listener, request).execute();
	}
	public static void sendAsynchrousPostRequest(ResponseListener listener, String request, HttpEntity entity, boolean contentIsJSON) {
		new ServerCommunication(listener, request, entity, contentIsJSON).execute();
	}
	
	
	@Override
	protected ServerResponse doInBackground(Void... params) {
		try {
			return sendRequest();
		}
		catch(IOException ex) {
			return null;
		}
	}
	protected void onPostExecute(ServerResponse result) {
		if (mListener != null) {
			mListener.readResponse(result);
		}
	}
	
}
