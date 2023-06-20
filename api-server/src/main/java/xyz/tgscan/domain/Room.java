package xyz.tgscan.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "room", schema = "public", catalog = "demo")
@Accessors(chain = true)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Room {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  private Long id;

  @Basic
  @Column(name = "link")
  private String link;

  @Basic
  @Column(name = "name")
  private String name;

  @Basic
  @Column(name = "jhi_desc")
  private String jhiDesc;

  @Basic
  @Column(name = "member_cnt")
  private Integer memberCnt;

  @Basic
  @Column(name = "type")
  private String type;

  @Basic
  @Column(name = "status")
  private String status;

  @Basic
  @Column(name = "collected_at")
  private Timestamp collectedAt;
}
