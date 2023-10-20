package xyz.tgscan.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import xyz.tgscan.dto.QueryDTO;

@Component
public class QueryProcessor {

  private static final Map<Nature, Float> natureWeight = new HashMap<Nature, Float>();

  static {

    natureWeight.put(Nature.a, 0.8f);
    
    natureWeight.put(Nature.ad, 0.7f);
    
    natureWeight.put(Nature.ag, 0.6f);
    
    natureWeight.put(Nature.al, 0.7f);
    
    natureWeight.put(Nature.an, 0.8f);
    
    natureWeight.put(Nature.b, 0.6f);
    
    natureWeight.put(Nature.begin, 0f);
    
    natureWeight.put(Nature.bg, 0.5f);
    
    natureWeight.put(Nature.bl, 0.6f);
    
    natureWeight.put(Nature.c, 0.3f);
    
    natureWeight.put(Nature.cc, 0.4f);
    
    natureWeight.put(Nature.d, 0.5f);
    
    natureWeight.put(Nature.dg, 0.4f);
    
    natureWeight.put(Nature.dl, 0.4f);
    
    natureWeight.put(Nature.e, 0.4f);
    
    natureWeight.put(Nature.end, 0f);
    
    natureWeight.put(Nature.f, 0.6f);
    
    natureWeight.put(Nature.g, 0.8f);
    
    natureWeight.put(Nature.gb, 0.9f);
    
    natureWeight.put(Nature.gbc, 0.9f);
    
    natureWeight.put(Nature.gc, 0.9f);
    
    natureWeight.put(Nature.gm, 0.9f);
    
    natureWeight.put(Nature.gp, 0.9f);
    
    natureWeight.put(Nature.h, 0.3f);
    
    natureWeight.put(Nature.i, 0.7f);
    
    natureWeight.put(Nature.j, 1f); 
    
    natureWeight.put(Nature.k, 0.3f);
    
    natureWeight.put(Nature.l, 0.7f);
    
    natureWeight.put(Nature.m, 0.8f);
    
    natureWeight.put(Nature.Mg, 0.8f);
    
    natureWeight.put(Nature.mq, 0.8f);
    
    natureWeight.put(Nature.n, 1f);
    
    natureWeight.put(Nature.nb, 0.9f);
    
    natureWeight.put(Nature.nba, 0.9f);
    
    natureWeight.put(Nature.nbc, 0.9f);
    
    natureWeight.put(Nature.nbp, 0.9f);
    
    natureWeight.put(Nature.nf, 0.8f);
    
    natureWeight.put(Nature.ng, 0.6f);
    
    natureWeight.put(Nature.nh, 0.9f);
    
    natureWeight.put(Nature.nhd, 0.9f);
    
    natureWeight.put(Nature.nhm, 0.9f);
    
    natureWeight.put(Nature.ni, 0.7f);
    
    natureWeight.put(Nature.nic, 0.8f);
    
    natureWeight.put(Nature.nis, 0.7f);
    
    natureWeight.put(Nature.nit, 0.8f);
    
    natureWeight.put(Nature.nl, 0.7f);
    
    natureWeight.put(Nature.nm, 0.8f);
    
    natureWeight.put(Nature.nmc, 0.9f);
    
    natureWeight.put(Nature.nn, 0.8f);
    
    natureWeight.put(Nature.nnd, 0.8f);
    
    natureWeight.put(Nature.nnt, 0.8f);
    
    natureWeight.put(Nature.ns, 1f); 
    
    natureWeight.put(Nature.nsf, 1f); 
    
    natureWeight.put(Nature.nt, 1f); 
    
    natureWeight.put(Nature.nth, 1f); 
    
    natureWeight.put(Nature.ntcb, 1f); 
    
    natureWeight.put(Nature.ntcf, 1f); 
    
    natureWeight.put(Nature.ntch, 1f); 
    
    natureWeight.put(Nature.ntc, 1f); 

    natureWeight.put(Nature.nx, 1f);
  }

   public QueryDTO process(String kw, Set<String> tags, String category, String lang) {
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
    return new QueryDTO().setKw(kw).setTokens(tokens)
            .setTermWeight(termWeight).setTags(tags.stream().map(String::toLowerCase).collect(Collectors.toSet()))
            .setLang(lang.toLowerCase())
            .setCategory(category.toLowerCase());
  }
}
