package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RankDto;
import com.thoughtworks.rslist.dto.RankRecordDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RankRecordRepository extends CrudRepository<RankRecordDto, Integer> {

    @Override
    List<RankRecordDto> findAll();
}
