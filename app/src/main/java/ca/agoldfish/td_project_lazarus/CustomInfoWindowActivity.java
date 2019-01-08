package ca.agoldfish.td_project_lazarus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

public class CustomInfoWindowActivity implements GoogleMap.InfoWindowAdapter {

    private View mWindow;
    private Context mContext;
    private PropertyPackage pp;


    public CustomInfoWindowActivity(Context context) {
        this.mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.activity_custom_info_window,null);
    }

    private void renderWindowText(Marker marker, View view){
        TextView address = view.findViewById(R.id.addressTxt);
        ImageView propIV = view.findViewById(R.id.propertyImg);

        TextView locationTV = view.findViewById(R.id.locationTxt);
        TextView priceTV = view.findViewById(R.id.priceTxt);
        TextView realtorTV = view.findViewById(R.id.realtorTxt);
        TextView bedTV = view.findViewById(R.id.bedTxt);
        TextView bathTV = view.findViewById(R.id.bathTxt);
        TextView sizeTV = view.findViewById(R.id.sizeTxt);

        Gson gson = new Gson();
        pp = gson.fromJson(marker.getSnippet(),PropertyPackage.class);


        address.setText(pp.getAddress());
        locationTV.setText(pp.getLocation());
        priceTV.setText(pp.getPrice());
        realtorTV.setText(pp.getRealtor());
        bedTV.setText(pp.getBedrooms());
        bathTV.setText(pp.getBathrooms());
        sizeTV.setText(pp.getSize());

//        propIV.setImageResource(R.drawable.arlogo);
//        ask teacher
//        Glide.with(mContext.getApplicationContext())
//                            .asBitmap()
//                            .load(pp.getPicture())
//                            .into(propIV);


//        Glide.with(mContext)
//                .load(pp.getPicture())
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        return false; // important to return false so the error placeholder can be placed
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        new Handler().postDelayed(() -> {
//                            if (marker.isInfoWindowShown()) {
//                                marker.showInfoWindow();
//                            }
//                        }, 100);
//                        return false;
//                    }
//                })
//                .into(propIV);
        //set values in here
    }
    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker,mWindow);
        return mWindow;
    }
}
