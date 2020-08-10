package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.*;
import com.thoughtworks.rslist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired RankDtoRepository rankDtoRepository;
  @Autowired RankRecordRepository rankRecordRepository;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    voteRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
    userDto =
        UserDto.builder()
            .voteNum(10)
            .phone("188888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("idolice")
            .build();
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    mockMvc
        .perform(get("/rs/list"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[0]", not(hasKey("user"))))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }

  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll();
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }

  @Test
  void should_get_specified_rank_and_origin_event_not_be_deleted_when_price_of_rank_is_zero() throws Exception {
    UserDto userDtoTestData = UserDto.builder().userName("user").gender("male").age(20).email("user@gmail.com")
            .phone("12345678901").build();
    userRepository.save(userDtoTestData);
    RsEventDto rsEventDtoTestData1 = RsEventDto.builder().eventName("event1").keyword("keyword1").voteNum(10)
            .user(userDtoTestData).build();
    RsEventDto rsEventDtoTestData2 = RsEventDto.builder().eventName("event2").keyword("keyword2").voteNum(0)
            .user(userDtoTestData).build();
    RsEventDto rsEventDtoTestData3 = RsEventDto.builder().eventName("event3").keyword("keyword3").voteNum(5)
            .user(userDtoTestData).build();
    rsEventRepository.save(rsEventDtoTestData1);
    rsEventRepository.save(rsEventDtoTestData2);
    rsEventRepository.save(rsEventDtoTestData3);
    assertEquals(3, rsEventRepository.count());
    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("event1")))
            .andExpect(jsonPath("$[0].keyword", is("keyword1")))
            .andExpect(jsonPath("$[0].voteNum", is(10)))
            .andExpect(jsonPath("$[1].eventName", is("event3")))
            .andExpect(jsonPath("$[1].keyword", is("keyword3")))
            .andExpect(jsonPath("$[1].voteNum", is(5)))
            .andExpect(jsonPath("$[2].eventName", is("event2")))
            .andExpect(jsonPath("$[2].keyword", is("keyword2")))
            .andExpect(jsonPath("$[2].voteNum", is(0)))
            .andExpect(status().isOk());
    assertEquals(0, rankDtoRepository.count());

    ObjectMapper objectMapper = new ObjectMapper();
    Trade trade = new Trade(100, 1);
    mockMvc.perform(post("/rs/buy/3").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(trade))).andExpect(status().isOk());

    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("event3")))
            .andExpect(jsonPath("$[0].keyword", is("keyword3")))
            .andExpect(jsonPath("$[0].voteNum", is(5)))
            .andExpect(jsonPath("$[1].eventName", is("event1")))
            .andExpect(jsonPath("$[1].keyword", is("keyword1")))
            .andExpect(jsonPath("$[1].voteNum", is(10)))
            .andExpect(jsonPath("$[2].eventName", is("event2")))
            .andExpect(jsonPath("$[2].keyword", is("keyword2")))
            .andExpect(jsonPath("$[2].voteNum", is(0)))
            .andExpect(status().isOk());
    List<RankDto> rankDtoList = rankDtoRepository.findAll();
    assertEquals(1, rankDtoList.size());
    assertEquals(100, rankDtoList.get(0).getPrice());
    assertEquals(1, rankDtoList.get(0).getRankPos());
    assertEquals(3, rankDtoList.get(0).getRsEventId());

    List<RankRecordDto> rankRecordList = rankRecordRepository.findAll();
    assertEquals(1, rankRecordList.size());
    assertEquals(100, rankRecordList.get(0).getPrice());
    assertEquals(1, rankRecordList.get(0).getRankPos());
    assertEquals(3, rankRecordList.get(0).getRsEventId());
  }

  @Test
  void should_get_specified_rank_and_delete_origin_event_when_price_or_rank_is_not_zero() throws Exception {
    UserDto userDtoTestData = UserDto.builder().userName("user").gender("male").age(20).email("user@gmail.com")
            .phone("12345678901").build();
    userRepository.save(userDtoTestData);
    RsEventDto rsEventDtoTestData1 = RsEventDto.builder().eventName("event1").keyword("keyword1").voteNum(10)
            .user(userDtoTestData).build();
    RsEventDto rsEventDtoTestData2 = RsEventDto.builder().eventName("event2").keyword("keyword2").voteNum(0)
            .user(userDtoTestData).build();
    RsEventDto rsEventDtoTestData3 = RsEventDto.builder().eventName("event3").keyword("keyword3").voteNum(5)
            .user(userDtoTestData).build();
    rsEventRepository.save(rsEventDtoTestData1);
    rsEventRepository.save(rsEventDtoTestData2);
    rsEventRepository.save(rsEventDtoTestData3);
    assertEquals(3, rsEventRepository.count());
    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("event1")))
            .andExpect(jsonPath("$[0].keyword", is("keyword1")))
            .andExpect(jsonPath("$[0].voteNum", is(10)))
            .andExpect(jsonPath("$[1].eventName", is("event3")))
            .andExpect(jsonPath("$[1].keyword", is("keyword3")))
            .andExpect(jsonPath("$[1].voteNum", is(5)))
            .andExpect(jsonPath("$[2].eventName", is("event2")))
            .andExpect(jsonPath("$[2].keyword", is("keyword2")))
            .andExpect(jsonPath("$[2].voteNum", is(0)))
            .andExpect(status().isOk());
    assertEquals(0, rankDtoRepository.count());

    ObjectMapper objectMapper = new ObjectMapper();
    Trade trade1 = new Trade(100, 1);
    mockMvc.perform(post("/rs/buy/3").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(trade1))).andExpect(status().isOk());

    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("event3")))
            .andExpect(jsonPath("$[0].keyword", is("keyword3")))
            .andExpect(jsonPath("$[0].voteNum", is(5)))
            .andExpect(jsonPath("$[1].eventName", is("event1")))
            .andExpect(jsonPath("$[1].keyword", is("keyword1")))
            .andExpect(jsonPath("$[1].voteNum", is(10)))
            .andExpect(jsonPath("$[2].eventName", is("event2")))
            .andExpect(jsonPath("$[2].keyword", is("keyword2")))
            .andExpect(jsonPath("$[2].voteNum", is(0)))
            .andExpect(status().isOk());
    List<RankDto> rankDtoList = rankDtoRepository.findAll();
    assertEquals(1, rankDtoList.size());
    assertEquals(100, rankDtoList.get(0).getPrice());
    assertEquals(1, rankDtoList.get(0).getRankPos());
    assertEquals(3, rankDtoList.get(0).getRsEventId());

    Trade trade2 = new Trade(500, 1);
    mockMvc.perform(post("/rs/buy/2").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(trade2))).andExpect(status().isOk());

    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].eventName", is("event2")))
            .andExpect(jsonPath("$[0].keyword", is("keyword2")))
            .andExpect(jsonPath("$[0].voteNum", is(0)))
            .andExpect(jsonPath("$[1].eventName", is("event1")))
            .andExpect(jsonPath("$[1].keyword", is("keyword1")))
            .andExpect(jsonPath("$[1].voteNum", is(10)))
            .andExpect(status().isOk());
    rankDtoList = rankDtoRepository.findAll();
    assertEquals(1, rankDtoList.size());
    assertEquals(500, rankDtoList.get(0).getPrice());
    assertEquals(1, rankDtoList.get(0).getRankPos());
    assertEquals(2, rankDtoList.get(0).getRsEventId());

    List<RankRecordDto> rankRecordList = rankRecordRepository.findAll();
    assertEquals(2, rankRecordList.size());
    assertEquals(100, rankRecordList.get(0).getPrice());
    assertEquals(1, rankRecordList.get(0).getRankPos());
    assertEquals(3, rankRecordList.get(0).getRsEventId());
    assertEquals(500, rankRecordList.get(1).getPrice());
    assertEquals(1, rankRecordList.get(1).getRankPos());
    assertEquals(2, rankRecordList.get(1).getRsEventId());
  }

  @Test
  void should_get_bad_request_when_amount_is_not_enough_to_buy_the_rank() throws Exception {
    UserDto userDtoTestData = UserDto.builder().userName("user").gender("male").age(20).email("user@gmail.com")
            .phone("12345678901").build();
    userRepository.save(userDtoTestData);
    RsEventDto rsEventDtoTestData1 = RsEventDto.builder().eventName("event1").keyword("keyword1").voteNum(10)
            .user(userDtoTestData).build();
    RsEventDto rsEventDtoTestData2 = RsEventDto.builder().eventName("event2").keyword("keyword2").voteNum(0)
            .user(userDtoTestData).build();
    RsEventDto rsEventDtoTestData3 = RsEventDto.builder().eventName("event3").keyword("keyword3").voteNum(5)
            .user(userDtoTestData).build();
    rsEventRepository.save(rsEventDtoTestData1);
    rsEventRepository.save(rsEventDtoTestData2);
    rsEventRepository.save(rsEventDtoTestData3);
    assertEquals(3, rsEventRepository.count());
    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("event1")))
            .andExpect(jsonPath("$[0].keyword", is("keyword1")))
            .andExpect(jsonPath("$[0].voteNum", is(10)))
            .andExpect(jsonPath("$[1].eventName", is("event3")))
            .andExpect(jsonPath("$[1].keyword", is("keyword3")))
            .andExpect(jsonPath("$[1].voteNum", is(5)))
            .andExpect(jsonPath("$[2].eventName", is("event2")))
            .andExpect(jsonPath("$[2].keyword", is("keyword2")))
            .andExpect(jsonPath("$[2].voteNum", is(0)))
            .andExpect(status().isOk());
    assertEquals(0, rankDtoRepository.count());

    ObjectMapper objectMapper = new ObjectMapper();
    Trade trade1 = new Trade(100, 1);
    mockMvc.perform(post("/rs/buy/3").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(trade1))).andExpect(status().isOk());

    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].eventName", is("event3")))
            .andExpect(jsonPath("$[0].keyword", is("keyword3")))
            .andExpect(jsonPath("$[0].voteNum", is(5)))
            .andExpect(jsonPath("$[1].eventName", is("event1")))
            .andExpect(jsonPath("$[1].keyword", is("keyword1")))
            .andExpect(jsonPath("$[1].voteNum", is(10)))
            .andExpect(jsonPath("$[2].eventName", is("event2")))
            .andExpect(jsonPath("$[2].keyword", is("keyword2")))
            .andExpect(jsonPath("$[2].voteNum", is(0)))
            .andExpect(status().isOk());
    List<RankDto> rankDtoList = rankDtoRepository.findAll();
    assertEquals(1, rankDtoList.size());
    assertEquals(100, rankDtoList.get(0).getPrice());
    assertEquals(1, rankDtoList.get(0).getRankPos());
    assertEquals(3, rankDtoList.get(0).getRsEventId());

    Trade trade2 = new Trade(50, 1);
    mockMvc.perform(post("/rs/buy/2").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(trade2))).andExpect(status().isBadRequest());
  }
}
