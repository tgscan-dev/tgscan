package xyz.tgscan.enums;

import java.util.List;

public class IdxConstant {
  public static final String ROOM_IDX = "room.0506";
  public static final String AUTOCOMPLETE_IDX = "autocomplete.0506";
  public static final String ROOM_NAME = "name";
  public static final String ROOM_MEMBER_CNT = "memberCnt";
  public static final String ROOM_TYPE = "type";
  public static final String ROOM_STATUS= "status";
  public static final String ROOM_DESC = "jhiDesc";
  public static final String AUTOCOMPLETE = "autoComplete";

  public static final String MESSAGE_IDX = "message.0502";

  public static final String MESSAGE_TITLE = "title";
  public static final String MESSAGE_SEND_TIME = "sendTime";
  public static final String MESSAGE_TYPE = "type";
  public static final String MESSAGE_CONTENT = "content";
  public static final List<String> AUTOCOMPLETE_FIELD =
      List.of(
          "suggest",
          "suggest._2gram",
          "suggest._3gram",
          "suggestPinyinFirstLetter",
          "suggestPinyinFirstLetter._2gram",
          "suggestPinyinFirstLetter._index_prefix",
          "suggestPinyinNormal",
          "suggestPinyinNormal._2gram",
          "suggestPinyinNormal._index_prefix");
}
