(defproject
  markbastian/planning "0.1.1-SNAPSHOT"
  :description "A planning library"
  :url "https://github.com/markbastian/planning"
  :scm {:name "git" :url "https://github.com/markbastian/planning"}
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories [["releases" {:url "https://repo.clojars.org" :creds :gpg}]
                 ["snapshots" {:url "https://repo.clojars.org" :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.priority-map "1.0.0"]
                 [org.clojure/clojurescript "1.10.773" :scope "provided"]
                 [tailrecursion/cljs-priority-map "1.2.1"]]

  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]

  :source-paths ["src/main/clj" "src/main/cljc"]
  :test-paths ["src/test/clj" "src/test/cljc"])
