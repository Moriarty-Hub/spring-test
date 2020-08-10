package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RankDto;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RankDtoRepository;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.service.RsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Validated
public class RsController {
  @Autowired RsEventRepository rsEventRepository;
  @Autowired UserRepository userRepository;
  @Autowired RankDtoRepository rankDtoRepository;
  @Autowired RsService rsService;

  @GetMapping("/rs/list")
  public ResponseEntity<List<RsEvent>> getRsEventListBetween(
      @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    /*List<RsEvent> rsEvents =
        rsEventRepository.findAll().stream()
            .map(
                item ->
                    RsEvent.builder()
                        .eventName(item.getEventName())
                        .keyword(item.getKeyword())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .build())
            .collect(Collectors.toList());
    if (start == null || end == null) {
      return ResponseEntity.ok(rsEvents);
    }
    return ResponseEntity.ok(rsEvents.subList(start - 1, end));*/
    List<RsEvent> rsEvents =
            rsEventRepository.findAll().stream()
                    .map(
                            item ->
                                    RsEvent.builder()
                                            .eventName(item.getEventName())
                                            .keyword(item.getKeyword())
                                            .userId(item.getId())
                                            .voteNum(item.getVoteNum())
                                            .build()).collect(Collectors.toList());
    List<RankDto> rankDtoList = rankDtoRepository.findAll();
    List<RsEvent> sortedRsEventList = new ArrayList<>(rsEvents.size());
    for (int i = 0; i < rsEvents.size(); i++) {
      sortedRsEventList.add(null);
    }
    for (RankDto rankDto : rankDtoList) {
      sortedRsEventList.set(rankDto.getRankPos() - 1, rsEvents.remove((int) rankDto.getRsEventId() - 1));
    }
    rsEvents = rsEvents.stream().sorted(Comparator.comparing(RsEvent::getVoteNum).reversed()).collect(Collectors.toList());
    for (int i = 0; i < sortedRsEventList.size(); i++) {
      if (sortedRsEventList.get(i) == null) {
        sortedRsEventList.set(i, rsEvents.remove(0));
      }
    }
    if (start == null || end == null) {
      return ResponseEntity.ok(sortedRsEventList);
    }
    return ResponseEntity.ok(sortedRsEventList.subList(start - 1, end));
  }

  @GetMapping("/rs/{index}")
  public ResponseEntity<RsEvent> getRsEvent(@PathVariable int index) {
    List<RsEvent> rsEvents =
        rsEventRepository.findAll().stream()
            .map(
                item ->
                    RsEvent.builder()
                        .eventName(item.getEventName())
                        .keyword(item.getKeyword())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .build())
            .collect(Collectors.toList());
    if (index < 1 || index > rsEvents.size()) {
      throw new RequestNotValidException("invalid index");
    }
    return ResponseEntity.ok(rsEvents.get(index - 1));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
    Optional<UserDto> userDto = userRepository.findById(rsEvent.getUserId());
    if (!userDto.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    RsEventDto build =
        RsEventDto.builder()
            .keyword(rsEvent.getKeyword())
            .eventName(rsEvent.getEventName())
            .voteNum(0)
            .user(userDto.get())
            .build();
    rsEventRepository.save(build);
    return ResponseEntity.created(null).build();
  }

  @PostMapping("/rs/vote/{id}")
  public ResponseEntity vote(@PathVariable int id, @RequestBody Vote vote) {
    rsService.vote(vote, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/rs/buy/{id}")
  public ResponseEntity buy(@PathVariable int id, @RequestBody Trade trade){
    rsService.buy(trade, id);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler(RequestNotValidException.class)
  public ResponseEntity<Error> handleRequestErrorHandler(RequestNotValidException e) {
    Error error = new Error(e.getMessage());
    return ResponseEntity.badRequest().body(error);
  }
}
