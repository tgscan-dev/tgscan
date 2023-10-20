package xyz.tgscan.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "search_log", schema = "public", catalog = "demo")
public class SearchLog {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  private Long id;

  @Basic
  @Column(name = "search_at")
  private Timestamp searchAt;

  @Basic
  @Column(name = "kw")
  private String kw;

  @Basic
  @Column(name = "t")
  private String t;

  @Basic
  @Column(name = "p")
  private Integer p;

  @Basic
  @Column(name = "ip")
  private String ip;

  @Basic
  @Column(name = "tags")
  private String tags;

  @Basic
  @Column(name = "category")
  private String category;



  @Basic
  @Column(name = "lang")
  private String lang;
}
