;;; Copyright (c) 2013 David Goldfarb. All rights reserved.
;;; Contact info: deg@degel.com
;;;
;;; The use and distribution terms for this software are covered by the Eclipse
;;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;; be found in the file epl-v10.html at the root of this distribution.
;;; By using this software in any fashion, you are agreeing to be bound by the
;;; terms of this license.
;;;
;;; You must not remove this notice, or any other, from this software.


(defproject degel/muxx "0.1.6-SNAPSHOT"
  :description "Deploy multiple production and dev websites on one instance of compojure."
  :url "https://github.com/deg/muxx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]

                 ;; Routing library for Ring web application library
                 [ring/ring-jetty-adapter "1.1.8"]
                 ;; [TODO] Upgrade to compojure 1.1.6, to try to remove these exclusions:
                 ;;   both needed because degel-clojure-utils uses midje 1.4.0 which uses newer
                 [compojure "1.1.5" :exclusions [ring/ring-core org.clojure/tools.macro]]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]


                 ;; HTML generation
                 [hiccup "1.0.4"]

                 [degel/degel-clojure-utils "0.1.20"]]
  :min-lein-version "2.0.0"
  ;:pedantic? :abort
  :main degel.muxx.main
  :profiles {:dev
             { :plugins [[lein-marginalia "0.7.1"]
                         [com.cemerick/austin "0.1.3"]]}})
