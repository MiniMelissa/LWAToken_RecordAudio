package com.example.xumeng.lwatoken;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends Activity {

    private static final String Tag= MainActivity.class.getName();

    private RequestContext mRequestContext;
    private View mLoginButton;
    private static final String PRODUCT_ID = "LWAToken";
    private static final String PRODUCT_DSN = "123456";
    private static final Scope ALEXA_ALL_SCOPE=ScopeFactory.scopeNamed("alexa:all");

    public String accessToken;

    private TextView textContent;
    private TextView loggoutTextView;
    private ProgressBar loginProgress;
    private boolean isLoggedIn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestContext=RequestContext.create(this);
        mRequestContext.registerListener(new AuthorizeListenerImpl());


        // Find the button with the login_with_amazon ID
        // and set up a click handler
        mLoginButton =  findViewById(R.id.login_with_amazon);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                doLogin();
                final JSONObject scopeData = new JSONObject();
                final JSONObject productInstanceAttributes = new JSONObject();

                try {
                    productInstanceAttributes.put("deviceSerialNumber", PRODUCT_DSN);
                    scopeData.put("productInstanceAttributes", productInstanceAttributes);
                    scopeData.put("productId", PRODUCT_ID);


                    AuthorizationManager.authorize(new AuthorizeRequest.Builder(mRequestContext)
                            .addScope(ScopeFactory.scopeNamed("alexa:all",scopeData))
                            .forGrantType(AuthorizeRequest.GrantType.ACCESS_TOKEN)
                            .shouldReturnUserData(false)
                            .build());
                } catch (JSONException e) {
                    // handle exception here
                    e.printStackTrace();
                }
//                getAccessToken();
                textContent.setText(accessToken);
            }
        });



        // Find the button with the logout ID and set up a click handler
        View logoutButton=findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                AuthorizationManager.signOut(getApplicationContext(), new Listener<Void, AuthError>() {
                    @Override
                    public void onSuccess(Void response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setLoggedOutState();
                            }
                        });
                    }

                    @Override
                    public void onError(AuthError authError) {
                        Log.e(Tag,"Error clearing authorizaiton state.",authError);
                    }
                });
            }
        });

        String logoutText="Logout";
        textContent=(TextView) findViewById(R.id.hint_info);
        loggoutTextView=(TextView)logoutButton;
        loggoutTextView.setText(logoutText);
        loginProgress=(ProgressBar)findViewById(R.id.log_in_progress);

    }



    private void showAuthToast(String authToastMessage){
        Toast authToast = Toast.makeText(getApplicationContext(),authToastMessage,Toast.LENGTH_LONG);
        authToast.setGravity(Gravity.CENTER,0,0);
        authToast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRequestContext.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
        AuthorizationManager.getToken(this, new Scope[] { ScopeFactory.scopeNamed("alexa:all") }, new TokenListener());
    }

    /**
     * Sets the text in the textContent {@link TextView} to the prompt it originally displayed.
     */
    private void resetTextCotent(){
        setLoggingInState(false);
        textContent.setText(getString(R.string.loginAmazonFirst));
    }

    /**
     * Sets the state of the application to reflect that the user is currently authorized.
     */
    private void setLoggedInState(){
        mLoginButton.setVisibility(Button.GONE);
        loggoutTextView.setVisibility(Button.VISIBLE);
        isLoggedIn=true;
        setLoggingInState(false);
    }

    /**
     * Sets the state of the application to reflect that the user is not currently authorized.
     */
    private void setLoggedOutState(){
        mLoginButton.setVisibility(Button.VISIBLE);
        loggoutTextView.setVisibility(Button.GONE);
        isLoggedIn=false;
        resetTextCotent();

    }

    /**
     * Turns on/off display elements which indicate that the user is currently in the process of logging in
     *
     * @param logginIn whether or not the user is currently in the process of logging in
     */
    private void setLoggingInState(final boolean logginIn){
        if(logginIn){
            mLoginButton.setVisibility(Button.GONE);
            loggoutTextView.setVisibility(Button.GONE);
//            setLoggedInButtonVisibility(Button.GONE);
            loginProgress.setVisibility(ProgressBar.VISIBLE);
            textContent.setVisibility(TextView.GONE);
        }else{
            if(isLoggedIn){
                loggoutTextView.setVisibility(Button.VISIBLE);
            }else{
                mLoginButton.setVisibility(Button.VISIBLE);
            }
            textContent.setVisibility(TextView.VISIBLE);
            loginProgress.setVisibility(ProgressBar.GONE);
        }
    }


    public class AuthorizeListenerImpl extends AuthorizeListener{
        @Override
        public void onSuccess(AuthorizeResult authorizeResult) {
            accessToken=authorizeResult.getAccessToken();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setLoggingInState(true);

                }
            });
        }

        /* There was an error during the attempt to authorize the application. */
        @Override
        public void onError(final AuthError authError) {
            Log.e(Tag,"Error when authorizing", authError);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAuthToast("Error when authorizing.Please try again.");
                    resetTextCotent();
                    setLoggingInState(false);
                }
            });
        }

        /* Authorization was cancelled before it could be completed. */
        @Override
        public void onCancel(final AuthCancellation authCancellation) {
            Log.e(Tag,"User cancelled authorization");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAuthToast("Authorization cancelled");
                    resetTextCotent();
                }
            });
        }
    }

    public class TokenListener implements Listener<AuthorizeResult, AuthError> {
        /* getToken completed successfully. */
        @Override
        public void onSuccess(AuthorizeResult authorizeResult) {
             accessToken = authorizeResult.getAccessToken();

        }

        /* There was an error during the attempt to get the token. */
        @Override
        public void onError(AuthError authError) {
        }
    }

}
