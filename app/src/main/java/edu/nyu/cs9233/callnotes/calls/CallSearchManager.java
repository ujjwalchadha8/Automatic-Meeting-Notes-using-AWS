package edu.nyu.cs9233.callnotes.calls;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.Tokens;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import searchcalls.SearchcallsClient;

public class CallSearchManager {

    private final SearchcallsClient apiClient;

    public CallSearchManager() {
        apiClient = new ApiClientFactory().credentialsProvider(AWSMobileClient.getInstance()).build(SearchcallsClient.class);
    }


    //Updated function with arguments and code updates
    public void searchCalls(String key, Listeners.GetObjectListener<List<String>> listener) {

        ApiRequest localRequest =
                new ApiRequest(apiClient.getClass().getSimpleName())
                        .withPath("/search")
                        .withHttpMethod(HttpMethodName.GET)
                        .withHeaders(new HashMap<String, String>())
                        .addHeader("Content-Type", "application/json")
                        .withParameter("q", key);

//        final String body = "";
//        final byte[] content = body.getBytes(StringUtils.UTF8);
//        if (body.length() > 0) {
//            localRequest = localRequest
//                    .addHeader("Content-Length", String.valueOf(content.length))
//                    .withBody(content);
//        }

        // Make network call on background thread
        new ResponseTask(apiClient, localRequest, listener).execute();

    }

    private static class ResponseTask extends AsyncTask<Void, Void, List<String>> {

        private final SearchcallsClient apigClient;
        private final ApiRequest request;
        private final Listeners.GetObjectListener<List<String>> listener;

        ResponseTask(SearchcallsClient apigClient, ApiRequest request, Listeners.GetObjectListener<List<String>> listener) {
            this.apigClient = apigClient;
            this.request = request;
            this.listener = listener;
        }

        @Override @Nullable
        protected List<String> doInBackground(Void... aVoid) {
            try {
                Debug.log("Invoking API w/ Request : " +
                        request.getHttpMethod() + ":" +
                        request.getPath());

                final ApiResponse response = apigClient.execute(request);

                final InputStream responseContentStream = response.getContent();

                if (responseContentStream != null) {
                    String responseData = IOUtils.toString(responseContentStream);
                    JSONObject jsonObject = new JSONObject(responseData);
                    List<String> callIds = new ArrayList<>();
                    for (int i = 0; i < jsonObject.getJSONArray("callIds").length(); i++) {
                        callIds.add(jsonObject.getJSONArray("callIds").getString(0));
                    }
                    return callIds;
                }
                Log.d("RCODE", response.getStatusCode() + " " + response.getStatusText());
                return null;
            } catch (final Exception exception) {
                Log.e("", exception.getMessage(), exception);
                exception.printStackTrace();
                listener.onFailure(exception);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> callIds) {
            listener.onSuccess(callIds);
        }
    }
}
