(ns leiningen.aws-api-gateway
  "Deploy swagger.json to API Gateway"
  (import com.amazonaws.service.apigateway.importer.ApiImporterMain)
  (require [clojure.pprint :refer [pprint]]
           [leiningen.core.project :refer [merge-profiles]]
           [clojure.reflect :refer [reflect]]
           [leiningen.core.eval :refer [eval-in-project]]))


(defn build-args
  [{api-gateway :api-gateway} task]
  (let [{:keys [swagger stage api-id profile raml-config]} api-gateway
        basearg (case task
          :update (vector "--update" api-id)
          :create (vector "--create"))
        stagearg (if (nil? stage) '() (vector "--stage" stage))
        profilearg (if (nil? profile) '() (vector "--profile" profile))
        raml-arg (if (nil? raml-config) '() (vector "--raml-config" raml-config))]
    (concat basearg stagearg profilearg raml-arg (vector swagger))))

(defn update-api
  "Update an existing API"
  [project args]
  (pprint (build-args project :update))
  ;(ApiImporterMain/main (into-array String ["--update" (-> project :api-gateway :swagger)]))
  )

(defn create-api
  "Create a new API"
  [project args]
  (pprint (build-args project :create))
  (ApiImporterMain/main (into-array String ["--create"])))

(defn aws-api-gateway
  "Deploy swagger.json to AWS API Gateway"
  {:subtasks [#'create-api #'update-api]}
  [project & [task args]]
  (case task
    "create-api" (create-api project args)
    "update-api" (update-api project args)
    :nil     :not-implemented-yet
    (leiningen.core.main/warn "Use 'create-api' or 'update-api' as subtasks")))
