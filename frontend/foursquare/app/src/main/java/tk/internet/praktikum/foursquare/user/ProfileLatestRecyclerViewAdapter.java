package tk.internet.praktikum.foursquare.user;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Gender;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueCheckinInformation;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.search.Utils;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class ProfileLatestRecyclerViewAdapter extends RecyclerView.Adapter<ProfileLatestRecyclerViewAdapter.ProfileLatestViewHolder> {

    class ProfileLatestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, date, shortName;
        public ImageView logo;

        public ProfileLatestViewHolder(View itemView) {
            super(itemView);
            logo = (ImageView) itemView.findViewById(R.id.profile_last_venue_logo);
            date = (TextView) itemView.findViewById(R.id.profile_last_venue_date);
            name = (TextView) itemView.findViewById(R.id.profile_last_venue_name);
            shortName = (TextView) itemView.findViewById(R.id.profile_last_venue_short_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
                loadVenue();
        }

        private void loadVenue() {
           // TODO - LOAD VENUE DETAILS VIEW
        }
    }

    private final String URL = "https://dev.ip.stimi.ovh/";
    private Context context;
    private LayoutInflater inflater;
    private List<VenueCheckinInformation> venueCheckinList = Collections.emptyList();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("d.M.y HH:mm",  Locale.ENGLISH);


    public ProfileLatestRecyclerViewAdapter(Context context, List<VenueCheckinInformation> venueCheckinList) {
        inflater = LayoutInflater.from(context);
        this.venueCheckinList = venueCheckinList;
        this.context = context;
    }

    @Override
    public ProfileLatestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.profile_last_entry, parent, false);

        return new ProfileLatestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProfileLatestViewHolder holder, int position) {
        VenueCheckinInformation currentCheckin = venueCheckinList.get(position);

        VenueService service = ServiceFactory
                .createRetrofitService(VenueService.class, URL, LocalStorage.
                        getSharedPreferences(context).getString(Constants.TOKEN, ""));

        try {
            service.getDetails(currentCheckin.getVenueID())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            venue -> {
                                holder.name.setText(venue.getName());
                                loadImage(venue, holder);
                            },
                            throwable -> {
                                Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }

        holder.date.setText(SIMPLE_DATE_FORMAT.format(currentCheckin.getLastDate()));
    }

    public void loadImage(Venue venue, ProfileLatestViewHolder holder){
        List<Image> images=venue.getImages();
        if(images.size()>0) {
            Image image = images.get(0);
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
            imageCacheLoader.loadBitmap(image, ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        holder.logo.setImageBitmap(bitmap);
                        holder.logo.setVisibility(View.VISIBLE);
                        holder.shortName.setVisibility(View.GONE);
                    });
        }
        else {
            holder.logo.setDrawingCacheEnabled(true);
            Bitmap bitmap= null;
            try {
                bitmap = Utils.decodeResourceImage(context,"default_image",50,50);
                holder.logo.setImageBitmap(bitmap);
                holder.shortName.setText(venue.getName().substring(0,1).toUpperCase());
                holder.shortName.setVisibility(View.VISIBLE);
                holder.logo.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return venueCheckinList.size();
    }
}
