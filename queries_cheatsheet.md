# Index Management

#### Create and configure indexes

```http request

### Create index with settings and mappings
PUT /products
Content-Type: application/json

{
    "settings": {
        "number_of_shards": 1, # can't change after index creation
        "number_of_replicas": 3 # can change at any time
    },
    "mappings": {
        "properties": {
            "name": {
                "type": "text"
            },
            "description": {
                "type": "text"
            },
            "price": {
                "type": "float"
            }
        }
    }
}

### Get Index Settings
GET /products/_settings

### Update Index Settings
PUT /products/_settings
Content-Type: application/json

{
  "settings": {
    "index": {
      "number_of_replicas": 1
    }
  }
}


### Configure Analyzer (also in settings)
PUT /russian_words_idx
Content-Type: application/json

{
  "settings": {
    "analysis": {
        "analyzer": "russian"
    }
  }
}

### Analyze String
GET /_analyze
Content-Type: application/json

{
    "analyzer": "russian",
    "text": "Привет. Я проживаю в Санкт-Петерубрге. Это очень красивое место"
}
```

# Search

## Structured Search

Query structured data: dates, times, numbers,...
Text **can** be structured too: enums, some structured identifiers
With structured search, the answer to your question is always a yes or no;
something either belongs in the set or it does not

This should make sense logically. A number can’t be more in a range than
any other number that falls in the same range.
It is either in the range or it isn’t.
Similarly, for structured text, a value is either equal or it isn’t. There is no concept of more similar.

### Finding Exact Values:

