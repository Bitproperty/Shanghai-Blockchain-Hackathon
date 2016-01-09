package com.coinomi.wallet.util;
/**
 * Created by andy on 16/1/8.
 */

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.*;
import com.android.volley.ParseError;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;


public class


























































































































































XMLRequest extends Request<XmlPullParser> {
    // confidential information, not showed in hackathon
    private static final String ID = "XXXXXXXXXX";
    private static final String PASS = "XXXXXXXXX";

    private final Response.Listener<XmlPullParser> mListener;
    private Map<String, String> mChallengeHeader = new HashMap<String, String>();

    public XMLRequest(int method, String url, Response.Listener<XmlPullParser> listener,
                      Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        setRetryPolicy(new DigestRetryPolicy());

    }

    public XMLRequest(String url, Response.Listener<XmlPullParser> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected Response<XmlPullParser> parseNetworkResponse(NetworkResponse response) {
        try {
            String xmlString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlString));
            return Response.success(xmlPullParser, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (XmlPullParserException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(XmlPullParser response) {
        mListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> newHeader = new HashMap<String, String>(super.getHeaders());
        if (mChallengeHeader.get("WWW-Authenticate") == null) {
            return newHeader;
        }

        DigestScheme ds = new DigestScheme();
        try {
            ds.processChallenge(new BasicHeader("WWW-Authenticate",
                    mChallengeHeader.get("WWW-Authenticate")));
        } catch (MalformedChallengeException e) {
            e.printStackTrace();
            throw new AuthFailureError();
        }

        Header header = null;
        try {
            header = ds.authenticate(new UsernamePasswordCredentials(ID, PASS),
                    new BasicHttpRequest(HttpGet.METHOD_NAME, getUrl()));
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw new AuthFailureError();
        }

        newHeader.put(header.getName(), header.getValue());

        return newHeader;
    }

    public void setChallengeHeader(Map<String, String> header) {
        this.mChallengeHeader = header;
    }

    class DigestRetryPolicy extends DefaultRetryPolicy {
        private int retryCount;


        public DigestRetryPolicy() {
            super(1000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            retryCount = 3;
        }


        @Override
        public void retry(VolleyError error) throws VolleyError {
            if (error.networkResponse.statusCode == HttpStatus.SC_UNAUTHORIZED) {
                if (retryCount == 0) {
                    throw new VolleyError();
                    //super.retry(error);
                }
                //Log.d(TAG, "DigestRetryPolicy#retry");

                setChallengeHeader(error.networkResponse.headers);
                retryCount--;
            } else {
                throw new VolleyError();
                //super.retry(error);
            }
        }
    }


}