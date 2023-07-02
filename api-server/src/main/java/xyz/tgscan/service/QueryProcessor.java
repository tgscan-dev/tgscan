package xyz.tgscan.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import xyz.tgscan.dto.QueryDTO;

@Component
public class QueryProcessor {

  private static final Map<Nature, Float> natureWeight = new HashMap<Nature, Float>();
  // 以下权重是根据词性的语义和功能进行简单的分配，可能不完全准确或合理，仅供参考
  // 一般来说，名词、动词、形容词、数词、量词等实词的权重较高，因为它们能够表达query的主要内容和意图
  // 而介词、连词、助词、标点符号等虚词的权重较低，因为它们主要起到连接和修饰的作用
  // 另外，一些专有名词、专名、缩略语等具有较强的区分度和指代性的词性的权重也较高
  // 以下是按照词性的字母顺序进行排序的
  static {

    // a 形容词
    natureWeight.put(Nature.a, 0.8f);
    // ad 副形词
    natureWeight.put(Nature.ad, 0.7f);
    // ag 形容词性语素
    natureWeight.put(Nature.ag, 0.6f);
    // al 形容词性惯用语
    natureWeight.put(Nature.al, 0.7f);
    // an 名形词
    natureWeight.put(Nature.an, 0.8f);
    // b 区别词
    natureWeight.put(Nature.b, 0.6f);
    // begin 仅用于始##始，不会出现在分词结果中
    natureWeight.put(Nature.begin, 0f);
    // bg 区别语素
    natureWeight.put(Nature.bg, 0.5f);
    // bl 区别词性惯用语
    natureWeight.put(Nature.bl, 0.6f);
    // c 连词
    natureWeight.put(Nature.c, 0.3f);
    // cc 并列连词
    natureWeight.put(Nature.cc, 0.4f);
    // d 副词
    natureWeight.put(Nature.d, 0.5f);
    // dg 辄,俱,复之类的副词
    natureWeight.put(Nature.dg, 0.4f);
    // dl 连语
    natureWeight.put(Nature.dl, 0.4f);
    // e 叹词
    natureWeight.put(Nature.e, 0.4f);
    // end 仅用于终##终，不会出现在分词结果中
    natureWeight.put(Nature.end, 0f);
    // f 方位词
    natureWeight.put(Nature.f, 0.6f);
    // g 学术词汇
    natureWeight.put(Nature.g, 0.8f);
    // gb 生物相关词汇
    natureWeight.put(Nature.gb, 0.9f);
    // gbc 生物类别
    natureWeight.put(Nature.gbc, 0.9f);
    // gc 化学相关词汇
    natureWeight.put(Nature.gc, 0.9f);
    // gm 数学相关词汇
    natureWeight.put(Nature.gm, 0.9f);
    // gp 物理相关词汇
    natureWeight.put(Nature.gp, 0.9f);
    // h 前缀
    natureWeight.put(Nature.h, 0.3f);
    // i 成语
    natureWeight.put(Nature.i, 0.7f);
    // j 简称略语
    natureWeight.put(Nature.j, 1f); // 简称略语具有较强的区分度和指代性，权重较高
    // k 后缀
    natureWeight.put(Nature.k, 0.3f);
    // l 习用语
    natureWeight.put(Nature.l, 0.7f);
    // m 数词
    natureWeight.put(Nature.m, 0.8f);
    // Mg 甲乙丙丁之类的数词
    natureWeight.put(Nature.Mg, 0.8f);
    // mq 数量词
    natureWeight.put(Nature.mq, 0.8f);
    // n 名词
    natureWeight.put(Nature.n, 1f);
    // nb 生物名
    natureWeight.put(Nature.nb, 0.9f);
    // nba 动物名
    natureWeight.put(Nature.nba, 0.9f);
    // nbc 动物纲目
    natureWeight.put(Nature.nbc, 0.9f);
    // nbp 植物名
    natureWeight.put(Nature.nbp, 0.9f);
    // nf 食品，比如“薯片”
    natureWeight.put(Nature.nf, 0.8f);
    // ng 名词性语素
    natureWeight.put(Nature.ng, 0.6f);
    // nh 医药疾病等健康相关名词
    natureWeight.put(Nature.nh, 0.9f);
    // nhd 疾病
    natureWeight.put(Nature.nhd, 0.9f);
    // nhm 药品
    natureWeight.put(Nature.nhm, 0.9f);
    // ni 机构相关（不是独立机构名）
    natureWeight.put(Nature.ni, 0.7f);
    // nic 下属机构
    natureWeight.put(Nature.nic, 0.8f);
    // nis 机构后缀
    natureWeight.put(Nature.nis, 0.7f);
    // nit 教育相关机构
    natureWeight.put(Nature.nit, 0.8f);
    // nl 名词性惯用语
    natureWeight.put(Nature.nl, 0.7f);
    // nm 物品名
    natureWeight.put(Nature.nm, 0.8f);
    // nmc 化学品名
    natureWeight.put(Nature.nmc, 0.9f);
    // nn 工作相关名词
    natureWeight.put(Nature.nn, 0.8f);
    // nnd 职业
    natureWeight.put(Nature.nnd, 0.8f);
    // nnt 职务职称
    natureWeight.put(Nature.nnt, 0.8f);
    // ns 地名
    natureWeight.put(Nature.ns, 1f); // 地名具有较强的区分度和指代性，权重较高
    // nsf 音译地名
    natureWeight.put(Nature.nsf, 1f); // 音译地名具有较强的区分度和指代性，权重较高
    // nt 机构团体名
    natureWeight.put(Nature.nt, 1f); // 机构团体名具有较强的区分度和指代性，权重较高
    // nth 医院
    natureWeight.put(Nature.nth, 1f); // 医院具有较强的区分度和指代性，权重较高
    // ntcb 银行
    natureWeight.put(Nature.ntcb, 1f); // 银行具有较强的区分度和指代性，权重较高
    // ntcf 工厂
    natureWeight.put(Nature.ntcf, 1f); // 工厂具有较强的区分度和指代性，权重较高
    // ntch 酒店宾馆
    natureWeight.put(Nature.ntch, 1f); // 酒店宾馆具有较强的区分度和指代性，权重较高
    // ntc 集团公司名
    natureWeight.put(Nature.ntc, 1f); // 集团公司名具有较强的区分度和指代性，权重

    natureWeight.put(Nature.nx, 1f);
  }

  public static void main(String[] args) {
    System.out.println(new QueryProcessor().process("最伟大的篮球"));
    System.out.println("  hi o  i ".replaceAll(" ", ""));
  }

  public QueryDTO process(String kw) {
    if (kw.length() > 38) {
      kw = kw.substring(0, 38);
    }

    var segment = HanLP.segment(kw);
    var tokens =
        segment.stream()
            .filter(x -> !x.nature.equals(Nature.w))
            .map(x -> x.word)
            .map(String::toLowerCase)
            .collect(Collectors.joining(" "));
    var termWeight =
        segment.stream()
            .filter(x -> !x.nature.equals(Nature.w))
            .map(x -> Pair.of(x.word.toLowerCase(), natureWeight.getOrDefault(x.nature, 0.1f)))
            .collect(
                Collectors.toMap(
                    Pair::getLeft, Pair::getRight, (a, b) -> (float) (a + Math.log1p(b))));
    return new QueryDTO().setKw(kw).setTokens(tokens).setTermWeight(termWeight);
  }
}
