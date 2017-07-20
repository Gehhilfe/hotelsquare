package tk.internet.praktikum.foursquare.search;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.TextComment;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.CommentService;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

import static android.app.Activity.RESULT_OK;

//import android.support.design.widget.FloatingActionButton;


public class VenueInDetailFragment extends Fragment implements OnMapReadyCallback {

    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = VenueInDetailFragment.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private ViewGroup container;
    private String venueId;
    private View view;

    private ImageView imageVenueOne;
    private ImageView imageVenueTwo;
    private ImageView imageVenueThree;

    private TextView venueName;
    private TextView venueAddress;
    private TextView venueIsOpened;
    private TextView venueWebsite;
    private TextView venueWebsiteLabel;
    private List<tk.internet.praktikum.foursquare.api.bean.Image> images;
    private GoogleMap map;
    private ProgressDialog progressDialog;
    private FloatingActionButton venueTextCommentButton;
    private FloatingActionButton venueImageCommentButton;
    private RecyclerView recyclerView;
    private AlertDialog venueTextCommentDialog;
    private AlertDialog venueImageCommentDialog;

    private Bitmap venueImageComment;
    private int currentPage;
    private ImageView selectedImageView;
    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;

    public static VenueInDetailFragment newInstance(String param1, String param2) {
        VenueInDetailFragment fragment = new VenueInDetailFragment();

        return fragment;
    }

