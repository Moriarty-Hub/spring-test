package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RankDto;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.FailedToBuyRankException;
import com.thoughtworks.rslist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock RankDtoRepository rankDtoRepository;
  @Mock RankRecordRepository rankRecordRepository;
  LocalDateTime localDateTime;
  Vote vote;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, rankDtoRepository, rankRecordRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void should_save_rankDto_and_rankRecordDto_into_repository_directly_when_rank_record_was_not_found() {
    // given
    when(rankDtoRepository.findRankDtoByRankPos(anyInt())).thenReturn(Optional.empty());
    Trade trade = new Trade(100, 1);
    int eventId = 1;

    // when
    rsService.buy(trade, eventId);

    // then
    verify(rankDtoRepository, times(1)).save(any());
    verify(rankRecordRepository, times(1)).save(any());
  }

  @Test
  void should_delete_origin_event_when_rank_record_was_found_and_amount_is_enough() {
    // given
    Trade trade = new Trade(200, 1);
    int eventId = 2;
    RankDto rankDto = new RankDto(1, 1, 100, 1);
    when(rankDtoRepository.findRankDtoByRankPos(anyInt())).thenReturn(Optional.of(rankDto));

    // when
    rsService.buy(trade, eventId);

    // then
    verify(rsEventRepository, times(1)).deleteById(1);
    verify(rankDtoRepository, times(1)).deleteById(1);
    verify(rankDtoRepository, times(1)).save(any());
    verify(rankRecordRepository, times(1)).save(any());
  }

  @Test
  void should_throw_exception_when_amount_is_not_enough() {
    // given
    Trade trade = new Trade(50, 1);
    int eventId = 2;
    RankDto rankDto = new RankDto(1, 1, 100, 1);
    when(rankDtoRepository.findRankDtoByRankPos(anyInt())).thenReturn(Optional.of(rankDto));

    // when & then
    assertThrows(FailedToBuyRankException.class, () -> rsService.buy(trade, eventId));
  }


}
