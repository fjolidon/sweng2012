package epfl.sweng.servercomm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerResponse {
	private int statusCode;
	private String body;
	
	public ServerResponse(HttpResponse response) throws IOException {
		statusCode = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			body = "";
		}
		else {
			long contentLength = entity.getContentLength();
			if (contentLength > Integer.MAX_VALUE) {
				throw new IOException("Cannot handle content length of more than "+Integer.MAX_VALUE);
			}
			if (contentLength == 0) { //if no content, don't bother to read the stream
				body = "";
			}
			else {
				ByteArrayOutputStream out;
				if (contentLength < 0) { //if content length is unknown, uses default buffer size
					out = new ByteArrayOutputStream();
				}
				else {
					out = new ByteArrayOutputStream((int)contentLength);
				}
				response.getEntity().writeTo(out);
				body = out.toString();
			}
		}
	}
	public int getStatusCode() {
		return statusCode;
	}
	public String getEntity() {
		return body;
	}
	public JSONObject getEntityAsJSON() throws JSONException {
		return new JSONObject(body);
	}
	

}
