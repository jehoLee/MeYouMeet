package ajou.com.skechip.Retrofit.api;

import ajou.com.skechip.Retrofit.json.TranslationResultDTO;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FirebaseApi {

    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAARAmpOcw:APA91bGZaSMcqIKDHMWNbOQhpyEpJ-8HRBi3hCxuPKuiQ40HWf0pqsRYXipcZCGpGJeVEdeChD-NWiEWEH66p-khBXimJrrC-iilA4IlJ6eL1dBE0xLmOE3ZTH53iOlB7UHxFSOccNaD"
    })
    @POST("send")
    Call<DefaultResponse>sendAlert(@Body TranslationResultDTO body);
}