package us.laderman.blackjack.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import us.laderman.blackjack.persistence.model.ResultRecord;

@Mapper
public interface ResultMapper {
    @Select("SELECT pk, handFk, wager, won FROM result WHERE pk = #{pk}")
    ResultRecord findById(@Param("pk") Integer pk);

    @Select("SELECT pk, handFk, wager, won FROM result WHERE handFk = #{handFk} ORDER BY pk")
    List<ResultRecord> findByHandId(@Param("handFk") Integer handFk);

    @Select("SELECT pk, handFk, wager, won FROM result ORDER BY pk")
    List<ResultRecord> findAll();

    @Insert("INSERT INTO result (handFk, wager, won) VALUES (#{handFk}, #{wager}, #{won})")
    @Options(useGeneratedKeys = true, keyProperty = "pk", keyColumn = "pk")
    int insert(ResultRecord result);

    @Update("UPDATE result SET handFk = #{handFk}, wager = #{wager}, won = #{won} WHERE pk = #{pk}")
    int update(ResultRecord result);

    @Delete("DELETE FROM result WHERE pk = #{pk}")
    int deleteById(@Param("pk") Integer pk);
}
