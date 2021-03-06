(ns lein-aws.elasticmapreduce
  (:require [lein-aws.core :refer [parse-flow]]
            [amazonica.core :refer :all]
            [amazonica.aws.elasticmapreduce :as emr]
            [clojure.pprint :refer [pprint]]))

(defn run-flow
  "Launch the elastic mapreduce jobflow `name` specified in the file at `path`"
  [project flow-name path]
  (let [flow-name (keyword flow-name)
        config (:aws project)
        job-flows (parse-flow path)
        flow (get job-flows flow-name)]
    (with-credential [(:access-key config) (:secret-key config)]
      (when-let [id (:job-flow-id (emr/run-job-flow flow))]
        (println (format "job-flow %s starting..." id))
        (loop [status (emr/describe-job-flows :job-flow-ids [id])]
          (case (-> status :job-flows first :execution-status-detail :state)
            "STARTING" (do
                         (Thread/sleep 10000)
                         (recur (emr/describe-job-flows :job-flow-ids [id])))
            "BOOTSTRAPPING" (do
                              (println (format "job-flow %s bootstrapping..." id))
                              (Thread/sleep 10000)
                              (recur (emr/describe-job-flows :job-flow-ids [id])))
            "RUNNING" (println (format "job-flow %s running. master public dns: %s"
                                       id
                                       (-> status :job-flows first :instances :master-public-dns-name)))
            "TERMINATED" (println (format "job-flow %s terminated" id))
            "SHUTTING_DOWN" (println (format "job-flow %s is shutting down" id))
            "FAILED" (println (format "job-flow %s failed" id))
            "WAITING"
            (do (println (emr/describe-job-flows :job-flow-ids [id]))
                (println (format "job-flow %s has entered the Waiting state." id)))))))))

(defn add-job-flow-steps
  [project job-flow-id step-name path]
  (when-let [config (:aws project)]
    (with-credential [(:access-key config) (:secret-key config)]
      (let [job-steps (parse-flow path)
            step-name (keyword step-name)
            step (get job-steps step-name)
            new-step (assoc step :job-flow-id job-flow-id)]
        (do
          (emr/add-job-flow-steps new-step)
          (println (format "Added step to job-flow %s `%s'"
                           job-flow-id new-step)))))))

(defn terminate-flow
  "Terminate the elastic mapreduce jobflows `ids`"
  [project & ids]
  (let [config (:aws project)]
    (with-credential [(:access-key config) (:secret-key config)]
      (emr/terminate-job-flows :job-flow-ids (vec ids)))
    (println "job flow(s) terminated")))

(defn describe-job-flows
  "Describe the elastic mapreduce jobflows `ids`"
  [project & ids]
  (let [config (:aws project)]
    (with-credential [(:access-key config) (:secret-key config)]
      (pprint (emr/describe-job-flows :job-flow-ids (vec ids))))))
