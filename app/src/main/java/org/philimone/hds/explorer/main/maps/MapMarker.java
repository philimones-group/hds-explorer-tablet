package org.philimone.hds.explorer.main.maps;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Point;

import java.io.Serializable;

public class MapMarker implements Serializable, Parcelable {
    private double gpsLatitude;
    private double gpsLongitude;
    private String tittle;
    private String description;

    public MapMarker(double gpsLatitude, double getLongitude, String tittle, String description) {
        this.gpsLatitude = gpsLatitude;
        this.gpsLongitude = getLongitude;
        this.tittle = tittle;
        this.description = description;
    }

    public MapMarker(String tittle, String description) {
        this.tittle = tittle;
        this.description = description;
    }

    public void setCoords(double latitude, double longitude){
        this.gpsLatitude = latitude;
        this.gpsLongitude = longitude;
    }

    public Point getPoint() {
        return Point.fromLngLat(this.gpsLongitude, this.gpsLatitude);
    }

    public double getGpsLatitude() {
        return gpsLatitude;
    }

    public double getGpsLongitude() {
        return gpsLongitude;
    }

    public String getTittle() {
        return tittle;
    }

    public String getDescription() {
        return description;
    }

    public void setGpsLatitude(double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public void setGpsLongitude(double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    protected MapMarker(Parcel in) {
        gpsLatitude = in.readDouble();
        gpsLongitude = in.readDouble();
        tittle = in.readString();
        description = in.readString();
    }

    public static final Creator<MapMarker> CREATOR = new Creator<MapMarker>() {
        @Override
        public MapMarker createFromParcel(Parcel in) {
            return new MapMarker(in);
        }

        @Override
        public MapMarker[] newArray(int size) {
            return new MapMarker[size];
        }
    };

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeDouble(this.gpsLatitude);
        dest.writeDouble(this.gpsLongitude);
        dest.writeString(this.tittle);
        dest.writeString(this.description);

    }


}
