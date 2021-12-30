package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import java.util.concurrent.*;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final PageParserFactory parserFactory;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          PageParserFactory parserFactory,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls
          ) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.parserFactory = parserFactory;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visitedUrls = new HashSet<>();


    for (String url : startingUrls) {
      pool.invoke(new crawInternalAction(url, deadline, maxDepth, visitedUrls, counts));
    }

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
              .setWordCounts(counts)
              .setUrlsVisited(visitedUrls.size())
              .build();
    }

    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build();
  }

  public final class crawInternalAction extends RecursiveAction
  {
    private final String url;
    Instant deadline;
    int maxDepth;
    Set<String> visitedUrls;
    Map<String, Integer> counts;


    public crawInternalAction(String url, Instant deadline, int maxDepth,
                              Set<String> visitedUrls, Map<String, Integer> counts){
      this.url = url;
      this.deadline = deadline;
      this.maxDepth = maxDepth;
      this.visitedUrls = visitedUrls;
      this.counts = counts;
    }

    @Override
    protected void compute() {
      if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
        return;
      }
      for (Pattern pattern : ignoredUrls) {
        if (pattern.matcher(url).matches()) {
          return;
        }
      }
      if (!visitedUrls.add(url)) {
        return;
      }
      Stream<String> subUrls;
      PageParser.Result result = parserFactory.get(url).parse();
      ExecutorService executor = Executors.newFixedThreadPool(12);
      List<Future<?>> futures = new ArrayList<>();
      for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
        futures.add(executor.submit(() -> {
          counts.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : e.getValue() + counts.get(e.getKey()));
        }));
      }

      for(Future<?> future : futures){
        try {
          future.get();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      executor.shutdown();

      try{
        subUrls = result.getLinks().stream();
      }
      catch (Exception ex){
        ex.printStackTrace();
        return;
      }
      List<crawInternalAction> subActions = subUrls.map(url -> new crawInternalAction(url, deadline, maxDepth-1, visitedUrls, counts))
              .collect(Collectors.toList());
      invokeAll(subActions);
    }
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
