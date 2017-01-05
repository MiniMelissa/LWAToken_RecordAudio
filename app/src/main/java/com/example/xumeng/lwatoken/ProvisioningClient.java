package com.example.xumeng.lwatoken;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
/**
 * Created by xumeng on 17/1/4.
 */
public class ProvisioningClient {
    private String endpoint;
    private SSLSocketFactory pinnedSSLSocketFactory;

//    public ProvisioningClient(Context context) throws Exception {
//        this.pinnedSSLSocketFactory = getPinnedSSLSocketFactory(context);
//    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    public void postCompanionProvisioningInfo(AppProvisioningInfo companionProvisioningInfo) throws IOException, JSONException {
        String jsonString = companionProvisioningInfo.toJson().toString();

        URL companionInfoEndpoint = new URL(endpoint + "/provision/companionInfo");

        HttpURLConnection connection = (HttpURLConnection) companionInfoEndpoint.openConnection();

        doRequest(connection, jsonString);
    }

    JSONObject doRequest(HttpURLConnection connection) throws IOException, JSONException {
        return doRequest(connection, null);
    }

    JSONObject doRequest(HttpURLConnection connection, String data) throws IOException, JSONException {
        int responseCode = -1;
        InputStream response = null;
        DataOutputStream outputStream = null;

        try {
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(pinnedSSLSocketFactory);
            }

            connection.setRequestProperty("Content-Type", "application/json");
            if (data != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(data.getBytes());
                outputStream.flush();
                outputStream.close();
            } else {
                connection.setRequestMethod("GET");
            }

            responseCode = connection.getResponseCode();
            response = connection.getInputStream();

            if (responseCode != 204) {
                String responseString = IOUtils.toString(response);
                JSONObject jsonObject = new JSONObject(responseString);
                return jsonObject;
            } else {
                return null;
            }
        } catch (IOException e) {
            if (responseCode < 200 || responseCode >= 300) {
                response = connection.getErrorStream();
                if (response != null) {
                    String responseString = IOUtils.toString(response);
                    throw new RuntimeException(responseString);
                }
            }
            throw e;
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(response);
        }
    }

//    private SSLSocketFactory getPinnedSSLSocketFactory(Context context) throws Exception {
//        InputStream caCertInputStream = null;
//        try {
//            caCertInputStream = context.getResources().openRawResource(R.raw.ca);
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            Certificate caCert = cf.generateCertificate(caCertInputStream);
//
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null, null);
//            trustStore.setCertificateEntry("myca", caCert);
//
//            TrustManagerFactory trustManagerFactory =
//                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(trustStore);
//
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
//            return sslContext.getSocketFactory();
//        } finally {
//            IOUtils.closeQuietly(caCertInputStream);
//        }
//    }

}
