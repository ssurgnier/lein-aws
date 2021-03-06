# lein-aws

This is very use case specific plugin to suit my own needs, but I'd
like to expand it to be a more general purpose AWS plugin; pull
requests welcomed!

Right it it supposed uploading files to s3 via the `s3-put` subtask,
running and terminating jobflow via the `run-flow` and `terminate-job`
subtask respectively. It is based on the awesome
[amazonica](https://github.com/mcohen01/amazonica) library.

Job flows are defined and loaded from a file containing a clojure definition of a
keyword identified to an amazonina run-job-flow map.

```clojure
{:my-flow
  {:name "my-job-flow"
  :log-uri "s3n://emr-logs/logs"
  :instances
    {:instance-groups [
       {:instance-type "m1.large"
        :instance-role "MASTER"
        :instance-count 1
        :market "SPOT"
        :bid-price "0.10"}]}
  :steps [
    {:name "my-step"
     :hadoop-jar-step
       {:jar "s3n://beee0534-ad04-4143-9894-8ddb0e4ebd31/hadoop-jobs/bigml"
        :main-class "bigml.core"
        :args ["s3n://beee0534-ad04-4143-9894-8ddb0e4ebd31/data" "output"]}}]}
}
```

Since job flows are parsed as clojure, you can factor out common steps:

```clojure
(let [enable-debugging
      {:name "Enable Debugging"
       :hadoop-jar-step
       {:jar "s3://elasticmapreduce/libs/script-runner/script-runner.jar"
        :args ["s3://elasticmapreduce/libs/state-pusher/0.1/fetch"]}}
      master-m1-xlarge
      {:instance-type "m1.xlarge"
       :instance-role "MASTER"
       :instance-count 1}]
  {:stage-job-flow
   {:name "Run job flow in stage"
    :log-uri "s3n://emr-logs/logs"
    :instances
    {:hadoop-version "1.0.3"
     :instance-groups
     [master-m1-xlarge]}
    :steps
    [enable-debugging
     {:name "run-step-in-staging"
      :hadoop-jar-step
      {:jar "s3n://beee0534-ad04-4143-9894-8ddb0e4ebd31/hadoop-jobs/bigml"
       :main-class "bigml.core"
       :args ["s3n://beee0534-ad04-4143-9894-8ddb0e4ebd31/data" "staging"]}}]}
   :prod-job-flow
   {:name "Run job flow in prod"
    :log-uri "s3n://emr-logs/logs"
    :instances
    {:hadoop-version "1.0.3"
     :instance-groups
     [master-m1-xlarge]}
    :steps
    [enable-debugging
     {:name "my-step"
      :hadoop-jar-step
      {:jar "s3n://beee0534-ad04-4143-9894-8ddb0e4ebd31/hadoop-jobs/bigml"
       :main-class "bigml.core"
       :args ["s3n://beee0534-ad04-4143-9894-8ddb0e4ebd31/data" "prod"]}}]}})
```

See the amazonica docs for more information.

## Usage

You need to let the plugin know about your aws credentils:

```clojure
:aws {:access-key "ACCESSKEY"
      :secret-key "SECRETKEY"
      :email "aws-login-email"}
```

Use this for user-level plugins:

Put `[lein-aws "0.1.4-SNAPSHOT"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-aws 0.1.4-SNAPSHOT`.

Use this for project-level plugins:

Put `[lein-aws "0.1.4-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

    $ lein help aws

## License

Copyright © 2013 Soren Macbeth

Distributed under the Eclipse Public License, the same as Clojure.
