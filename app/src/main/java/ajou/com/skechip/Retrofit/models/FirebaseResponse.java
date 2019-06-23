package ajou.com.skechip.Retrofit.models;

import java.util.List;

//{
//        "multicast_id": 5266717693514615194,
//        "success": 1,
//        "failure": 0,
//        "canonical_ids": 0,
//        "results": [
//        {
//              "message_id": "0:1559981197493382%cbbaafcdcbbaafcd"
//        }]
//}
public class FirebaseResponse {
    private int multicast_id;
    private int success;
    private int failure;
    private int canonical_ids;
    private List<String> results;

    public FirebaseResponse(int multicast_id, int success, int failure, int canonical_ids, List<String> results) {
        this.multicast_id = multicast_id;
        this.success = success;
        this.failure = failure;
        this.canonical_ids = canonical_ids;
        this.results = results;
    }
    public int getSuccess() {
        return success;
    }
}
