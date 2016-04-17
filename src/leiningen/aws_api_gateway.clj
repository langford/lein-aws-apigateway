(ns leiningen.aws-api-gateway
  "Deploy swagger.json to API Gateway"
  (import com.amazonaws.services.apigateway.AmazonApiGatewayClient
          com.amazonaws.services.apigateway.model.ImportRestApiRequest)
  (require [clojure.pprint :refer [pprint]]
           [byte-streams :as byte-streams]
           [amazonica.aws.apigateway :as aws]
           [leiningen.core.project :refer [merge-profiles]]
           [clojure.reflect :refer [reflect]]
           [leiningen.core.eval :refer [eval-in-project]]))


(defn build-args
  [{api-gateway :api-gateway} task]
  (let [{:keys [swagger deploy api-id profile raml-config]} api-gateway
        basearg (case task
                  :update (vector "--update" api-id)
                  :create (vector "--create"))
        stagearg (if (nil? deploy) '() (vector "--deploy" deploy))
        profilearg (if (nil? profile) '() (vector "--profile" profile))
        raml-arg (if (nil? raml-config) '() (vector "--raml-config" raml-config))]
    (concat basearg stagearg profilearg raml-arg (vector swagger))))

(defn update-api
  "Update an existing API"
  [project args]
  (pprint (build-args project :update))
  (println "ApiImporterMain/main" (into-array String (build-args project :update))))


(defn create-api
  "Create a new API"
  [project args]
  (pprint (build-args project :create))
  (println "ApiImporterMain/main" (into-array String (build-args project :create))))

(defn aws-api-gateway
  "Deploy swagger.json to AWS API Gateway"
  {:subtasks [#'create-api #'update-api]}
  [project & [task args]]
  (case task
    "create-api" (create-api project args)
    "update-api" (update-api project args)
    :nil     :not-implemented-yet
    (leiningen.core.main/warn "Use 'create-api' or 'update-api' as subtasks")))

(def myfile (clojure.java.io/file "/Users/trieloff/Documents/excelsior/resources/swagger-example.json"))

(def myswagger (byte-streams/convert myfile java.nio.ByteBuffer))

(def request
               (.withBody
                   (ImportRestApiRequest.)
                   myswagger)
               )

(identity (.setParameters request {"Name" "FooBar"}))

(aws/get-rest-apis :limit 100)


(.importRestApi (AmazonApiGatewayClient.) request)
