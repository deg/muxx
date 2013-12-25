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
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [redirect]]
            [shoreleave.middleware.rpc :as rpc]
            [net.cgrand.enlive-html :as html]
            [compojure.handler :refer [site]]
            [cemerick.austin :as austin]
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
   dev-background-image ;; Watermark to  show on dev page
   ])


(defn add-app
  "Add an app to this deployment. Apps are keyed by name."
  [{:keys [name base-page production-js dev-js dev-background-image] :as app-map}]
  (swap! app-dispatch
         assoc name (->app-data name base-page production-js dev-js dev-background-image)))


(defn default-app-properties
  "Default app description properties. Apps can override these, but the default
   values are usually fine."
  [app-name]
  {:name app-name
   :base-page (str "/" app-name ".html")
   :production-js (str "js/" app-name ".js")
   :dev-js (str "js/" app-name "-dev.js")
   :dev-background-image "dev-page.png"})


(defn- dev-page
  "Create a development page from the production page. Currently, this
   changes which JavaScript is run (typically to support debugging), and
   injects Austin support for brower-repl access to the page."
  ([{:keys [base-page production-js dev-js dev-background-image]}]
     (dev-page (str "public" base-page) production-js dev-js dev-background-image))
  ([page production-js dev-js dev-background-image]
     ((html/template page []
        [:body] (html/set-attr :background dev-background-image)
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


(def app (-> app-routes rpc/wrap-rpc site))


(defn run-servers
  "Main entry-point. Runn Muxx to multiplex one or more apps"
  [& {:keys [apps port]}]
  ;; [TODO] Using an atom for this is kinda grody. Is there some
  ;;        cleaner way to pass our state down to app-routes?
  (dorun (map add-app apps))
  (defonce ^:private server
    (run-jetty #'app {:port (Integer. (or port (System/getenv "PORT") 3000))
                      :join? false}))
  server)


(defn run-client-repl
  "Trampoline a server repl into a client page hosting the clojurescript code.
   You then need to load or refresh the page."
  []
  (let [repl-env (reset! austin-repls/browser-repl-env (austin/repl-env))]
    (austin-repls/cljs-repl repl-env)))
