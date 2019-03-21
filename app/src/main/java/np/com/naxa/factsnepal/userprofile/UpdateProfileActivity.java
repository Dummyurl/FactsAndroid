package np.com.naxa.factsnepal.userprofile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.gson.Gson;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.json.JSONException;

import java.util.HashMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import np.com.naxa.factsnepal.R;
import np.com.naxa.factsnepal.common.BaseActivity;
import np.com.naxa.factsnepal.feed.list.FeedListActivity;
import np.com.naxa.factsnepal.network.NetworkApiClient;
import np.com.naxa.factsnepal.network.NetworkApiInterface;
import np.com.naxa.factsnepal.utils.ActivityUtil;
import np.com.naxa.factsnepal.utils.ImageUtils;

public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UpdateProfileActivity";

    private EditText etWard;
    private AutoCompleteTextView etDistrict;
    private Button btnNext, btnGetGPS;
    private EditText etDob;
    private Spinner spinnerGender;


    private DisposableSubscriber<Boolean> disposableObserver = null;
    private Flowable<CharSequence> genderChangeObservable;
    private Flowable<CharSequence> gpsChangeObservable;
    private AutoCompleteTextView etProvience;
    private EditText etMunicipality;
    private EditText etStreet;
    private MaterialButton btnSkip;
    private ImageView ivProfilePhoto;

    private String facebookToken = "", fbProfileImage = "", provider = "", gender = "male";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);
        initView();
        setupToolbar();
        setupDistrictAutoComplete();
        setupProvinceAutoComplete();


        Intent intent = getIntent();


        try {
            HashMap<String, AccessToken> hashMap = (HashMap<String, AccessToken>) intent.getSerializableExtra("map");
            AccessToken token = hashMap.get("token");
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender, birthday");


            fbProfileImage = String.format("https://graph.facebook.com/%s/picture?type=large",token.getUserId());
            facebookToken = token.getToken();
            provider = "facebook";

            Log.d(TAG, "onCreate: userID "+token.getUserId());
            Log.d(TAG, "onCreate: userToken "+token.getToken());
            ImageUtils.loadRemoteImage(this,fbProfileImage).circleCrop().into(ivProfilePhoto);


            GraphRequest request = GraphRequest.newMeRequest(token, (object, response) -> {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            request.setParameters(parameters);
            request.executeAsync();

        } catch (NullPointerException e) {
            //unhandled
            //WIP
        }

        makeUIObservable();
    }

    private void initView() {
        etDob = findViewById(R.id.et_dob);
        spinnerGender = findViewById(R.id.spinner_gender);
        etWard = findViewById(R.id.et_ward);
        etDistrict = findViewById(R.id.et_district);
        etProvience = findViewById(R.id.et_province);
        etMunicipality = findViewById(R.id.et_municipality);
        etStreet = findViewById(R.id.et_street);
        btnSkip = findViewById(R.id.btn_skip);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        btnSkip.setOnClickListener(this);
        btnNext = (MaterialButton) findViewById(R.id.btn_next);
        btnGetGPS = (MaterialButton) findViewById(R.id.btn_get_gps);
        btnNext.setOnClickListener(this);
        btnGetGPS.setOnClickListener(this);

    }

    private void setupDistrictAutoComplete() {

        String[] arr = getResources().getStringArray(R.array.districts);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, arr);

        etDistrict.setThreshold(1);
        etDistrict.setAdapter(adapter);
    }

    private void setupProvinceAutoComplete() {

        String[] arr = getResources().getStringArray(R.array.provinces);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, arr);

        etProvience.setThreshold(1);
        etProvience.setAdapter(adapter);
    }

    private void makeUIObservable() {
        Flowable<CharSequence> dobChangeObservable = RxTextView.textChanges(etDob).skip(1).toFlowable(BackpressureStrategy.LATEST);
        Flowable<CharSequence> wardChangeObservable = RxTextView.textChanges(etWard).skip(1).toFlowable(BackpressureStrategy.LATEST);
        Flowable<CharSequence> districtChangeObservable = RxTextView.textChanges(etDistrict).skip(1).toFlowable(BackpressureStrategy.LATEST);
        Flowable<CharSequence> provinceChangeObservable = RxTextView.textChanges(etProvience).skip(1).toFlowable(BackpressureStrategy.LATEST);
        Flowable<CharSequence> municipalityChangeObservable = RxTextView.textChanges(etMunicipality).skip(1).toFlowable(BackpressureStrategy.LATEST);
        Flowable<CharSequence> streetChangeObservable = RxTextView.textChanges(etStreet).skip(1).toFlowable(BackpressureStrategy.LATEST);

        disposableObserver = Flowable.combineLatest(dobChangeObservable, wardChangeObservable, districtChangeObservable, provinceChangeObservable, municipalityChangeObservable, streetChangeObservable,
                this::isValidForm)
                .subscribeWith(new DisposableSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean isValid) {
                        updateButton(isValid);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private boolean isValidForm(CharSequence dob, CharSequence ward, CharSequence district, CharSequence province, CharSequence municipality, CharSequence street) {

        boolean validDob, validWard, validDistrict, validProvince, validMunicipality, validStreet;
        validDob = !dob.toString().isEmpty();
        validWard = !ward.toString().isEmpty();
        validDistrict = !district.toString().isEmpty();
        validProvince = !province.toString().isEmpty();
        validMunicipality = !municipality.toString().isEmpty();
        validStreet = !street.toString().isEmpty();

        if (!validDob) {
            showError(etDob, getString(R.string.msg_invalid_input, "year"));
        }


        if (!validWard) {
            showError(etWard, getString(R.string.msg_invalid_input, "ward"));
        }


        if (!validDistrict) {
            showError(etDistrict, getString(R.string.msg_invalid_input, "district"));
        }


        if (!validProvince) {
            showError(etProvience, getString(R.string.msg_invalid_input, "province"));
        }


        if (!validMunicipality) {
            showError(etMunicipality, getString(R.string.msg_invalid_input, "municipality"));
        }

        if (!validStreet) {
            showError(etStreet, getString(R.string.msg_invalid_input, "street"));
        }

        return validDob && validWard && validDistrict && validProvince && validMunicipality && validStreet;

    }


    private void showError(View view, String errorMessage) {
        if (view instanceof EditText) {
            ((EditText) view).setError(errorMessage);
        } else if (view instanceof AutoCompleteTextView) {
            ((AutoCompleteTextView) view).setError(errorMessage);

        }
    }


    private void showError(AutoCompleteTextView editText, String error_message) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableObserver.dispose();
    }

    public void updateButton(boolean valid) {
        if (valid) {
            btnNext.setEnabled(true);
        } else {
            btnNext.setEnabled(false);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                userRegistration();
                break;
            case R.id.btn_skip:
                ActivityUtil.openActivity(FeedListActivity.class, this, null, false);
                break;
            default:
                break;
        }
    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.first:
                if (checked)
                    gender = "male";
                break;

            case R.id.second:
                if (checked)
                    gender = "female";
                break;
        }
    }

    private void userRegistration(){
        UserDetails userDetails = new UserDetails(fbProfileImage, etWard.getText().toString(), etDistrict.getText().toString(), etProvience.getText().toString(),
                etMunicipality.getText().toString(), etStreet.getText().toString(), etDob.getText().toString(), gender, provider, facebookToken);

        Gson gson = new Gson();
        String jsonInString = gson.toJson(userDetails);
        Log.d(TAG, "userRegistration: "+jsonInString);

        apiInterface.getUserDetailsResponse(userDetails)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<UserDetailsResponse>() {
                    @Override
                    public void onNext(UserDetailsResponse userDetailsResponse) {
                        if(userDetailsResponse.getSuccess()){

                        }else {

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}