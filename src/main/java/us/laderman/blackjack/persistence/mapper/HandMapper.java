package us.laderman.blackjack.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import us.laderman.blackjack.persistence.model.HandRecord;

@Mapper
public interface HandMapper {
    @Select("""
            SELECT pk, gameFk, isDealer AS dealer, isDoubled AS doubled,
                   isBlackjack AS blackjack, isInsured AS insured
            FROM hand WHERE pk = #{pk}
            """)
    HandRecord findById(@Param("pk") Integer pk);

    @Select("""
            SELECT pk, gameFk, isDealer AS dealer, isDoubled AS doubled,
                   isBlackjack AS blackjack, isInsured AS insured
            FROM hand WHERE gameFk = #{gameFk} ORDER BY pk
            """)
    List<HandRecord> findByGameId(@Param("gameFk") Integer gameFk);

    @Select("""
            SELECT pk, gameFk, isDealer AS dealer, isDoubled AS doubled,
                   isBlackjack AS blackjack, isInsured AS insured
            FROM hand ORDER BY pk
            """)
    List<HandRecord> findAll();

    @Insert("""
            INSERT INTO hand (gameFk, isDealer, isDoubled, isBlackjack, isInsured)
            VALUES (#{gameFk}, #{dealer}, #{doubled}, #{blackjack}, #{insured})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "pk", keyColumn = "pk")
    int insert(HandRecord hand);

    @Update("""
            UPDATE hand SET gameFk = #{gameFk}, isDealer = #{dealer},
                isDoubled = #{doubled}, isBlackjack = #{blackjack}, isInsured = #{insured}
            WHERE pk = #{pk}
            """)
    int update(HandRecord hand);

    @Delete("DELETE FROM hand WHERE pk = #{pk}")
    int deleteById(@Param("pk") Integer pk);
}
