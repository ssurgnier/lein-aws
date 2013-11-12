(ns leiningen.aws
  (:require [lein-aws.s3 :refer [s3-put]]
            [lein-aws.elasticmapreduce :as emr
             :refer [run-flow terminate-flow]]
            [lein-aws.datapipeline :refer [list-pipelines
                                           activate-pipeline
                                           get-definition
                                           put-pipeline
                                           create-pipeline]]))

(defn aws
  "Interact with AWS"
  {:help-arglists '([s3-put run-flow terminate-flow list-pipelines
                     put-pipeline activate-pipeline get-definition
                     create-pipeline])
   :subtasks [#'s3-put #'run-flow #'terminate-flow #'list-pipelines
              #'put-pipeline #'activate-pipeline #'get-definition
              #'create-pipeline]}
  [project subtask & args]
  (let [f (case subtask
            "s3-put" s3-put
            "run-flow" run-flow
            "terminate-flow" terminate-flow
            "describe-job-flows" emr/describe-job-flows
            "add-job-flow-steps" emr/add-job-flow-steps
            "create-pipeline" create-pipeline
            "put-pipeline" put-pipeline
            "list-pipelines" list-pipelines
            "activate-pipeline" activate-pipeline
            "get-definition" get-definition
            :else (fn [& _]
                    (println (format "Unknown subtask `%s'" subtask))))]
    (apply f project args)))

