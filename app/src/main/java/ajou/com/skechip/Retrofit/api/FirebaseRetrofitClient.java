package ajou.com.skechip.Retrofit.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FirebaseRetrofitClient {
    private static final String FIREBASE_URL = "http://fcm.googleapis.com/fcm/";
    private Retrofit Firebase_retrofit;
    private FirebaseRetrofitClient(){
        Firebase_retrofit = new Retrofit.Builder()
                .baseUrl(FIREBASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public FirebaseApi getFirebaseApi(){ return Firebase_retrofit.create(FirebaseApi.class); }

}
