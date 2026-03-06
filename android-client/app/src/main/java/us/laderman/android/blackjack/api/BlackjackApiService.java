package us.laderman.android.blackjack.api;

import us.laderman.android.blackjack.model.GameState;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BlackjackApiService {

    @POST("api/blackjack/start")
    Call<GameState> startGame(@Query("hands") int hands,
            @Query("decks") int decks,
            @Query("doubleOn9") boolean doubleOn9,
            @Query("hitSoft17") boolean hitSoft17);

    @GET("api/blackjack/state/{id}")
    Call<GameState> getGameState(@Path("id") String gameId);

    @POST("api/blackjack/hit/{id}")
    Call<GameState> hit(@Path("id") String gameId, @Query("hand") int handIndex);

    @POST("api/blackjack/stand/{id}")
    Call<GameState> stand(@Path("id") String gameId, @Query("hand") int handIndex);

    @POST("api/blackjack/double/{id}")
    Call<GameState> doubleDown(@Path("id") String gameId, @Query("hand") int handIndex);

    @POST("api/blackjack/split/{id}")
    Call<GameState> split(@Path("id") String gameId, @Query("hand") int handIndex);

    @POST("api/blackjack/insurance/{id}")
    Call<GameState> insurance(@Path("id") String gameId, @Query("hand") int handIndex);

    @POST("api/blackjack/decline-insurance/{id}")
    Call<GameState> declineInsurance(@Path("id") String gameId, @Query("hand") int handIndex);
}
