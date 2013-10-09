;;; Copyright (c) 2013, David Goldfarb. All rights reserved.
;;; Contact info: deg@degel.com
;;;
;;; The use and distribution terms for this software are covered by the Eclipse
;;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;; be found in the file epl-v10.html at the root of this distribution.
;;; By using this software in any fashion, you are agreeing to be bound by the
;;; terms of this license.
;;;
;;; You must not remove this notice, or any other, from this software.


(ns degel.muxx.server
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]
            [net.cgrand.enlive-html :as html]
            [compojure.handler :refer [site]]
            [cemerick.austin.repls :as austin-repls]
            [degel.cljutil.devutils :as dev]))


;;; All of the web apps we are handling.
;;; Call app-app to add or modify.
;;; [TODO] Do we need an API to remove a website?
(def app-dispatch (atom {}))

(defrecord ^:private app-data
  [name           ;; Name of a website
   base-page      ;; base html page, for the production site.
   production-js  ;; JavaScript to load on the production page.
   dev-js         ;; JavaScript to load on the development page.
   ])


(defn add-app
  "Add an app to this deployment. Apps are keyed by name."
  [& {:keys [name base-page production-js dev-js]}]
  (swap! app-dispatch
         assoc name (->app-data name base-page production-js dev-js)))


(defn- dev-page
  "Create a development page from the production page. Currently, this
   changes which JavaScript is run (typically to support debugging), and
   injects Austin support for brower-repl access to the page."
  ([{:keys [base-page production-js dev-js]}]
     (dev-page (str "public" base-page) production-js dev-js))
  ([page production-js dev-js]
     ((html/template page []
        [:body] (html/append (html/html [:script (austin-repls/browser-connected-repl-js)]))
        [[:script (html/attr= :src production-js)]] (html/set-attr :src dev-js)))))


(defn- find-site-records
  "Match the server name to choose one of our apps"
  [server-request]
   (filter (fn [{:keys [name] :as record}]
             (re-matches (re-pattern (str "(?i).*" name ".*")) server-request))
           (vals @app-dispatch)))

(defn dev-site?
  "Is the app name suffixed with '-dev'? If so, this is a request for the development site."
  [server-request {:keys [name] :as record}]
  (string? (re-matches (re-pattern (str "(?i).*" name "-dev.*")) server-request)))


;;; [TODO] Need informative and customizable error pages.
(defroutes app-routes
  (GET "/" {:keys [server-name] :as all-keys}
    (let [[matching-site & extra-matches] (find-site-records server-name)]
      (cond (nil? matching-site)        (not-found "<h1>Muxx moans: 'app website not found'.</h1>")
            extra-matches               (not-found "<h1>Muxx moans: 'Ambiguous app website URL'.</h1>")
            (dev-site? server-name
                       matching-site)   (dev-page matching-site)
            true                        (redirect (:base-page matching-site)))))
  (resources "/")   ;; to serve static pages saved in resources/public directory
  (not-found "<h1>Muxx moans: 'page not found'.</h1>"))


(def app (-> app-routes wrap-rpc site))