    public VenueInDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venue_in_detail, container, false);
        // layoutInflater=inflater;
        //this.container=container;
        imageVenueOne = (ImageView) view.findViewById(R.id.image_venue_one);
        imageVenueTwo = (ImageView) view.findViewById(R.id.image_venue_two);
        imageVenueThree = (ImageView) view.findViewById(R.id.image_venue_three);

        venueName = (TextView) view.findViewById(R.id.venue_name);
        venueAddress = (TextView) view.findViewById(R.id.venue_address);
        venueIsOpened = (TextView) view.findViewById(R.id.venue_is_opened);
        venueWebsite = (TextView) view.findViewById(R.id.venue_website);
        venueWebsiteLabel = (TextView) view.findViewById(R.id.venue_website_label);

        venueTextCommentButton = (FloatingActionButton) view.findViewById(R.id.venue_detail_text_comment_button);
        venueTextCommentButton.setOnClickListener(v -> {
            showUpTextCommentDialog();
        });
        venueImageCommentButton = (FloatingActionButton) view.findViewById(R.id.venue_detail_image_commnent_button);
        venueImageCommentButton.setOnClickListener(v -> {
            showUpImageCommentDialog();
        });
        recyclerView = (RecyclerView) view.findViewById(R.id.comments_venue);

        progressDialog = new ProgressDialog(getActivity(), 1);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for seeing venue details...");
        progressDialog.show();

        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venueDetails_mapView));
        mapFragment.getMapAsync(this);
        currentPage = 0;
        renderContent();

        return view;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }


    private void renderContent() {
        Log.d(LOG, "##### Venue Id: " + venueId);
        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getDetails(venueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venue -> {
                            renderVenueInformation(venue);
                            Location location = venue.getLocation();
                            updateVenueLocation(location);
                            images = venue.getImages();
                            Log.d(LOG, "all images size: " + images.size());
                            if (images.size() > 0) {
                                Log.d(LOG, "++++ get images");
                                Image image = images.get(0);
                                ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());
                                imageCacheLoader.loadBitmap(image, ImageSize.LARGE)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(bitmap -> {
                                            imageVenueOne.setImageBitmap(bitmap);
                                            imageVenueTwo.setImageBitmap(bitmap);
                                            imageVenueThree.setImageBitmap(bitmap);
                                        });
                            }
                            renderCommentVenue(venue);
                            progressDialog.dismiss();
                        },
                        throwable -> {
                            //TODO
                            //handle exception
                            progressDialog.dismiss();
                            Log.d(LOG, "#### exception" + throwable.getCause());

                        }
                );
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(false);
        //map.getUiSettings().setMapToolbarEnabled(true);
    }

    public void updateVenueLocation(Location location) {
        LatLng venueLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("venue_location_marker", "mipmap", getContext().getPackageName()));

        map.addMarker(new MarkerOptions()
                .position(venueLocation))
                .setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmap));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 14));


    }

    public void renderVenueInformation(Venue venue) {
        this.venueName.setText(venue.getName());
        this.venueAddress.setText(venue.getVicinity());
        this.venueWebsiteLabel.setText(getString(R.string.websiteLabel));
        this.venueWebsite.setText(venue.getWebsite());

        if (venue.is_open())
            this.venueIsOpened.setText(getString(R.string.isOpened));
        else
            this.venueIsOpened.setText(getString(R.string.closed));
    }

    public void renderCommentVenue(Venue venue) {
        String venueId = venue.getId();
        CommentService commentService = ServiceFactory.createRetrofitService(CommentService.class, URL);
        commentService.getComments(venueId, currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                            updateRecyclerView(comments);
                        },
                        throwable -> {
                            Log.d(LOG, throwable.getMessage());
                        });

    }

    private void showUpTextCommentDialog() {
        //Todo
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View textCommentView = inflater.inflate(R.layout.venue_comment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //Todo
                // call venueServices
                System.out.println("*** comment text");
                EditText textCommentContent = (EditText) textCommentView.findViewById(R.id.venue_text_comment_content);
                String commnent = textCommentContent.getText().toString().trim();
                if (!commnent.isEmpty()) {
                    VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
                    TextComment textComment = new TextComment(commnent);
                    SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getContext());
                    User user = new User(sharedPreferences.getString(Constants.NAME, ""), sharedPreferences.getString(Constants.EMAIL, ""));
                    textComment.setAuthor(user);
                    venueService.addTextComment(textComment, venueId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(textComment1 -> {
                                        Log.d(LOG, "##### textcomment: " + textComment1.getId());

                                    },
                                    throwable -> {
                                        Log.d(LOG, throwable.getMessage());
                                    });


                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                venueTextCommentDialog.dismiss();
            }
        });

        venueTextCommentDialog = builder.create();
        venueTextCommentDialog.setView(textCommentView);
        venueTextCommentDialog.show();


    }

    private void showUpImageCommentDialog() {
        //Todo
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View venueImageCommentView = inflater.inflate(R.layout.venue_image_comment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //Todo
                // call venueServices

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                venueImageCommentDialog.dismiss();
            }
        });

        venueImageCommentDialog = builder.create();

        venueImageCommentDialog.setView(venueImageCommentView);
        Button venuImageCommentButton = (Button) venueImageCommentView.findViewById(R.id.venue_image_comment_button);
        selectedImageView = (ImageView) venueImageCommentView.findViewById(R.id.venue_image_comment);
        venuImageCommentButton.setOnClickListener(v -> {
            uploadPicture();
        });
        venueImageCommentDialog.show();
    }


    private void uploadPicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] options = {"Camera", "Gallery", "Cancel"};
        builder.setTitle("Select an option to choose your avatar.");
        builder.setItems(options, (dialog, option) -> {
            switch (options[option]) {
                case "Camera":
                    Log.d(LOG, "Camera");
                    cameraIntent();
                    break;
                case "Gallery":
                    Log.d(LOG, "gallery");
                    galleryIntent();
                    break;
                case "Cancel":
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    venueImageComment = (Bitmap) data.getExtras().get("data");
                    selectedImageView.setImageBitmap(venueImageComment);
                }
                break;

            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        venueImageComment = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        selectedImageView.setImageBitmap(venueImageComment);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void updateRecyclerView(List<Comment> comments) {
        Log.d(LOG, "***size :" + comments.size());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new CommentVenueAdapter(comments, this));
    }


}
