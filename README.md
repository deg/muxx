muxx
====

Deploy multiple production and dev websites on one instance of compojure.

The Muxx library wraps around Compojure to give an easy way to deploy
multiple small web apps to a single server.

_Note: I've designed muxx for single-page JavaScript apps. It can certainly
be generalized to deal with other kinds of web sites, but I've not yet
given any thought to such directions._

Goals:
* Let each web app live in its own Clojure project
* Create deployments that bundle multiple independent apps on a single server
* Supported for real deployment as well as testing on localhost
* Support for dev and production versions of each app
* Support for ClojureScript and browser-repls, e.g. via [https://github.com/cemerick/austin](Austin).
* Don't hide any of the underlying flexibility of compojure, ring, leiningen, etc.

Motivations to use muxx include increased modularity and reduced cloud
deployment costs for apps that are very lightly used. For example, I
have several personal apps that I access only once every few days, but
I want them each to be instantly available when I need them. It would
be foolish to keep a VM instance running 24/7 for each one of them.

## Usage

The latest version of this project on clojars is:
![latest version](https://clojars.org/muxx/latest-version.svg)

Configure your DNS to find all of your apps at the IP of your hosting service.

It's also very useful to map "*.local.yourdomain.com" to 127.0.0.1
(localhost). Then you can run production and dev versions of all your
apps locally and simultaneously:
* `app1.local.yourdomain.com` is the production version of app1
* `app1-dev.local.yourdomain.com` is the dev version of app1
* `app2.local.yourdomain.com` is the production version of app2
* `app2-dev.local.yourdomain.com` is the dev version of app2
* etc.

Each web app is registered by calling degel.muxx.server/add-app.

Then, you need to write a deployment function that calls,
e.g. run-jetty on muxx/app.

(Note: this is still undergoing rapid evolution. I hope to have a much
cleaner interface very soon).

## License

Copyright © 2013 David Goldfarb, deg@degel.com

Distributed under the Eclipse Public License, the same as Clojure.

The use and distribution terms for this software are covered by the
[Eclipse Public License
1.0](http://opensource.org/licenses/eclipse-1.0.php) which can be
found in the file epl-v10.html at the root of this distribution.  By
using this software in any fashion, you are agreeing to be bound by
the terms of this license.

You must not remove this notice, or any other, from this software.
