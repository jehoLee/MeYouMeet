package ajou.com.skechip.Retrofit.conn;

import java.util.ArrayList;
import java.util.List;

import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import ajou.com.skechip.Retrofit.models.TimeTableResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallMethod {
    public void append_server(List<Cell> cell, Long kakaoUserID, char type) {
        for (Cell c : cell) {
            Call<DefaultResponse> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .createTimeTable(kakaoUserID, type, c.getSubjectName(), c.getPlaceName(), c.getPosition());
            call.enqueue(new Callback<DefaultResponse>() {
                @Override
                public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {

                }

                @Override
                public void onFailure(Call<DefaultResponse> call, Throwable t) {

                }
            });
        }

    }

    public void append_server(List<Cell> cells, ArrayList<Long> kakaoUserIDs, char type) {
        for (Long userID : kakaoUserIDs) {
            this.append_server(cells, userID, type);
        }
    }

    public void update_server(Cell cell, Long kakaoUserID) {
        Call<TimeTableResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .updateTimeTable(kakaoUserID, 'c', cell.getSubjectName(), cell.getPlaceName(), cell.getPosition());

        call.enqueue(new Callback<TimeTableResponse>() {
            @Override
            public void onResponse(Call<TimeTableResponse> call, Response<TimeTableResponse> response) {

            }

            @Override
            public void onFailure(Call<TimeTableResponse> call, Throwable t) {

            }
        });
    }

    public void delete_server(List<Cell> cell, Long kakaoUserID) {
        for (Cell c : cell) {
            Call<TimeTableResponse> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .deleteTimeTable(kakaoUserID, c.getPosition());

            call.enqueue(new Callback<TimeTableResponse>() {
                @Override
                public void onResponse(Call<TimeTableResponse> call, Response<TimeTableResponse> response) {

                }

                @Override
                public void onFailure(Call<TimeTableResponse> call, Throwable t) {

                }
            });
        }
    }
}