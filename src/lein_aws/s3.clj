(ns lein-aws.s3
  (:require [amazonica.core :refer :all]
            [amazonica.aws.s3 :as s3]
            [lein-aws.core :refer [parse-flow]]))

(defn s3-put
  "Puts the file at `path` to `bucket` with ACL `acl`."
  ([project acl bucket path]
     (let [config (:aws project)
           f (clojure.java.io/file path)
           key (.getName f)
           acl (case acl
                 "public-read" [[(:email config) "FullControl"] ["AllUsers" "Read"]]
                 "private" [[(:email config) "FullControl"]]
                 "public-read-write" [[(:email config) "FullControl"] ["AllUsers" "ReadWrite"]])]
       (with-credential [(:access-key config) (:secret-key config)]
         (println (format "%s -> s3://%s/%s" path bucket key))
         (s3/put-object :bucket-name bucket
                        :key key
                        :file f
                        :access-control-list
                        {:grant-all acl}))))
  ([project step-name config-path]
     (when-let [config (:aws project)]
       (with-credential [(:access-key config) (:secret-key config)]
         (let [job-steps (parse-flow config-path)
               step-name (keyword step-name)
               {:keys [file bucket-name key] :as step}
               (get job-steps step-name)]
           (println (format "%s -> %s/%s" file bucket-name key))
           (s3/put-object step))))))