[From The Definitive Guide Book](https://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html)

```http request
### Filter Query From Range
GET /products/_search
Content-Type: application/json

{
    "query" : {
        "constant_score" : {
            "filter" : {
                "range" : {
                    "price" : {
                        "gte" : 20,
                        "lt"  : 40
                    }
                }
            }
        }
    }
}
```

### Dealing with Null Values

Think back to our earlier example, where documents have a field named tags. This is a multivalue field. A document may
have one tag, many tags, or potentially no tags at all. If a field has no values, how is it stored in an inverted index?

That’s a trick question, because the answer is: it isn’t stored at all. Let’s look at that inverted index from the
previous section:

Index is map: Token -> DocIDs:

```yaml
index:
  open_source: 2
  search: 1,2
```

How would you store a field that doesn’t exist in that data structure? You can’t!

#### exist Query

Consider following SQL query:

```SQL
SELECT tags
FROM posts
WHERE tags IS NOT NULL
```

In Elasticsearch, we use the **exists** query:

```http request
GET /my_index/posts/_search
Content-Type: application/json

{
    "query" : {
        "constant_score" : {
            "filter" : {
                "exists" : { "field" : "tags" }
            }
        }
    }
}
```

**SQL**

```SQL
SELECT tags
FROM posts
WHERE tags IS NULL
```

**ES**

```http request
GET /my_index/posts/_search
Content-Type: application/json

{
    "query" : {
        "constant_score" : {
            "filter": {
                "missing" : { "field" : "tags" }
            }
        }
    }
}
```

### All About Caching

[From The Definitive Guide Book](https://www.elastic.co/guide/en/elasticsearch/guide/current/filter-caching.html)

### Full-Text Search

The two most important aspects of full-text search are as follows:

**Relevance**:  
The ability to rank results by how relevant they are to the given query, whether relevance is calculated using TF/IDF,
proximity to a geolocation, fuzzy similarity, or some other algorithm.

**Analysis**:  
The process of converting a block of text into distinct, normalized tokens in order to:

- create an inverted index and
- query the inverted index.

if you query a full-text (analyzed) field, they will first pass the query string  
through the appropriate analyzer to produce the list of terms to be queried.

#### match Query

main use case for the match query is for full-text search.

```http request
GET /products/_search
Content-Type: application/json

{
    "query": {
        "match": {
            "description": {
                "query": "синяя"
            }
        }
    }
}
```

And results are:

```json
[
  {
    "_index": "products",
    "_id": "1",
    "_score": 0.51159585,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "oKk0sIsBkn7n2iW9zyMQ",
    "_score": 0.51159585,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "p6k0sIsBkn7n2iW98CO6",
    "_score": 0.51159585,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная красная",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "vKk1sIsBkn7n2iW9UCNI",
    "_score": 0.3135587,
    "_source": {
      "name": "ваза под сухоцветы",
      "description": "ваза декоративная под сухоцветы синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  }
]
```

##### Multiword Match Query

```http request
GET /products/_search
Content-Type: application/json

{
    "query": {
        "match": {
            "description": {
                "query": "декоративная ваза синяя"
            }
        }
    }
}
```

Result:

```json

[
  {
    "_index": "products",
    "_id": "1",
    "_score": 0.51159585,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "oKk0sIsBkn7n2iW9zyMQ",
    "_score": 0.51159585,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "p6k0sIsBkn7n2iW98CO6",
    "_score": 0.51159585,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная красная",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "vKk1sIsBkn7n2iW9UCNI",
    "_score": 0.3135587,
    "_source": {
      "name": "ваза под сухоцветы",
      "description": "ваза декоративная под сухоцветы синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  }
]
```

Again all four are returned.  
Because **match** query is executed as follows:

- split into tokens
- tokens are filtered into terms
- each **term** is queried with **term** query
- results are combined with **OR** operator

I.e. query:

```json
{
  "match": {
    "title": "brown fox"
  }
}
```

Is executed the same as:

```json
{
  "bool": {
    "should": [
      {
        "term": {
          "title": "brown"
        }
      },
      {
        "term": {
          "title": "fox"
        }
      }
    ]
  }
}
```

We can provide operator explicitly:

```json
{
  "query": {
    "match": {
      "description": {
        "query": "декоративная ваза синяя",
        "operator": "and"
      }
    }
  }
}
```

Result:

```json
[
  {
    "_index": "products",
    "_id": "1",
    "_score": 1.9256885,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "oKk0sIsBkn7n2iW9zyMQ",
    "_score": 1.9256885,
    "_source": {
      "name": "ваза",
      "description": "ваза декоративная синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  },
  {
    "_index": "products",
    "_id": "vKk1sIsBkn7n2iW9UCNI",
    "_score": 1.5854443,
    "_source": {
      "name": "ваза под сухоцветы",
      "description": "ваза декоративная под сухоцветы синяя",
      "rating": 4.50,
      "price": 5200.00
    }
  }
]
```

### Boosting Query

Imagine that we want to search for documents about "full-text search,"
but we want to give more weight to documents that also mention "Elasticsearch" or "Lucene."
By more weight, we mean that documents mentioning "Elasticsearch" or "Lucene"
will receive a higher relevance `_score` than those that don’t,
which means that they will appear higher in the list of results.

A simple bool query allows us to write this fairly complex logic as follows:

```json
{
  "query": {
    "bool": {
      "must": {
        "match": {
          "content": {
            "query": "full text search",
            "operator": "and"
          }
        }
      },
      "should": [
        {
          "match": {
            "content": "Elasticsearch"
          }
        },
        {
          "match": {
            "content": "Lucene"
          }
        }
      ]
    }
  }
}
```

The more should clauses that match, the more relevant the document.

But what if we want to give more weight to the docs that contain Lucene and even more weight to the docs containing
Elasticsearch?

We can control the relative weight of any query clause by specifying a `boost` value, which defaults to `1`. A `boost`
value greater than `1` increases the relative weight of that clause. So we could rewrite the preceding query as follows:

```json
{
  "query": {
    "bool": {
      "must": {
        "match": {
          "content": {
            "query": "full text search",
            "operator": "and"
          }
        }
      },
      "should": [
        {
          "match": {
            "content": {
              "query": "Elasticsearch",
              "boost": 3
            }
          }
        },
        {
          "match": {
            "content": {
              "query": "Lucene",
              "boost": 2
            }
          }
        }
      ]
    }
  }
}
```

### Multiple Query Strings

#### Best Fields: dis_max Query (Disjunction Max Query)

Disjunction means or (while conjunction means and) so the Disjunction Max Query simply means return documents that match
any of these queries, and return the score of the best matching query.

Imagine having 2 docs:

```json
[
  {
    "title": "Quick brown rabbits",
    "body": "Brown rabbits are commonly seen."
  },
  {
    "title": "Keeping pets healthy",
    "body": "My quick brown fox eats rabbits on a regular basis."
  }
]
```

The user types in the words “Brown fox” and clicks Search.
We don’t know ahead of time if the user’s search terms will be found in the `title` or the `body` field of the post,
but it is likely that **the user is searching for related words**.

Document 2 appears to be the better match.

`bool` query will return wrong result, since it just counts the number of terms.

`dist_max` will return the correct result:

```json
{
  "query": {
    "dis_max": {
      "queries": [
        {
          "match": {
            "title": "Brown fox"
          }
        },
        {
          "match": {
            "body": "Brown fox"
          }
        }
      ]
    }
  }
}
```

return documents that match any of these queries,
and return **the score of the best matching query.**

### multi_match Query

There are several types of multi_match query:

- `best_fields`
- `most_fields`
- `cross_fields`

By default, this query runs as type `best_fields`, which means that it generates a match query
for each field and wraps them in a dis_max query.

This `dis_max` query:

```json
{
  "dis_max": {
    "queries": [
      {
        "match": {
          "title": {
            "query": "Quick brown fox"
          }
        }
      },
      {
        "match": {
          "body": {
            "query": "Quick brown fox"
          }
        }
      }
    ]
  }
}
```

could be rewritten more concisely with `multi_match` as follows:

```json
{
  "multi_match": {
    "query": "Quick brown fox",
    "type": "best_fields",
    "fields": [
      "title",
      "body"
    ]
  }
}
```

#### Boosting individual fields

caret symbol `^` is used for boosting:

```json
{
  "multi_match": {
    "query": "Quick brown fox",
    "fields": [
      "*_title",
      "chapter_title^2"
      // has a boost of 2
    ]
  }
}
```

### [Most Field](https://www.elastic.co/guide/en/elasticsearch/guide/current/most-fields.html): multi_match Query

If a user searches for “quick brown fox,” a document that contains fast foxes may well be a reasonable result to return.
However, if we have two documents, one of which contains jumped and the other jumping, the user would probably expect
the first document to rank higher, as it contains exactly what was typed in.

We can use [multifields](https://www.elastic.co/guide/en/elasticsearch/guide/current/multi-fields.html) to improve
relevance.

Creating an index:
`PUT /my_index`

```json
{
  "settings": {
    "number_of_shards": 1
  },
  "mappings": {
    "my_type": {
      "properties": {
        "title": {
          "type": "string",
          "analyzer": "english",
          "fields": {
            "std": {
              "type": "string",
              "analyzer": "standard"
            }
          }
        }
      }
    }
  }
}
```

- The `title` field is stemmed by the english analyzer.
- The `title.std` field uses the standard analyzer and so is not stemmed.

And we put two docs:

```json
{
  "title": "My rabbit jumps"
}
```

```json
{
  "title": "Jumping jack rabbits"
}
```

The query:

```json
{
  "query": {
    "match": {
      "title": "jumping rabbits"
    }
  }
}
```

Will result to:

```json
{
  "hits": [
    {
      "_id": "1",
      "_score": 0.42039964,
      "_source": {
        "title": "My rabbit jumps"
      }
    },
    {
      "_id": "2",
      "_score": 0.42039964,
      "_source": {
        "title": "Jumping jack rabbits"
      }
    }
  ]
}
```

Because usual `match` query on `title` field uses stemmed values when counting the `_score`.
And the `_score` is the same.

To improve that, we can use `multi_match`:

```json
{
  "query": {
    "multi_match": {
      "query": "jumping rabbits",
      "type": "most_fields",
      "fields": [
        "title",
        "title.std"
      ]
    }
  }
}
```

which results to:

```json
{
  "hits": [
    {
      "_id": "2",
      "_score": 0.8226396,
      "_source": {
        "title": "Jumping jack rabbits"
      }
    },
    {
      "_id": "1",
      "_score": 0.10741998,
      "_source": {
        "title": "My rabbit jumps"
      }
    }
  ]
}
```

`most_field` will wrap query clauses from `multi_match` queries into `bool` clause, instead of `dis_max`.

### [Cross Fields](https://www.elastic.co/guide/en/elasticsearch/guide/current/_cross_fields_entity_search.html): multi_match Query

Suits well for a set of fields like: address, name, etc.
Address:

```json
{
  "address": {
    "country": "RF",
    "city": "Moscow",
    "street": "Tverskaya",
    ...
  }
}
```

And when user searches an address: "Russia, Moscow city, Tverskaya street", we must search among all fields.
`multi_match` query of type `best_fields` won't suite, as well as `most_fields`.

```json
{
  "query": {
    "multi_match": {
      "query": "peter smith",
      "type": "cross_fields",
      "operator": "and",
      "fields": [
        "first_name",
        "last_name"
      ]
    }
  }
}
```

`cross_fields` treats all of the fields as one big field, and looks for each term in any field.

For the `cross_fields` query type to work optimally, all fields should have the same analyzer. Fields that share an
analyzer are grouped together as blended fields.

One of the advantages of using the `cross_fields` query over custom `_all` fields is that you can boost individual
fields at query time.

For fields of equal value like `first_name` and `last_name`, this generally isn’t required, but if you were searching
for books using the `title` and `description` fields, you might want to give more weight to the title field.

```json
{
  "query": {
    "multi_match": {
      "query": "peter smith",
      "type": "cross_fields",
      "fields": [
        "title^2",
        "description"
      ]
    }
  }
}
```

**It is not useful to mix not_analyzed fields with analyzed fields in multi_match queries.**

### match_phrase query

when you want to find words that are near each other

It keeps only documents that contain all of the search terms, in **the same positions relative to each other**.

Requiring exact-phrase matches may be too strict a constraint.
Perhaps we do want documents that contain “quick brown fox” to be considered a match for the query “quick fox,”
even though the positions aren’t exactly equivalent.

```json
{
  "query": {
    "match_phrase": {
      "title": {
        "query": "quick fox",
        "slop": 1
      }
    }
  }
}
```

`slop` means - how many times do you need to move a term in order to make the query and document match.
To make the query `quick fox` match a document containing `quick brown fox` we need a slop of just `1`.

By setting a high `slop` value like `50` or `100`, you can exclude documents in which the words are really too far
apart, but
give a higher score to documents in which the words are closer together.

The following proximity query for `quick dog` matches both documents that contain the words `quick` and `dog`, but gives
a higher score to the document in which the words are nearer to each other:

```json
{
  "query": {
    "match_phrase": {
      "title": {
        "query": "quick dog",
        "slop": 50
      }
    }
  }
}
```

results to:

```json
{
  "hits": [
    {
      "_id": "3",
      "_score": 0.75,
      "_source": {
        "title": "The quick brown fox jumps over the quick dog"
      }
    },
    {
      "_id": "2",
      "_score": 0.28347334,
      "_source": {
        "title": "The quick brown fox jumps over the lazy dog"
      }
    }
  ]
}
```

#### [Proximity for relevance](https://www.elastic.co/guide/en/elasticsearch/guide/current/proximity-relevance.html)

Although proximity queries are useful, the fact that they require all terms to be present can make them overly strict.
if six out of seven terms match, a document is probably relevant enough to be worth showing to the user, but
the `match_phrase` query would exclude it.

Instead of using proximity matching as an absolute requirement, we can use it as a _signal_

We can use a simple `match` query as a `must` clause. This is the query that will determine which documents are included
in
our result set. We can trim the long tail with the `minimum_should_match` parameter. Then we can add other, more
specific
queries as `should` clauses. Every one that matches will increase the relevance of the matching docs.

```json
{
  "query": {
    "bool": {
      "must": {
        "match": {
          "title": {
            "query": "quick brown fox",
            "minimum_should_match": "30%"
          }
        }
      },
      "should": {
        "match_phrase": {
          "title": {
            "query": "quick brown fox",
            "slop": 50
          }
        }
      }
    }
  }
}
```

### [Improving Performance](https://www.elastic.co/guide/en/elasticsearch/guide/current/_improving_performance.html)

Phrase and proximity queries are more expensive than simple `match` queries.
The Lucene nightly benchmarks show that a simple `term` query is about `10` times as fast as a `phrase` query, and about
20
times as fast as a `proximity` query (a phrase query with slop).

#### Rescoring Results

A query may match millions of results, but chances are that **our users are interested in only the first few pages** of
results.

A simple `match` query will already have ranked documents that contain all search terms near the top of the list.
Really, we just want to **rerank the top results** to give an extra relevance bump to those documents that also match
the phrase query.

The search API supports exactly this functionality via `rescoring`. The `rescore phase` allows you to `apply phrase
query to just the top K results` from each shard. These top results are then resorted
according to their new scores.

```json
{
  "query": {
    "match": {
      "title": {
        "query": "quick brown fox",
        "minimum_should_match": "30%"
      }
    }
  },
  "rescore": {
    "window_size": 50,
    "query": {
      "rescore_query": {
        "match_phrase": {
          "title": {
            "query": "quick brown fox",
            "slop": 50
          }
        }
      }
    }
  }
}
```

### Query-Time Search-as-You-Type

For instance, if a user types in `johnnie walker bl`, we would like to show results for **Johnnie Walker Black** Label
and **Johnnie Walker Blue** Label before they can finish typing their query.

```json{
    "match_phrase_prefix" : {
        "brand" : "johnnie walker bl"
    }
}
```

You can use `slop` as with regular `math_phrase` query:

```json
{
  "match_phrase_prefix": {
    "brand": {
      "query": "walker johnnie bl",
      "slop": 10
    }
  }
}
```

Even though the words are in the wrong order, the query still matches because we have set a high enough slop value to
allow some flexibility in word positions.

A prefix of `a` could match hundreds of thousands of terms. Not only would matching on this many terms be resource
intensive, but it would also not be useful to the user.

We can limit the impact of the prefix expansion by setting `max_expansions`

```json
{
  "match_phrase_prefix": {
    "brand": {
      "query": "johnnie walker bl",
      "max_expansions": 50
    }
  }
}
```

#### Index Time Optimizations: [N-grams](https://www.elastic.co/guide/en/elasticsearch/guide/current/_ngrams_for_partial_matching.html)

for search-as-you-type, we use a specialized form of n-grams called edge n-grams. Edge n-grams are anchored to the
beginning of the word. Edge n-gramming the word `quick` would result in this:

- q
- qu
- qui
- quic
- quick

#### Preparing index:

The first step is to configure a custom edge_ngram token filter, which we will call the autocomplete_filter:

```json
{
  "filter": {
    "autocomplete_filter": {
      "type": "edge_ngram",
      "min_gram": 1,
      "max_gram": 20
    }
  }
}
```

This configuration says that, for any term that this token filter receives, it should produce an n-gram anchored to the
start of the word of minimum length 1 and maximum length 20.

Then we need to use this token filter in a custom analyzer, which we will call the `autocomplete` analyzer:

```json
{
  "analyzer": {
    "autocomplete": {
      "type": "custom",
      "tokenizer": "standard",
      "filter": [
        "lowercase",
        "autocomplete_filter"
      ]
    }
  }
}
```

This analyzer will tokenize a string into individual terms by using the `standard` tokenizer, lowercase each term, and
then produce edge n-grams of each term, thanks to our `autocomplete_filter`.

The full request to create the index and instantiate the token filter and analyzer looks like this:

`PUT /my_index`
```json
{
    "settings": {
        "number_of_shards": 1, 
        "analysis": {
            "filter": {
                "autocomplete_filter": { 
                    "type":     "edge_ngram",
                    "min_gram": 1,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "autocomplete": {
                    "type":      "custom",
                    "tokenizer": "standard",
                    "filter": [
                        "lowercase",
                        "autocomplete_filter" 
                    ]
                }
            }
        }
    }
}
```

### [Completion Suggester](https://opensearch.org/docs/2.2/opensearch/search/autocomplete/#completion-suggester)

### [Search As You Type](https://opensearch.org/docs/2.2/opensearch/search/autocomplete/#search-as-you-type)


