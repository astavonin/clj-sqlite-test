(ns clj-sqlite-test.db
  (:require [clojure.java.jdbc :as jdbc]
            [java-jdbc.sql :as sql]
            [clj-time.core :as t]
            [dire.core :refer [with-handler!]]
            [clojure.tools.logging :as log]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "database.db"
   })

(defn init-db []
  (jdbc/db-do-commands db
                       (jdbc/create-table-ddl :authors
                                              [:author_id :integer "primary key"]
                                              [:follow :boolean]
                                              [:follow_date :datetime])
                       (jdbc/create-table-ddl :images
                                              [:image_id :integer "primary key"]
                                              [:author_id :integer "references authors (author_id)"]
                                              [:commented :boolean]
                                              [:liked :boolean])

                       "CREATE UNIQUE INDEX image_id ON images(image_id)"
                       "CREATE UNIQUE INDEX author_id ON authors(author_id)"))

(defn add-author [id follow]
  (let [author-info {:author_id id :follow follow :follow_date (if follow (t/now))}]
    (jdbc/insert! db :authors author-info)))

(defn add-image
  ([id author]
   (add-image id author false false))
  ([id author commened liked]
   (jdbc/insert! db :images {:image_id id :author_id author :commented commened :liked liked}))
  )

(defn image-info [id]
  (jdbc/query db ["select * from images where image_id = ?" id]))

(defn author-info [id]
  (jdbc/query db
              (sql/select [:*] :authors
                          (sql/where {:author_id id})))
  )

(defn update-image [id info]
  (jdbc/update! db :images info
                (sql/where {:image_id id})))

(defn update-author [id info]
  (jdbc/update! db :authors info ["author_id = ?" id]))

;; Error handlers ;;

(with-handler! #'init-db
               java.lang.Exception
               (fn [e & args]
                 (log/warn "init-db: " e)))

(with-handler! #'jdbc/insert!
               java.lang.Exception
               (fn [e & args]
                 (log/warn "insertetion error: " e)))

(with-handler! #'jdbc/update!
               java.lang.Exception
               (fn [e & args]
                 (log/warn "update error: " args)))
