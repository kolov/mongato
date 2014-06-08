(defproject com.akolov/mongato "0.2.0-SNAPSHOT"
  :description "Adding metainfo to Monger maps "
  :url "https://github.com/kolov/mongato"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/monger "1.4.2"]
                 ]
  :profiles {
              :dev {:dependencies [[midje "1.6.3"]
                                   [midje-junit-formatter "0.1.0-SNAPSHOT"]]
                    :test-paths   ["test"]
                    :plugins      [[lein-deps-tree "0.1.2"]
                                   [lein-midje "3.1.3"]
                                   [test2junit "1.0.1"]
                                   [lein-release "1.0.5"]]
                    }
              }
  )
