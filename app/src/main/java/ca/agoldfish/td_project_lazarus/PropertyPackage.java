package ca.agoldfish.td_project_lazarus;

import android.os.Parcel;
import android.os.Parcelable;

public class PropertyPackage implements Parcelable {

    // Member variables
    private String address;
    private String arcore = "false";//default if not set
    private String bathrooms;
    private String bedrooms;
    private String city;
    private String description;
    private String phone;
    private String picture;
    private String price;
    private String realtor;
    private String realtorCompany;
    private double latitude;
    private double longitude;
    private String size;
    private String state;
    private String zipcode;
    private String type;
    //gallery maybe


    public PropertyPackage() {

    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArcore() {
        return arcore;
    }

    public void setArcore(String arcore) {
        this.arcore = arcore;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(String bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(String bedrooms) {
        this.bedrooms = bedrooms;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRealtor() {
        return realtor;
    }

    public void setRealtor(String realtor) {
        this.realtor = realtor;
    }

    public String getRealtorCompany() {
        return realtorCompany;
    }

    public void setRealtorCompany(String realtorCompany) {
        this.realtorCompany = realtorCompany;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return city + ", " +state + " " + zipcode;
    }

    // In constructor you will read the variables from Parcel. Make sure to read them in the same sequence in which you have written them in Parcel.
    public PropertyPackage(Parcel in) {
        address = in.readString();
        arcore = in.readString();
        bathrooms = in.readString();
        bedrooms = in.readString();
        city = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        phone = in.readString();
        picture = in.readString();
        price = in.readString();
        realtor = in.readString();
        realtorCompany = in.readString();
        size = in.readString();
        state = in.readString();
        zipcode = in.readString();
        type = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(arcore);
        dest.writeString(bathrooms);
        dest.writeString(bedrooms);
        dest.writeString(city);
        dest.writeString(description);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(phone);
        dest.writeString(picture);
        dest.writeString(price);
        dest.writeString(realtor);
        dest.writeString(realtorCompany);
        dest.writeString(size);
        dest.writeString(state);
        dest.writeString(zipcode);
        dest.writeString(type);
    }

    // This is to de-serialize the object
    public static final Parcelable.Creator<PropertyPackage> CREATOR = new Parcelable.Creator<PropertyPackage>() {
        public PropertyPackage createFromParcel(Parcel in) {
            return new PropertyPackage(in);
        }

        @Override
        public PropertyPackage[] newArray(int size) {
            return new PropertyPackage[size];
        }
    };
}
