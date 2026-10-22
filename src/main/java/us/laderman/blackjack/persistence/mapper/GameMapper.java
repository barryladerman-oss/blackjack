package us.laderman.blackjack.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import us.laderman.blackjack.persistence.model.GameRecord;

@Mapper
public interface GameMapper {
    @Select("SELECT pk, time, num_decks AS numDecks FROM game WHERE pk = #{pk}")
    GameRecord findById(@Param("pk") Integer pk);

    @Select("SELECT pk, time, num_decks AS numDecks FROM game ORDER BY pk")
    List<GameRecord> findAll();

    @Insert("INSERT INTO game (time, num_decks) VALUES (#{time}, #{numDecks})")
    @Options(useGeneratedKeys = true, keyProperty = "pk", keyColumn = "pk")
    int insert(GameRecord game);

    @Update("UPDATE game SET time = #{time}, num_decks = #{numDecks} WHERE pk = #{pk}")
    int update(GameRecord game);

    @Delete("DELETE FROM game WHERE pk = #{pk}")
    int deleteById(@Param("pk") Integer pk);
}
