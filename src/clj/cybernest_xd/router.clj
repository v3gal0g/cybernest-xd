(ns cybernest-xd.router
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition.table :refer [table-routes]]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.csrf :as csrf]
            [io.pedestal.http.content-negotiation :as conneg]
            [ring.util.response :as response]
            [integrant.core :as ig]

            ;; [reitit.ring :as ring]
            ;; [reitit.http :as http]
            ;; [reitit.coercion.spec]
            ;; [reitit.swagger :as swagger]
            ;; [reitit.swagger-ui :as swagger-ui]
            ;; [reitit.http.coercion :as coercion]
            ;; [reitit.dev.pretty :as pretty]
            ;; [reitit.http.interceptors.parameters :as parameters]
            ;; [reitit.http.interceptors.muuntaja :as muuntaja]
            ;; [reitit.http.interceptors.exception :as exception]
            ;; [reitit.http.interceptors.multipart :as multipart]
            ;; [reitit.pedestal :as pedestal]
            ;; [ring.middleware.reload :refer [wrap-reload]]
            ;; [muuntaja.core :as m]
            ;; [clojure.core.async :as a]
            ;; [clojure.java.io :as io]

            [hashp.core]
            [next.jdbc :as jdbc]

            [spyscope.core]
            [cybernest-xd.journal :as xd-play]
            [cybernest-xd.db :as db]))





(defn hello-interceptor [request]
  {:status 200 :body "Hello, chrysalis 2"})

(defn hello [req]
  {:status 200 :body "Chrysalis [xD]"})

;; So this will be the macrocosm for my push. My personal database and helper but I 'll also be able to push to Chrysalis-XD from here
(defn response
  ([status body]
   (response status body {}))
  ([status body headers]
   {:status status :body body :headers headers}))


(def common-interceptors [(body-params/body-params) http/html-body])
(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])
(def content-negotiation-interceptor (conneg/negotiate-content supported-types))

;; TODO: How to handle multple data response formats? Showing specifically should return either json or html
(def routes (route/expand-routes
             #{["/" :get hello-interceptor :route-name :greet]
               ["/iotas" :get [(body-params/body-params)
                               http/json-body
                               db/find-all-iotas]]
               ["/iota" :post  [(body-params/body-params)
                                ;; db/insert-iota :route-name ::db/insert-iota
                                http/json-body
                                db/insert-iota] :route-name ::db/insert-iota]})) ; NOTE: 500 error but it's still writing to the database.
                                        ; NOTE: COME BACK HERE AND TAKE A GOOD LOOK, it works but I still get a wrong number of args passed


;; ----------------------------------------------------------------------------------------------------------------------------------------
;; (def router
;;     (pedestal/routing-interceptor
;;      (http/router
;;       [["/swagger.json"
;;         {:get {:no-doc  true
;;                :swagger {:info {:title       "cybernest-xd: per aspera ad astra"
;;                                 :description "my paracosmic playground"}}
;;                :handler (swagger/create-swagger-handler)}}]

;;        ["/"
;;         {:swagger {:tags ["basic"]}

;;          :get  {:interceptor hello-interceptor}
;;          {"/iota" {:interceptors
;;                    :post {:interceptor db/insert-iota}}

;;           }
;;          }]

;;        #_["iota"
;;           :post {:interceptor xd-play }]
;;        ;; TODO: now how to make this work...
;;        ;; TODO: check what you're doing here... your swagger tags should be seperate and above allroutes under topic

;;        #_["/iota"
;;           {:swagger    {:tags ["iotas"]}
;;            :parameters {:body {:architect_id int?, :post string?}}
;;            ;; :get {:interceptors journal/post-iota}
;;            :post       {:handler
;;                         (fn [context]
;;                           (let [architect-id (-> context :request :json-params :architect_id)
;;                                 post         (-> context :request :json-params :post)
;;                                 ]
;;                             (journal/query! (journal/post-iota {:architect_id architect-id :post post}))
;;                             ))}}]]

;;       { ;:reitit.interceptor/transform dev/print-context-diffs ;; pretty context diffs
;;        ;;:validate spec/validate ;; enable spec validation for route data
;;        ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
;;        :exception pretty/exception
;;        :data      {:coercion     reitit.coercion.spec/coercion
;;                    :muuntaja     m/instance
;;                    :interceptors [ ;; swagger feature
;;                                   swagger/swagger-feature
;;                                   ;; query-params & form-params
;;                                   (parameters/parameters-interceptor)
;;                                   ;; content-negotiation
;;                                   (muuntaja/format-negotiate-interceptor)
;;                                   ;; encoding response body
;;                                   (muuntaja/format-response-interceptor)
;;                                   ;; exception handling
;;                                   (exception/exception-interceptor)
;;                                   ;; decoding request body
;;                                   (muuntaja/format-request-interceptor)
;;                                   ;; coercing response bodys
;;                                   (coercion/coerce-response-interceptor)
;;                                   ;; coercing request parameters
;;                                   (coercion/coerce-request-interceptor)
;;                                   ;; multipart
;;                                   (multipart/multipart-interceptor)]}})

;;      ;; optional default ring handler (if no routes have matched)
;;      (ring/routes
;;       (swagger-ui/create-swagger-ui-handler
;;        {:path   "/swagger"
;;         :config {:validatorUrl     nil
;;                  :operationsSorter "alpha"}})
;;       (ring/create-resource-handler {:path "/"})
;;       (ring/create-default-handler))))


(def url-for (route/url-for-routes routes))
(def form-action (route/form-action-for-routes routes))

#_(form-action :create-iota :params {:architect_id 1 :post "ok"})
#_(url-for :create-iota :params {:architect_id 1 :post "hey"})
