package xyz.tgscan.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.elasticsearch.annotations.Document;
import xyz.tgscan.enums.IdxConstant;

@Data
@Accessors(chain = true)
@Document(indexName = IdxConstant.ROOM_IDX, createIndex = false)
public class RoomDoc {
  @JsonIgnore private String _class;
  @Id private String id;

  private String link;
  private String userName;
  private String name;
  private String phraseName;
  private String standardName;

  private String jhiDesc;
  private String standardJhiDesc;
  private String phraseJhiDesc;

  private Integer memberCnt;
  private List<String> tags;
  private List<String> lang;
  private List<String> category;

  private String type;

  private String status;

  private Date sendTime;

  public static RoomDoc fromEntity(Room room) {
    RoomDoc roomDoc = new RoomDoc();
    roomDoc.setId(room.getLink());
    roomDoc.setPhraseName(room.getName());
    roomDoc.setStandardName(room.getName());
    var split = room.getLink().split("/");
    roomDoc.setUserName(split[split.length - 1]);
    roomDoc.setPhraseJhiDesc(room.getJhiDesc());
    roomDoc.setStandardJhiDesc(room.getJhiDesc());
    roomDoc.setLink(room.getLink());
    roomDoc.setName(room.getName());
    roomDoc.setJhiDesc(room.getJhiDesc());
    roomDoc.setMemberCnt(room.getMemberCnt());
    roomDoc.setType(room.getType());
    roomDoc.setStatus(room.getStatus());
    roomDoc.setSendTime(new Date(0));
    var newLang = Arrays.stream(Optional.ofNullable(room.getLang())
            .orElse("")
            .split(",")).map(String::trim)
            .map(String::toLowerCase).distinct().collect(Collectors.toList());
    roomDoc.setLang(newLang);
    var tags = Optional.ofNullable(room.getTags()).orElse("").split(",");
    var newTags = Arrays.stream(tags).map(String::trim).map(String::toLowerCase).distinct().collect(Collectors.toList());
    roomDoc.setTags(newTags);
    var newCategory = Arrays.stream(Optional.ofNullable(room.getCategory()).orElse("").split(",")).map(String::trim).distinct().collect(Collectors.toList());
    roomDoc.setCategory(newCategory);
    return roomDoc;
  }
}
