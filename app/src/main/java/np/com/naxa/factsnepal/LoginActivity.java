package np.com.naxa.factsnepal;

import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.HashMap;

import np.com.naxa.factsnepal.common.BaseLoginActivity;
import np.com.naxa.factsnepal.feed.list.FeedListActivity;
import np.com.naxa.factsnepal.utils.ActivityUtil;
import np.com.naxa.factsnepal.utils.Utils;

public class LoginActivity extends BaseLoginActivity {

    @Override
    public void onFacebookLogiSuccess(LoginResult result) {
        Profile profile = Profile.getCurrentProfile();

        HashMap<String, AccessToken> map = new HashMap<>();
        map.put("token", result.getAccessToken());
        ActivityUtil.openActivity(UpdateProfileActivity.class, this, map, false);

    }

//    @Override
//    public void onTwitterLoginSuccess(Result<TwitterSession> twitterSessionResult) {
//        Utils.log(this.getClass(), twitterSessionResult.data.getUserName());
//    }

    @Override
    public void onGoogleLoginSuccess(GoogleSignInAccount account) {
        Utils.log(this.getClass(), account.getDisplayName());
        startActivity(new Intent(this, FeedListActivity.class));
    }


}