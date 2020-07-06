(defproject
  planning "0.1.0-SNAPSHOT"
  :description "A planning library"
  :url "https://github.com/markbastian/planning"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.priority-map "1.0.0"]]

  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]

  :aliases {"min-build" ["with-profile" "cljs" "cljsbuild" "once" "min"]
            "fig" ["with-profile" "cljs" "figwheel"]}

  :profiles {:uberjar {:aot :all}
             :dev     {:plugins      [[lein-figwheel "0.5.20"]
                                      [lein-cljsbuild "1.1.7"]
                                      [org.clojure/clojurescript "1.10.439"]]
                       :dependencies [[cider/piggieback "0.5.0"]
                                      [figwheel-sidecar "0.5.20"]]
                       :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}
             :cljs    {:plugins      [[lein-cljsbuild "1.1.7"]]
                       :dependencies [[org.clojure/clojurescript "1.10.773"]
                                      [tailrecursion/cljs-priority-map "1.2.1"]
                                      [reagent "0.10.0"]
                                      [cljsjs/hammer "2.0.8-0"]]}}

  :source-paths ["src/main/clj" "src/main/cljc"]
  :test-paths ["src/test/clj" "src/test/cljc"]

  :repl-options {:init-ns planning.core}

  :cljsbuild {:builds
              {:dev {:source-paths ["src/main/cljs" "src/main/cljc"]
                     :figwheel     true
                     :compiler     {:main                 planning.app
                                    :asset-path           "js/out"
                                    :output-to            "resources/public/js/planning.js"
                                    :output-dir           "resources/public/js/out"
                                    :source-map-timestamp true
                                    :pretty-print         true}}

               :min {:source-paths ["src/main/cljs" "src/main/cljc"]
                     :compiler     {:main          planning.app
                                    :output-to     "resources/public/js/planning.js"
                                    :optimizations :advanced
                                    :pretty-print  false}}}}
  :figwheel {:css-dirs ["resources/public/css"]})
