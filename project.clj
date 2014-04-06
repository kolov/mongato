(defproject mongato "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.typed "0.2.44"]
                 [com.novemberain/monger "1.4.2"]
                 ]

  :profiles {
              :dev {:dependencies   [[midje "1.5.1"]
                                     [midje-junit-formatter "0.1.0-SNAPSHOT"]]
                    :resource-paths ["config/kuala"]
                    :test-paths     ["test"]
                    :plugins        [[lein-deps-tree "0.1.2"]
                                     [lein-midje "3.0.0"]
                                     [test2junit "1.0.1"]]
                    }
              }
  )
