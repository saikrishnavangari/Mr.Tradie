package au.com.appscore.mrtradie;

import java.io.Serializable;

/**
 * Created by adityathakar on 20/08/15.
 */
public class Business implements Serializable{

    private String name;
    private String fullAddress, street, city, state, postCode, country, latitude, longitude, companyLogo,rating, aboutCompany, jsonArrayReviews, email, website, phoneNumber, photo1, photo2, photo3;
    public float distance = 0, score =0;

    public Business(String name, String fullAddress, String street, String city, String state, String postCode, String country, String latitude, String longitude, String companyLogo, String rating, String aboutCompany, String jsonArrayReviews, String email, String website, String phoneNumber, String photo1, String photo2, String photo3) {
        this.name = name;
        this.fullAddress = fullAddress;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postCode = postCode;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.companyLogo = companyLogo;
        this.rating = rating;
        this.aboutCompany = aboutCompany;
        this.jsonArrayReviews = jsonArrayReviews;
        this.email = email;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.photo1 = photo1;
        this.photo2 = photo2;
        this.photo3 = photo3;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto1() {
        return photo1;
    }

    public void setPhoto1(String photo1) {
        this.photo1 = photo1;
    }

    public String getPhoto2() {
        return photo2;
    }

    public void setPhoto2(String photo2) {
        this.photo2 = photo2;
    }

    public String getPhoto3() {
        return photo3;
    }

    public void setPhoto3(String photo3) {
        this.photo3 = photo3;
    }


    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJsonArrayReviews() {
        return jsonArrayReviews;
    }

    public void setJsonArrayReviews(String jsonArrayReviews) {
        this.jsonArrayReviews = jsonArrayReviews;
    }

    public String getAboutCompany() {
        return aboutCompany;
    }

    public void setAboutCompany(String aboutCompany) {
        this.aboutCompany = aboutCompany;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getName() {
        return this.name;
    }

    public String getFullAddress() {
        return this.fullAddress;
    }


}
