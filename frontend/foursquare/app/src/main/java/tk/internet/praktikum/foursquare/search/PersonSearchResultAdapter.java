package tk.internet.praktikum.foursquare.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.CommentAdapter;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.user.ProfileActivity;

/**
 * Created by gehhi on 17.08.2017.
 */

class PersonSearchResultAdapter extends RecyclerView.Adapter<PersonSearchResultAdapter.MyViewHolder> {
    private static final String LOG = PersonSearchResultAdapter.class.getName();
    private ArrayList<User> results;
    private Context context;

    public PersonSearchResultAdapter(Context context) {
        this.results = new ArrayList<User>();
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_search_result_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = results.get(position);
        StringBuilder sb = new StringBuilder();
        sb.append(user.getDisplayName());
        if (user.getAge() != 0) {
            sb.append(", ");
            sb.append(user.getAge());
        }
        if (user.getCity() != null && !user.getCity().isEmpty()) {
            sb.append(", ");
            sb.append(user.getCity());
        }
        holder.name.setText(sb.toString());

        holder.itemView.setOnClickListener(l -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("Parent", "SearchPerson");
            intent.putExtra("userID", user.getId());
            context.startActivity(intent);
        });

        if (user.getAvatar() != null) {
            try {
                ImageCacheLoader loader = new ImageCacheLoader(context);
                loader.loadBitmap(user.getAvatar(), ImageSize.SMALL)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> holder.avatar.setImageBitmap(bitmap), err -> Log.d(LOG, err.toString(), err));
            } catch (Exception err) {
                Log.d(LOG, err.toString(), err);
            }
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public void setResults(List<User> results) {
        this.results = new ArrayList<>(results);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView avatar;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }
}
