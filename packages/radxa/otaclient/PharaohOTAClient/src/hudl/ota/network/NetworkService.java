package hudl.ota.network;

import hudl.ota.Constants;
import hudl.ota.model.UpdateCheck;
import hudl.ota.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;

import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

public class NetworkService {

	private static NetworkService _instance;

	private static final int TIMEOUT_CONNECT = 10000;
	private static final int TIMEOUT_READ = 10000;

	private static final String HEADER_VALUE = "Dalvik/1.6.0 (Linux; U; Android 4.2.2; hudl ht7s3 Build/JDQ39)";

	public static NetworkService getInstance() {
		if (_instance == null) {
			_instance = new NetworkService();
		}
		return _instance;
	}

	public UpdateCheck getUpdateInformation(String buildVersion, String url, Context context, boolean isFirstJourney) {

		HttpClient client = getHttpClient();

		HttpGet request = new HttpGet(url + buildVersion);

		request.addHeader("X-DeviceID", Util.getWifiMACDeviceID(context));
		request.addHeader("X-FirstJourney", Boolean.toString(isFirstJourney));

		HttpResponse response;
		try {
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				byte[] raw = loadAll(response.getEntity().getContent());

				ObjectMapper mapper = new ObjectMapper();

				return mapper.readValue(raw, UpdateCheck.class);
			} else {

				Log.e(Constants.TAG, "Check status code: " + statusCode);
				return null;
			}

		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

	}

	/*
	 * Facility method to manage HttpClient configuration
	 */
	private HttpClient getHttpClient() {
		DefaultHttpClient client = new DefaultHttpClient();

		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_READ);

		params.setParameter(CoreProtocolPNames.USER_AGENT, HEADER_VALUE);

		client.setParams(params);

		return client;
	}

	protected byte[] loadAll(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[8192];
		int read;
		while ((read = input.read(buffer)) != -1) {
			baos.write(buffer, 0, read);
		}

		return baos.toByteArray();
	}

}
