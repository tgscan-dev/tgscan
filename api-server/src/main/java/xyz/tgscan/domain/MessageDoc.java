package xyz.tgscan.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.annotations.Document;
import xyz.tgscan.enums.IdxConstant;
import xyz.tgscan.enums.TgRoomTypeParamEnum;

@Data
@Slf4j
@Accessors(chain = true)
@Document(indexName = IdxConstant.MESSAGE_IDX, createIndex = false)
public class MessageDoc {
  private static final Set<String> namePrefix =
      Stream.of(
              "\uD83D\uDCCC 名称 | ",
              "名称：",
              "名称:",
              "资源标题：",
              "资源名称：",
              "中文标题：",
              "片名：",
              "资源名称:",
              "中文片名：",
              "资源名字：",
              "资源名字:",
              "中文片名: ")
          .collect(Collectors.toSet());
  @JsonIgnore private String _class;
  @Id private String id;
  private String content;
  private String phraseContent;
  private String type;
  private String username;
  private Long offset;
  private String title;
  private String phraseTitle;
  private List<String> tags;
  private Date sendTime;

  public static MessageDoc fromEntity(Message messages) {
    MessageDoc messageDoc = new MessageDoc();
    messageDoc.setId(messages.getId().toString());
    var msg = messages.getContent();

    var split = msg.trim().split("\n");
    var title =
        Arrays.stream(split)
            .filter(StringUtils::isNotEmpty)
            .filter(y -> namePrefix.stream().anyMatch(y::startsWith))
            .findFirst()
            .orElse(Arrays.stream(split).findFirst().orElse(""))
            .replaceAll(String.join("|", namePrefix), "");
    var tags = parseTags(msg);
    messageDoc.setTitle(title);
    messageDoc.setPhraseTitle(title);
    messageDoc.setTags(tags);

    messageDoc.setContent(msg);
    messageDoc.setPhraseContent(msg);
    messageDoc.setType(TgRoomTypeParamEnum.MESSAGE.name());
    messageDoc.setUsername(messages.getUsername());
    messageDoc.setOffset(messages.getOffset());
    messageDoc.setSendTime(messages.getSendTime());
    return messageDoc;
  }

  private static ArrayList<String> parseTags(String input) {
    Pattern pattern = Pattern.compile("#\\S+");
    Matcher matcher = pattern.matcher(input);
    var strings = new ArrayList<String>();
    while (matcher.find()) {
      String tag = matcher.group();
      strings.add(tag.substring(1));
    }
    return strings;
  }
}
