package us.laderman.blackjack.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import us.laderman.blackjack.persistence.model.CardRecord;

@Mapper
public interface CardMapper {
    @Select("SELECT pk, handFk, suit, `value` FROM card WHERE pk = #{pk}")
    CardRecord findById(@Param("pk") Integer pk);

    @Select("SELECT pk, handFk, suit, `value` FROM card WHERE handFk = #{handFk} ORDER BY pk")
    List<CardRecord> findByHandId(@Param("handFk") Integer handFk);

    @Select("SELECT pk, handFk, suit, `value` FROM card ORDER BY pk")
    List<CardRecord> findAll();

    @Insert("INSERT INTO card (handFk, suit, `value`) VALUES (#{handFk}, #{suit}, #{value})")
    @Options(useGeneratedKeys = true, keyProperty = "pk", keyColumn = "pk")
    int insert(CardRecord card);

    @Update("UPDATE card SET handFk = #{handFk}, suit = #{suit}, `value` = #{value} WHERE pk = #{pk}")
    int update(CardRecord card);

    @Delete("DELETE FROM card WHERE pk = #{pk}")
    int deleteById(@Param("pk") Integer pk);
}
