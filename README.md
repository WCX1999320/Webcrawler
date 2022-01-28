# Parallel Web Crawler

# Introduction
The source code for a legacy web crawler, which is single-threaded. Here, we need to use  advanced features as multi-threading, reflection, design patterns, AOP, Functional Programming and I/O Files to increase crawler throughput.Furthermore, we applied the AOP to measure the performance of  crawler.Finally, the new parallel web crawler was five times more efficient than the legacy single-thread one.

1.Implemented crawler configuration and crawler output by using Jackson.
2.Applied ForkJoinPool to make the legacy web crawler parallel and improved the crawler five times more efficient.
3.Used concurrentHashMap to avoid the same URL be counted twice int this parallel web crawler.
4.Implemented the final word count sorting using only functional programming techniques.
5.Implemented a basic method profiler by appling AOP.

# Tech Stack

Jackson + Guice(dependency injection) + Concurrency(ForkJoinPool) + Unit Test + Design Patterns + AOP

# Configuration

## JSON Configuration Example

![image](https://user-images.githubusercontent.com/90006503/151612321-37675626-97aa-428a-b398-642da372bbbf.png)


### StartPages 
These URLs are the starting point of the web crawl.
### IgnoredUrls 
A list of regular expressions defining which, if any, URLs should not be followed by the web crawler. In this example, the second starting page will be ignored.
ignoredWords - A list of regular expressions defining which words, if any, should not be counted toward the popular word count. In this example, words with 3 or fewer characters are ignored.
### Parallelism 
The desired parallelism that should be used for the web crawl. If set to 1, the legacy crawler should be used. If less than 1, parallelism should default to the number of cores on the system.
### ImplementationOverride 
An explicit override for which web crawler implementation should be used for this crawl. In this example, the legacy crawler will always be used, regardless of the value of the "parallelism" option.
If this option is empty or unset, the "parallelism" option will be used (instead of the "implementationOverride" option) to determine which crawler to use. If this option is set to a non-empty string that is not the fully-qualified name of a class that implements the WebCrawler interface, the crawler will immediately fail.

### MaxDepth 
The max depth of the crawl. The "depth" of a crawl is the maximum number of links the crawler is allowed to follow from the starting pages before it must stop. This option can be used to limit how far the crawler drifts from the starting URLs, or can be set to a very high number if that doesn't matter.

### TimeoutSeconds 
The max amount of time the crawler is allowed to run, in seconds. Once this amount of time has been reached, the crawler will finish processing any HTML it has already downloaded, but it is not allowed to download any more HTML or follow any more hyperlinks.

### PopularWordCount 
The number of popular words to record in the output. In this example, the 3 most frequent words will be recorded. If there is a tie in the top 3, word length is used as a tiebreaker, with longer words taking preference. If the words are the same length, words that come first alphabetically get ranked higher.

### ProfileOutputPath 
Path to the output file where performance data for this web crawl should be written. If there is already a file at that path, the new data should be appended. If this option is empty or unset, the profile data should be printed to standard output.

### ResultPath 
Path where the web crawl result JSON should be written. If a file already exists at that path, it should be overwritten. If this option is empty or unset, the result should be printed to standard output.
