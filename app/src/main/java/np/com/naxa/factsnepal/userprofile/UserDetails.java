package np.com.naxa.factsnepal.userprofile;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetails implements Parcelable {

    @SerializedName("photo_url")
    @Expose
    private String photo_url;
    @SerializedName("ward")
    @Expose
    private String ward;
    @SerializedName("district")
    @Expose
    private String district;
    @SerializedName("province")
    @Expose
    private String province;
    @SerializedName("municipality")
    @Expose
    private String municipality;
    @SerializedName("street")
    @Expose
    private String street;
    @SerializedName("birth_year")
    @Expose
    private String birth_year;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("provider")
    @Expose
    private String provoder;
    @SerializedName("token")
    @Expose
    private String token;

    public UserDetails(String photo_url, String ward, String district, String province, String municipality, String street, String birth_year, String gender, String provoder, String token) {
        this.photo_url = photo_url;
        this.ward = ward;
        this.district = district;
        this.province = province;
        this.municipality = municipality;
        this.street = street;
        this.birth_year = birth_year;
        this.gender = gender;
        this.provoder = provoder;
        this.token = token;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBirth_year() {
        return birth_year;
    }

    public void setBirth_year(String birth_year) {
        this.birth_year = birth_year;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProvoder() {
        return provoder;
    }

    public void setProvoder(String provoder) {
        this.provoder = provoder;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.photo_url);
        dest.writeString(this.ward);
        dest.writeString(this.district);
        dest.writeString(this.province);
        dest.writeString(this.municipality);
        dest.writeString(this.street);
        dest.writeString(this.birth_year);
        dest.writeString(this.gender);
        dest.writeString(this.provoder);
        dest.writeString(this.token);
    }

    protected UserDetails(Parcel in) {
        this.photo_url = in.readString();
        this.ward = in.readString();
        this.district = in.readString();
        this.province = in.readString();
        this.municipality = in.readString();
        this.street = in.readString();
        this.birth_year = in.readString();
        this.gender = in.readString();
        this.provoder = in.readString();
        this.token = in.readString();
    }

    public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel source) {
            return new UserDetails(source);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };
}