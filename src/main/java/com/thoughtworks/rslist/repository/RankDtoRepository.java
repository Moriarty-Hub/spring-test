package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RankDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RankDtoRepository extends CrudRepository<RankDto, Integer> {

    @Override
    List<RankDto> findAll();

    Optional<RankDto> findRankDtoByRankPos(int rankPos);

    @Query("DELETE FROM RankDto rankDto WHERE rankDto.rankPos = :rankPos")
    void deleteByRankPos(@Param("rankPos") int rankPos);
}
