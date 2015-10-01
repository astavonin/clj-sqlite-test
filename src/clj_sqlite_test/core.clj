(ns clj-sqlite-test.core
  (:require [clj-sqlite-test.db :as db]
            [clojure.tools.logging :as log])
  (:gen-class))


(defn -main [& args]
  (db/init-db)

  (db/add-author 2 0)
  (db/author-info 2)

  (db/add-image 1 1)
  (db/update-image 1 {:commented true})
  (db/image-info 1)
  (db/add-image 1 1)
  )
