package xyz.tgscan.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.elasticsearch.annotations.Document;
import xyz.tgscan.enums.IdxConstant;

@Data
@Accessors(chain = true)
@Document(indexName = IdxConstant.ROOM_IDX, createIndex = false)
public class SearchRespDTO {
  private List<Object> doc;
  private Long totalPage;

  public SearchRespDTO(long totalPage, List<Object> docs) {
    this.totalPage = totalPage;
    this.doc = docs;
  }
}
