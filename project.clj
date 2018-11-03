(defproject
  planning "0.1.0-SNAPSHOT"
  :description "A planning library"
  :url "https://github.com/markbastian/planning"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/data.priority-map "0.0.10"]
                 [quil "2.4.0"]]

  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-cljsbuild "1.1.4"]
                             [org.clojure/clojurescript "1.9.229"]]}
             :cljs {:plugins [[lein-cljsbuild "1.1.4"]] }}

  :source-paths ["src/clj" "src/cljc"]

  :clj {:builds [{ :source-paths ["src/clj" "src/cljc" "test"] }]}

  :repl-options {:init-ns planning.core}

  :cljsbuild {:builds [{ :source-paths ["src/cljs" "src/cljc"]
                        :dependencies [[tailrecursion/cljs-priority-map "1.2.1"]]
                        :compiler { :output-to "resources/public/js/planning.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]})
